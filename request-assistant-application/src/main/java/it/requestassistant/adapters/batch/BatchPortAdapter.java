package it.requestassistant.adapters.batch;

import it.requestassistant.adapters.persistence.dao.RequestResearch;
import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.application.port.out.AiAnalyzerPort;
import it.requestassistant.application.port.out.MessageResearchPort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.port.out.RequestResearchPort;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchPortAdapter implements BatchPort  {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageResearchPort messagePort;

    @Autowired
    private RequestResearchPort requestResearchPort;

    @Autowired
    private PersistenceDaoPort persistenceDaoPort;

    @Autowired
    private AiAnalyzerPort aiAnalyzerPort;

    @Override
    public List<Message> getMessagesToProcess() {
        return messagePort.findMessagesToProcess();
    }

    @Override
    public Request searchRequestForMessage(Message message) {
        return requestResearchPort.searchByMessage(message);
    }

    @Override
    public List<DecisionOption> generateProposal(Message message, Request request) {
        return aiAnalyzerPort.analyzeMessage(message, request);
    }

    @Override
    public void generateDecision(Message message, Request request, List<DecisionOption> options) {
        logger.debug("generateDecision...");
    }

    @Override
    public void moveMessageInProgress(Message message) throws Exception {
        messagePort.moveMessageInProgress(message);
    }

    @Override
    public void persistMessage(Message message) {
        persistenceDaoPort.insertMessage(message);
    }
}
