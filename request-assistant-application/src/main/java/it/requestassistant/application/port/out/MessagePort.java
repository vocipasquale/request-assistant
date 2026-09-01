package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.Message;

import java.util.List;

public interface MessagePort {

    List<Message> findMessagesToProcess();
    Message moveMessageInProgress(Message message) throws Exception;
    Message moveMessageInDone(Message message) throws Exception;

    Message moveMessageInDiscarded(Message message) throws Exception;

    void displayMessage(Message message);
}
