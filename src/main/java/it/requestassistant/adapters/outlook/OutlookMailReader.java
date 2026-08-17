package it.requestassistant.adapters.outlook;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import it.requestassistant.domain.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class OutlookMailReader implements MailReader {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<Message> getMessages(String rootFolderName, String folderName) {
        List<Message> messages = new ArrayList<>();

        logger.info("Avvio connessione Outlook");
        ActiveXComponent outlook =
                new ActiveXComponent("Outlook.Application");

        try{
            Dispatch namespace =
                    outlook.getProperty("Session").toDispatch();
            logger.info("Prelevato session");

            Dispatch rootFolder = findFolder(namespace, rootFolderName);

            if(Objects.isNull(rootFolder)){
               return null;
            }
            logger.info("Cartella "+rootFolderName+" trovata");

            ///////////////////////////////////////////////////////////////////////////////////
            //la cartella root è stata trovata, rieseguo la ricerca della cartella delle richieste
            //partendo da qui...
            Dispatch richiesteFolder = findFolder(rootFolder, folderName);
            if(Objects.isNull(richiesteFolder)){
                return null;
            }
            ////////////////////////////////////////////////////////////////////////////////

            //cartella trovata!
            logger.info("Cartella "+folderName+" trovata");

            //recupero la mail dalla cartella delle richieste
            Dispatch items =
                    Dispatch.get(richiesteFolder, "Items").toDispatch();
            int count = Dispatch.get(items, "Count").getInt();
            logger.info("Trovate "+count+ " email in "+folderName);

            for(int i=1; i<=count; i++){
               logger.info("conversione messaggio "+ i);
                messages.add(mailToMessage(Dispatch.call(items, "Item", new Variant(i)).toDispatch()));
            }

            return messages;

        } finally {
            outlook.safeRelease();
        }

    }

    @Override
    public Message mailToMessage(Dispatch mail) {

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

    private boolean hasAttachments(Dispatch mail){
        Dispatch attachments =
                Dispatch.get(mail, "Attachments").toDispatch();

        return Dispatch.get(attachments, "Count").getInt() >0 ? true : false;
    }

    public Dispatch findFolder(Dispatch startFolder, String targetFolderName){
        boolean trovata = false;
        Dispatch foundFolder = null;

        Dispatch folders =
                Dispatch.get(startFolder, "Folders").toDispatch();
        logger.info("Prelevati folders");

        int count = Dispatch.get(folders, "Count").getInt();
        logger.info("Folder trovati: "+count);

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