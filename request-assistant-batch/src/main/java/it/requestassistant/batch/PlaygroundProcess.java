package it.requestassistant.batch;

import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Component
public class PlaygroundProcess {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BatchPort batchPort;

    public PlaygroundProcess(BatchPort batchPort) {
        this.batchPort = batchPort;
    }


    public void runOnce() {
        logger.info("=================================");
        logger.info(" Request Assistant - Playground");
        logger.info("=================================");


        //recupero le nuove mail/messages nella cartella (in arrivo)
        List<Message> messages = batchPort.getMessagesToProcess();

        if (Objects.isNull(messages) || messages.isEmpty()) {
            logger.info("Nessuna email");
            return;
        }

        logger.info("Trovate " + messages.size() + " mail!");

        //ricerca di una probabile pratica in cui inserire ogni messaggio
        for (Message message : messages) {
            logger.debug("mailID: " + message.entryId());

            //ricerco delle request inerenti al message
            Request request = batchPort.searchRequestForMessage(message);

            //persisto sul DB il message: da fare dopo batchPort.searchRequestForMessage(message);
            batchPort.persistMessage(message);

            //sottopongo il risultato della ricerca all'AI
            List<DecisionOption> options = batchPort.generateProposal(message, request);

            //richiedo la creazione della "decisione" che dovrà prendere l'operatore
            batchPort.generateDecision(message, request, options);

            //faccio spostare la mail relativa al message corrente in modo che non venga analizzata ancora
            try {
                batchPort.moveMessageInProgress(message);
            } catch (Exception e) {
                logger.error("Errore spostamento messaggio {}", message.entryId(), e);
            }
        }
    }
}