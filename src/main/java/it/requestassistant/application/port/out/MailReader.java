package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.MailMessage;

public interface MailReader {

    MailMessage getLastMessage(String folderName);

}
