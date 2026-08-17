package it.requestassistant.poc;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutlookConnectionTest {
    public static Logger logger = LoggerFactory.getLogger(OutlookConnectionTest.class);

    public static void main(String[] args) {

        logger.info("Avvio test Outlook");

        ActiveXComponent outlook =
                new ActiveXComponent("Outlook.Application");

        Dispatch namespace =
                outlook.getProperty("Session").toDispatch();

        Dispatch richiesteFolder =
                findFolder(
                        Dispatch.get(namespace, "Folders").toDispatch(),
                        "RichiesteAbilitazioni"
                );

        if (richiesteFolder == null) {
            logger.info("Cartella non trovata");
            return;
        }

        Dispatch items =
                Dispatch.get(richiesteFolder, "Items").toDispatch();

        int count =
                Dispatch.get(items, "Count").getInt();

        logger.info("Email presenti: " + count);

        if (count == 0) {
            logger.info("Nessuna email");
            return;
        }

        Dispatch mail =
                Dispatch.call(
                        items,
                        "Item",
                        new Variant(count)
                ).toDispatch();

        String subject =
                Dispatch.get(mail, "Subject").getString();

        String sender =
                Dispatch.get(mail, "SenderName").getString();

        String received =
                Dispatch.get(mail, "ReceivedTime").toString();

        logger.info("EMAIL TROVATA");
        logger.info("--------------------");
        logger.info("Mittente : " + sender);
        logger.info("Oggetto  : " + subject);
        logger.info("Ricevuta : " + received);

        outlook.safeRelease();

        logger.info("--------------------");
        logger.info("Fine test");
    }


    private static Dispatch findFolder(Dispatch parent, String targetName) {

        String name =
                Dispatch.get(parent, "Name").getString();

        if (targetName.equals(name)) {
            return parent;
        }

        Dispatch folders =
                Dispatch.get(parent, "Folders").toDispatch();

        int count =
                Dispatch.get(folders, "Count").getInt();

        for (int i = 1; i <= count; i++) {

            Dispatch child =
                    Dispatch.call(
                            folders,
                            "Item",
                            new Variant(i)
                    ).toDispatch();

            Dispatch result =
                    findFolder(child, targetName);

            if (result != null) {
                return result;
            }
        }

        return null;
    }
}