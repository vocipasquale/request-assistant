package it.requestassistant.application.port.in;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;

import java.util.List;

public interface BatchPort {

    List<Message> getMessagesToProcess();

    void moveMessageInProgress(Message message) throws Exception;

    boolean processMessage(Message message);

    List<Request> getRequestsToProcess();

    void processRequest(Request request);
}
