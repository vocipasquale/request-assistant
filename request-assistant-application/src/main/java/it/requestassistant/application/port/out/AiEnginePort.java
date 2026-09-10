package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;
import it.requestassistant.domain.model.Request;

public interface AiEnginePort {
    PendingDecision analyzeMessage(Message message, Request candidate);
    PendingDecision analyzeRequest(Request request);
}
