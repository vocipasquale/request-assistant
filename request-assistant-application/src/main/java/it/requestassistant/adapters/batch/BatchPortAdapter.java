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
        logger.debug("Aggiorno entryId, precedente: {}", message.getEntryId());
        logger.debug("Aggiorno entryId, nuovo: {}", movedMessage.getEntryId());
        persistenceDaoPort.refreshEntryIdMessage(message.getEntryId(), movedMessage.getEntryId());
    }

    @Override
    public boolean processMessage(Message message) {
        logger.debug("Ricerca request per entryId del messaggio {}", message.getEntryId());
        Request request = requestResearchPort.searchByMessage(message);

        //sottopongo il risultato della ricerca all'AI
        //AI si preoccupa di creare le proposte/decisioni che dovrà prendere l'operatore
        logger.debug("Analisi del risultato tramite motore AI...");
        PendingDecision pendingDecision = aiAnalyzerPort.analyzeMessage(message, request);

        //persisto su database
        logger.debug("Persistenza completa su database.");
        persistToDb(pendingDecision);

        return true;
    }

    @Override
    public List<Request> getRequestsToProcess() {
        return persistenceDaoPort.findRequestsToProcess();
    }

    @Override
    public void processRequest(Request request) {
        //sottopongo la request all'AI
        //AI si preoccupa di creare le proposte/decisioni che dovrà prendere l'operatore
        logger.debug("Analisi AI in corso per request id {}", request.getId());
        PendingDecision pendingDecision = aiAnalyzerPort.analyzeRequest(request);

        logger.debug("Persisto la pending decision sul database...");
        persistToDb(pendingDecision);
    }

    private void persistToDb(PendingDecision pendingDecision){
        //...logging nel dao...
        persistenceDaoPort.insertPendingDecision(pendingDecision);
    }
}
