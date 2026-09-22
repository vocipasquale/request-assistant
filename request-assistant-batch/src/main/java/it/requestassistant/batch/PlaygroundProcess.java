package it.requestassistant.batch;

import it.requestassistant.application.port.in.BatchPort;
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


    public void runOnce() throws Exception {
        logger.info("=================================");
        logger.info(" Assistente Richieste - Playground");
        logger.info("=================================");

        processMessages();
        processRequests();

    }

    private void processRequests() {
        //recuper le richieste da sollecitare o da chiudere...
        List<Request> requests = batchPort.getRequestsToProcess();

        if (Objects.isNull(requests) || requests.isEmpty()) {
            logger.info("Nessuna richiesta pendente");
            return;
        }

        logger.info("Trovate " + requests.size() + " richieste da processare!");
        requests.stream().forEach(request ->{
            logger.debug("Richiesta id {} in processamento...", request.getId());
            batchPort.processRequest(request);
        });
    }

    private void processMessages() throws Exception {
        //recupero le nuove mail/messages nella cartella (in arrivo)
        List<Message> messages = batchPort.getMessagesToProcess();

        if (Objects.isNull(messages) || messages.isEmpty()) {
            logger.info("Nessuna email");
            return;
        }
        logger.info("Trovate " + messages.size() + " mail!");

        for (Message message : messages) {
            logger.debug("Elaboro il messaggio mailID: " + message.getEntryId());

            if (batchPort.processMessage(message)) {//processamento messaggio
                logger.debug("Messaggio elaborato correttamente, mailID: " + message.getEntryId());
                moveMessageInProgress(message);
            } else {
                logger.debug("Elaborazione del messaggio fallita, mailID: " + message.getEntryId());
            }
        }
    }

    private void moveMessageInProgress(Message message) throws Exception {
            logger.debug("Sposto la mail in lavorazione, mailID: " + message.getEntryId());
            batchPort.moveMessageInProgress(message);
    }
}