package it.requestassistant.adapters.outlook;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import it.requestassistant.application.port.out.MessagePort;
import it.requestassistant.domain.model.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MessagePortAdapter implements MessagePort {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mail.folder.root}")
    private String rootFolderName = "Pa.Voci@almaviva.it";

    @Value("${mail.folder.in_arrivo}")
    private String inArrivoFolderName = "RichiesteAbilitazioni";

    @Value("${mail.folder.in_lavorazione}")
    private String inLavorazioneFolderName = "in_lavorazione"; // Pa.Voci@almaviva.it/RichiesteAbilitazioni/in_lavorazione

    @Value("${mail.folder.evase}")
    private String evaseFolderName = "evase"; // Pa.Voci@almaviva.it/RichiesteAbilitazioni/evase

    @Value("${mail.folder.scartate}")
    private String scartateFolderName = "scartate"; // Pa.Voci@almaviva.it/RichiesteAbilitazioni/scartate

    private ActiveXComponent outlook;
    private Dispatch namespace;
    private Dispatch rootFolder;
    private Dispatch inArrivoFolder;
    private Dispatch inLavorazioneFolder;
    private Dispatch evaseFolder;
    private Dispatch scartateFolder;

    /**
     * Restituisce il componente ActiveX Outlook.Application
     *
     * @return
     */
    //@PostConstruct
    private void initOutlookConnection() throws Exception {
        outlook = new ActiveXComponent("Outlook.Application");
        if (Objects.isNull(outlook)) throw new Exception("Errore durante recupero connessione Outlook!");
        logger.info("Stabilita connessione Outlook.");

        namespace = outlook.getProperty("Session").toDispatch();
        if (Objects.isNull(namespace)) throw new Exception("Errore durante recupero del Namespace!");
        logger.info("Namespace recuperato.");

        rootFolder = findFolder(namespace, rootFolderName); //pa.voci@almaviva.it
        if (Objects.isNull(rootFolder)) throw new Exception("Root folder " + rootFolderName + " non trovato!");
        logger.info("Root folder " + rootFolderName + " trovato.");

        inArrivoFolder = findFolder(rootFolder, inArrivoFolderName);
        if (Objects.isNull(inArrivoFolder)) throw new Exception("Folder " + inArrivoFolderName + " non trovato!");
        logger.info("Folder " + inArrivoFolderName + " trovato.");

        inLavorazioneFolder = findFolder(inArrivoFolder, inLavorazioneFolderName);
        if (Objects.isNull(inLavorazioneFolder))
            throw new Exception("Folder " + inLavorazioneFolderName + " non trovato!");
        logger.info("Folder " + inLavorazioneFolderName + " trovato.");

        evaseFolder = findFolder(inArrivoFolder, evaseFolderName);
        if (Objects.isNull(evaseFolder)) throw new Exception("Folder " + evaseFolderName + " non trovato!");
        logger.info("Folder " + evaseFolderName + " trovato.");

        scartateFolder = findFolder(inArrivoFolder, scartateFolderName);
        if (Objects.isNull(scartateFolder)) throw new Exception("Folder " + scartateFolderName + " non trovato!");
        logger.info("Folder " + scartateFolderName + " trovato.");
    }

    /**
     * chiude la connessione outlook
     *
     */
    //@PreDestroy
    private void releaseOutlookConnection() {
        if (!Objects.isNull(outlook)) {
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

        try {
            checkAndRefreshOutlookConnection();
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

        } finally {
            releaseOutlookConnection();
        }
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
        boolean trovata = false;

        logger.debug("Ricerca mail con entryId {} nel folder {}",
                entryId, Dispatch.get(sourceFolder, "Name").getString());

        Dispatch items =
                Dispatch.get(sourceFolder, "Items").toDispatch();
        int count = Dispatch.get(items, "Count").getInt();

        String entryIdTest = "";
        for (int i = 1; i <= count && !trovata; i++) {
            mail = Dispatch.call(items, "Item", new Variant(i))
                    .toDispatch();
            entryIdTest = Dispatch.get(mail, "EntryID").getString();
            trovata = entryId.equals(entryIdTest);
        }
        if (trovata) {
            logger.debug("Mail con entryId " + entryId + " trovata.");
            return mail;
        }


        return null;
    }

    /**
     * Sposta il message da inArrivoFolder a inLavorazioneFolder
     *
     * @param message
     * @throws Exception
     */
    @Override
    public Message moveMessageInProgress(Message message) throws Exception {
        try {
            checkAndRefreshOutlookConnection();
            Dispatch mailToMove = findMailByEntryId(inArrivoFolder, message.entryId());

            if (Objects.isNull(mailToMove)) {
                throw new Exception("Mail con entryId " + message.entryId() + " non trovata nella cartella " + inArrivoFolderName);
            }

            return mailToMessage(Dispatch.call(mailToMove, "Move", inLavorazioneFolder).getDispatch());
        } finally {
            releaseOutlookConnection();
        }
    }

    @Override
    public Message moveMessageInDone(Message message) throws Exception {
        try {
            checkAndRefreshOutlookConnection();
            Dispatch mailToMove = findMailByEntryId(inLavorazioneFolder, message.entryId());

            if (Objects.isNull(mailToMove)) {
                throw new Exception("Mail con entryId " + message.entryId() + " non trovata nella cartella " + inLavorazioneFolderName);
            }

            return mailToMessage(Dispatch.call(mailToMove, "Move", evaseFolder).getDispatch());
        } finally {
            releaseOutlookConnection();
        }
    }

    @Override
    public Message moveMessageInDiscarded(Message message) throws Exception {
        try {
            checkAndRefreshOutlookConnection();
            Dispatch mailToMove = findMailByEntryId(inLavorazioneFolder, message.entryId());

            if (Objects.isNull(mailToMove)) {
                throw new Exception("Mail con entryId " + message.entryId() + " non trovata nella cartella " + inLavorazioneFolderName);
            }

            return mailToMessage(Dispatch.call(mailToMove, "Move", scartateFolder).getDispatch());
        } finally {
            releaseOutlookConnection();
        }
    }

    private void checkAndRefreshOutlookConnection() {
        try {
            releaseOutlookConnection();
            initOutlookConnection();
        } catch (Exception e) {
            logger.error("Errore durante l'inizializzazione della connessione a Outlook", e);
            releaseOutlookConnection();
            throw new RuntimeException(e);
        }

    }

    @Override
    public void displayMessage(Message message) {
        try {
            checkAndRefreshOutlookConnection();

            logger.debug("Mostro la mail con entryId {} che si trova in {}", message.entryId(), inLavorazioneFolderName);
            Dispatch mailToShow = findMailByEntryId(inLavorazioneFolder, message.entryId());

            if (Objects.isNull(mailToShow)) {
                throw new RuntimeException("Mail con entryId " + message.entryId() + " non trovata nella cartella " + inLavorazioneFolderName);
            }

            Dispatch.call(mailToShow, "Display", false);
            logger.debug("entryId della mail mostrata: {}", Dispatch.get(mailToShow, "EntryID").getString());

        } finally {
            releaseOutlookConnection();
        }

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