package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.PendingDecision;

public interface ActionPerformerPort {
    void perform(PendingDecision pendingDecision, DecisionOption decisionOption) throws Exception;
}
