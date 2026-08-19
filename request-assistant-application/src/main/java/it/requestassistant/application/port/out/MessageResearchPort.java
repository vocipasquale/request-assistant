package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.Message;

import java.util.List;

public interface MessageResearchPort {

    List<Message> findMessagesToProcess();
    void moveMessageInProgress(Message message) throws Exception;
    void moveMessageInDone(Message message) throws Exception;
}
