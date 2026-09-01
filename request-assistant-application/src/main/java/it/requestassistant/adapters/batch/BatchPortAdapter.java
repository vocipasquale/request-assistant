package it.requestassistant.adapters.batch;

import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.application.port.out.AiEnginePort;
import it.requestassistant.application.port.out.MessagePort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.port.out.RequestResearchPort;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;
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
    private MessagePort messagePort;

    @Autowired
    private RequestResearchPort requestResearchPort;

    @Autowired
    private AiEnginePort aiAnalyzerPort;

    @Autowired
    private PersistenceDaoPort persistenceDaoPort;

    @Override
    public List<Message> getMessagesToProcess() {
        return messagePort.findMessagesToProcess();
    }


    @Override
    public void moveMessageInProgress(Message message) throws Exception {
        Message movedMessage = messagePort.moveMessageInProgress(message);
        logger.debug("Refresh entryId, old: {}", message.entryId());
        logger.debug("Refresh entryId, new : {}", movedMessage.entryId());
        persistenceDaoPort.refreshEntryIdMessage(message.entryId(), movedMessage.entryId());
    }

    @Override
    public boolean processMessage(Message message) {
        logger.debug("Research request for message entryID {}", message.entryId());
        Request request = requestResearchPort.searchByMessage(message);

        //sottopongo il risultato della ricerca all'AI
        //AI si preoccupa di creare le proposte/decisioni che dovrà prendere l'operatore
        logger.debug("Result analysis using an AI engine...");
        PendingDecision pendingDecision = aiAnalyzerPort.analyzeMessage(message, request);

        //persist in to DB
        logger.debug("All persist on database.");
        persistToDb(pendingDecision);

        return true;
    }

    private void persistToDb(PendingDecision pendingDecision){
        //...logging in dao...
        persistenceDaoPort.insertPendingDecision(pendingDecision);
    }
}
