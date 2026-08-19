package it.requestassistant.adapters.outlook;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import it.requestassistant.application.port.out.MessageInterface;
import it.requestassistant.domain.model.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MessageService implements MessageInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mail.folder.root}")
    private String rootFolderName = "Pa.Voci@almaviva.it";

    @Value("${mail.folder.in_arrivo}")
    private String inArrivoFolderName = "RichiesteAbilitazioni";

    @Value("${mail.folder.in_lavorazione}")
    private String inLavorazioneFolderName = "in_lavorazione"; // Pa.Voci@almaviva.it/RichiesteAbilitazioni/in_lavorazione

    @Value("${mail.folder.evase}")
    private String evaseFolderName = "evase"; // Pa.Voci@almaviva.it/RichiesteAbilitazioni/evase

    private ActiveXComponent outlook;
    private Dispatch namespace;
    private Dispatch rootFolder;
    private Dispatch inArrivoFolder;
    private Dispatch inLavorazioneFolder;
    private Dispatch evaseFolder;

    /**
     * Restituisce il componente ActiveX Outlook.Application
     * @return
     */
    @PostConstruct
    private void initOutlookConnection() throws Exception {
        outlook = new ActiveXComponent("Outlook.Application");
        if(Objects.isNull(outlook)) throw new Exception("Errore durante recupero connessione Outlook!");
        logger.info("Stabilita connessione Outlook.");

        namespace = outlook.getProperty("Session").toDispatch();
        if(Objects.isNull(namespace)) throw new Exception("Errore durante recupero del Namespace!");
        logger.info("Namespace recuperato.");

        rootFolder = findFolder(namespace, rootFolderName); //pa.voci@almaviva.it
        if(Objects.isNull(rootFolder)) throw new Exception("Root folder "+rootFolderName +" non trovato!" );
        logger.info("Root folder "+rootFolderName +" trovato.");

        inArrivoFolder = findFolder(rootFolder, inArrivoFolderName);
        if(Objects.isNull(inArrivoFolder)) throw new Exception("Folder "+inArrivoFolderName +" non trovato!" );
        logger.info("Folder "+inArrivoFolderName +" trovato.");

        inLavorazioneFolder = findFolder(inArrivoFolder, inLavorazioneFolderName);
        if(Objects.isNull(inLavorazioneFolder)) throw new Exception("Folder "+inLavorazioneFolderName +" non trovato!" );
        logger.info("Folder "+inLavorazioneFolderName +" trovato.");

        evaseFolder = findFolder(inArrivoFolder, evaseFolderName);
        if(Objects.isNull(evaseFolder)) throw new Exception("Folder "+evaseFolderName +" non trovato!" );
        logger.info("Folder "+evaseFolderName +" trovato.");

    }

    /**
     * chiude la connessione outlook
     *
     */
    @PreDestroy
    private void releaseOutlookConnection() {
        if(!Objects.isNull(outlook)){
            outlook.safeRelease();
            logger.info("Chiusura connessione Outlook!");
        }
    }

    /**
     * restituisce tutti i messaggi da elaborare
     *
     * @return
     */
    @Override
    public List<Message> findMessagesToProcess() {
        List<Message> messages = new ArrayList<>();

        //recupero la mail dalla cartella delle richieste
        Dispatch items =
                Dispatch.get(inArrivoFolder, "Items").toDispatch();

        int count = Dispatch.get(items, "Count").getInt();
        logger.info("Trovate " + count + " email in " + inArrivoFolderName);

        for (int i = 1; i <= count; i++) {
            logger.debug("conversione messaggio " + i);
            messages.add(mailToMessage(Dispatch.call(items, "Item", new Variant(i)).toDispatch()));
        }

        return messages;
    }

    /**
     * metedo di utility, restituisce il dispatch del folder che contiene la mail con entryId passato per input.
     *
     * @param sourceFolder
     * @param entryId
     * @return
     */
    private Dispatch findMailByEntryId(Dispatch sourceFolder, String entryId) {
        Dispatch mail = null;

        logger.debug("Ricerca mail con entryId " + entryId);

        Dispatch items =
                Dispatch.get(sourceFolder, "Items").toDispatch();
        int count = Dispatch.get(items, "Count").getInt();

        for (int i = 1; i <= count; i++) {
            mail = Dispatch.call(items, "Item", new Variant(i))
                    .toDispatch();
            if(entryId.equals(Dispatch.get(mail, "EntryID").getString())){
                logger.debug("Mail con entryId " + entryId+" trovata.");
                return mail;
            }
        }

        return mail;
    }

    /**
     * Sposta il message da inArrivoFolder a inLavorazioneFolder
     *
     * @param message
     * @throws Exception
     */
    @Override
    public void moveMessageInProgress(Message message) throws Exception {
        Dispatch mailToMove = findMailByEntryId(inArrivoFolder, message.entryId());
        Dispatch.call(mailToMove, "Move", inLavorazioneFolder);
    }

    @Override
    public void moveMessageInDone(Message message) throws Exception {
        Dispatch mailToMove = findMailByEntryId(inLavorazioneFolder, message.entryId());
        Dispatch.call(mailToMove, "Move", evaseFolder);
    }


    /**
     * Rimappa la "mail" in "message"
     *
     * @param mail
     * @return
     */
    private Message mailToMessage(Dispatch mail) {

        return new Message(
                Dispatch.get(mail, "Subject").getString(),
                Dispatch.get(mail, "SenderEmailAddress").getString(),
                Dispatch.get(mail, "ReceivedTime").getJavaDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime(),
                Dispatch.get(mail, "To").getString(),
                Dispatch.get(mail, "CC").getString(),
                Dispatch.get(mail, "Body").getString(),
                Dispatch.get(mail, "EntryID").getString(),
                Dispatch.get(mail, "ConversationID").getString(),
                Dispatch.get(mail, "ConversationTopic").getString(),
                Dispatch.get(mail, "Importance").getInt(),
                hasAttachments(mail),
                Dispatch.get(mail, "Categories").getString()
        );

    }

    private Boolean hasAttachments(Dispatch mail) {
        Dispatch attachments =
                Dispatch.get(mail, "Attachments").toDispatch();

        return Dispatch.get(attachments, "Count").getInt() > 0 ? true : false;

    }


    /**
     * restituisce il forder cercato partendo da una posizione
     *
     * @param startFolder
     * @param targetFolderName
     * @return
     */
    private Dispatch findFolder(Dispatch startFolder, String targetFolderName) {
        boolean trovata = false;
        Dispatch foundFolder = null;

        Dispatch folders =
                Dispatch.get(startFolder, "Folders").toDispatch();
        logger.info("Prelevati folders");

        int count = Dispatch.get(folders, "Count").getInt();
        logger.info("Folder trovati: " + count);

        for (int i = 1; i <= count && !trovata; i++) {
            foundFolder = Dispatch.call(
                    folders,
                    "Item",
                    new Variant(i)
            ).toDispatch();

            String name = Dispatch.get(foundFolder, "Name").getString();
            logger.debug("Cartella: " + name);

            if (targetFolderName.equalsIgnoreCase(name)) {
                trovata = true;
            }
        }

        if (!trovata) {
            logger.info("Cartella non trovata");
            return null;
        }
        return foundFolder;
    }
}