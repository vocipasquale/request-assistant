package it.requestassistant.application.port.in;

import it.requestassistant.domain.model.Message;
import java.util.List;

public interface BatchPort {

    List<Message> getMessagesToProcess();

    void moveMessageInProgress(Message message) throws Exception;

    boolean processMessage(Message message);
}
