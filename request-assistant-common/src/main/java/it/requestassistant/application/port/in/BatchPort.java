package it.requestassistant.application.port.in;

import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;

import java.util.List;

public interface BatchPort {

    List<Message> getMessagesToProcess();

    Request searchRequestForMessage(Message message);

    List<DecisionOption> generateProposal(Message message, Request request);

    void generateDecision(Message message, Request request, List<DecisionOption> options);

    void moveMessageInProgress(Message message) throws Exception;

    void persistMessage(Message message);
}
