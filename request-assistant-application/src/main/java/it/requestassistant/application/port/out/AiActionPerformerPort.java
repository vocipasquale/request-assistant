package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.DataAction;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.PendingDecision;

public interface AiActionPerformerPort {
    void perform(PendingDecision pendingDecision, DecisionOption decisionOption, DataAction dataAction) throws Exception;
}
