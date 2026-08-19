package it.requestassistant.adapters.ai;

import it.requestassistant.application.port.out.AiAnalyzer;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiAnalyzerService implements AiAnalyzer {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<DecisionOption> analyzeMessage(Message message, Request candidate) {
        logger.info("AI: analyzing Message...");
        return List.of();
    }
}
