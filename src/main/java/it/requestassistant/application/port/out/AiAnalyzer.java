package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;

import java.util.List;

public interface AiAnalyzer {
    List<DecisionOption> analyzeMessage(Message message, Request candidate);
}
