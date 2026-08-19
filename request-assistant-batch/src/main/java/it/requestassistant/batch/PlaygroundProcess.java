package it.requestassistant.batch;

//import it.requestassistant.adapters.ai.AiAnalyzerService;
//import it.requestassistant.adapters.outlook.MessageService;
import it.requestassistant.application.port.in.BatchPort;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Component
public class PlaygroundProcess implements CommandLineRunner {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private final TaskExecutor taskExecutor;
    private final BatchPort batchPort;

    public PlaygroundProcess(TaskExecutor taskExecutor, BatchPort batchPort) {
        this.taskExecutor = taskExecutor;
        this.batchPort = batchPort;
    }



    @Override
    public void run(String... args) throws Exception {
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
            Request request = batchPort.searchRequestForMessage(message);

            //sottopongo il risultato della ricerca all'AI
            List<DecisionOption> options = batchPort.generateProposal(message, request);

            //richiedo la creazione della "decisione" che dovrà prendere l'operatore
            batchPort.generateDecision(message, request, options);

            //faccio spostare la mail relativa al message corrente in modo che non venga analizzata ancora
            batchPort.moveMessageInProgress(message);
        }
    }
}