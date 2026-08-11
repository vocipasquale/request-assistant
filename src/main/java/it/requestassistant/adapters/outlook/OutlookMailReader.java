package it.requestassistant.adapters.outlook;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import it.requestassistant.application.port.out.MailReader;
import it.requestassistant.domain.model.MailMessage;

import static javax.management.remote.JMXConnectorFactory.connect;

/// /   -Djava.library.path=lib

public class OutlookMailReader implements MailReader {

    @Override
    public MailMessage getLastMessage(String folderName) {
        ActiveXComponent outlook = connect();

        try {

            // qui, per il momento, sposteremo il codice
            // già funzionante del PoC

            throw new UnsupportedOperationException("Da implementare");

        } finally {
            outlook.safeRelease();
        }
    }

    private ActiveXComponent connect() {
        return new ActiveXComponent("Outlook.Application");
    }

    private Dispatch findFolder(Dispatch folders, String folderName) {
        throw new UnsupportedOperationException("Da implementare");
    }

    private MailMessage toMailMessage(Dispatch mailItem) {
        throw new UnsupportedOperationException("Da implementare");
    }

}