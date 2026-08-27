package it.requestassistant.application.port.out;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;
import it.requestassistant.domain.model.Request;

import java.util.List;

public interface AiAnalyzerPort {
    PendingDecision analyzeMessage(Message message, Request candidate);
}
