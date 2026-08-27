package it.requestassistant.batch;

import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.domain.model.Message;
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
        logger.info(" Request Assistant - Playground");
        logger.info("=================================");


        //recupero le nuove mail/messages nella cartella (in arrivo)
        List<Message> messages = batchPort.getMessagesToProcess();

        if (Objects.isNull(messages) || messages.isEmpty()) {
            logger.info("Nessuna email");
            return;
        }
        logger.info("Trovate " + messages.size() + " mail!");

        for (Message message : messages) {
            logger.debug("Process message mailID: " + message.entryId());

            if (batchPort.processMessage(message)) {//processamento messaggio
                logger.debug("Message processed successfully mailID: " + message.entryId());
                moveMessageInProgress(message);
            } else {
                logger.debug("Message processing failed mailID: " + message.entryId());
            }
        }
    }

    private void moveMessageInProgress(Message message) throws Exception {
            logger.debug("Move mail in to InProgress, mailID: " + message.entryId());
            batchPort.moveMessageInProgress(message);
    }
}