package it.requestassistant.client.playground;

import it.requestassistant.adapters.ai.AiAnalyzerService;
import it.requestassistant.adapters.outlook.MessageService;
import it.requestassistant.domain.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Component
public class PlaygroundProcess implements CommandLineRunner {
    public Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private AiAnalyzerService playgroundAiAnalyzer;

    @Autowired
    private MessageService messageService;

//    @Autowired
//    private PendingDecisionRepository pendingDecisionRepository;


    @Override
    public void run(String... args) throws Exception {
        logger.info("=================================");
        logger.info(" Request Assistant - Playground");
        logger.info("=================================");


        //recupero le nuove mail/messages nella cartella (in arrivo)
        List<Message> messages = messageService.findMessagesToProcess();

        if (Objects.isNull(messages) || messages.isEmpty()) {
            logger.info("Nessuna email");
            return;
        }

        logger.info("Trovate " + messages.size() + " mail!");

        //ricerca di una probabile pratica in cui inserire ogni messaggio
        for (Message message : messages) {
            logger.debug("mailID: " + message.entryId());
            //Request request = playgroundRequestSearch.search(message);

            //sottopongo il risultato della ricerca all'AI
            //List<DecisionOption> options = playgroundAiAnalyzer.analyzeMessage(message, request);

            //creo e persisto la "proposta" per una verifica dell'operatore tramite Dashboard
            // PendingDecision proposta = new PendingDecision()
            // pendingDecisionRepository.save()

            //sposto la mail relativa al message corrente in modo che non venga analizzata ancora
            messageService.moveMessageInProgress(message);

        }


    }
}