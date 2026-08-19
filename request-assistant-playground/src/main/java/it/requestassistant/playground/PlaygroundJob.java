package it.requestassistant.playground;

import it.requestassistant.playground.client.PlaygroundClient;
import it.requestassistant.playground.dto.MessageDto;
import it.requestassistant.playground.dto.MoveInProgressRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Component
public class PlaygroundJob {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PlaygroundClient client;

    public PlaygroundJob(PlaygroundClient client) {
        this.client = client;
    }


    @Scheduled(fixedDelay = 30_000)
    public void execute() {

        logger.info("=================================");
        logger.info(" Request Assistant - Playground");
        logger.info("=================================");


        //recupero le nuove mail/messages nella cartella (in arrivo)
        List<MessageDto> messages = client.getMessagesToProcess();

        if (Objects.isNull(messages) || messages.isEmpty()) {
            logger.info("Nessuna email");
            return;
        }

        logger.info("Trovate " + messages.size() + " mail!");

        //ricerca di una probabile pratica in cui inserire ogni messaggio
        for (MessageDto message : messages) {
            logger.debug("mailID: " + message);
            //Request request = playgroundRequestSearch.search(message);

            //sottopongo il risultato della ricerca all'AI
            //List<DecisionOption> options = playgroundAiAnalyzer.analyzeMessage(message, request);

            //creo e persisto la "proposta" per una verifica dell'operatore tramite Dashboard
            // PendingDecision proposta = new PendingDecision()
            // pendingDecisionRepository.save()

            //sposto la mail relativa al message corrente in modo che non venga analizzata ancora
            client.moveMessageInProgress(new MoveInProgressRequest());

        }


    }
}