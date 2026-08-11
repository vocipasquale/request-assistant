package it.requestassistant.poc;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class OutlookConnectionTest {

    public static void main(String[] args) {

        System.out.println("Avvio test Outlook");

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
            System.out.println("Cartella non trovata");
            return;
        }

        Dispatch items =
                Dispatch.get(richiesteFolder, "Items").toDispatch();

        int count =
                Dispatch.get(items, "Count").getInt();

        System.out.println("Email presenti: " + count);

        if (count == 0) {
            System.out.println("Nessuna email");
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

        System.out.println();
        System.out.println("EMAIL TROVATA");
        System.out.println("--------------------");
        System.out.println("Mittente : " + sender);
        System.out.println("Oggetto  : " + subject);
        System.out.println("Ricevuta : " + received);

        outlook.safeRelease();

        System.out.println("--------------------");
        System.out.println("Fine test");
    }


    private static Dispatch findFolder(Dispatch folders, String targetName) {

        int count =
                Dispatch.get(folders, "Count").getInt();

        for (int i = 1; i <= count; i++) {

            Dispatch folder =
                    Dispatch.call(
                            folders,
                            "Item",
                            new Variant(i)
                    ).toDispatch();

            String name =
                    Dispatch.get(folder, "Name").getString();

            if (targetName.equals(name)) {
                return folder;
            }

            Dispatch subFolders =
                    Dispatch.get(folder, "Folders").toDispatch();

            Dispatch result =
                    findFolder(subFolders, targetName);

            if (result != null) {
                return result;
            }
        }

        return null;
    }
}