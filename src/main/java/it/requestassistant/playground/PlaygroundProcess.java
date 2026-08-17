package it.requestassistant.playground;

import it.requestassistant.adapters.outlook.OutlookMailReader;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
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
    
    private static final String ROOT_FOLDER = "Pa.Voci@almaviva.it";
    private static final String RICHIESTE_ABILITAZIONI = "RichiesteAbilitazioni";

    @Autowired
    private PlaygroundRequestSearch playgroundRequestSearch;

    @Autowired
    private PlaygroundAiAnalyzer playgroundAiAnalyzer;

//    @Autowired
//    private PendingDecisionRepository pendingDecisionRepository;


    @Override
    public void run(String... args) {


        logger.info("=================================");
        logger.info(" Request Assistant - Playground");
        logger.info("=================================");

        OutlookMailReader outlookMailReader = new OutlookMailReader();
        List<Message> messages = outlookMailReader.getMessages(ROOT_FOLDER, RICHIESTE_ABILITAZIONI);


        if (Objects.isNull(messages) || messages.isEmpty()) {
            logger.info("Nessuna email");
            return;
        }

        logger.info("Trovate "+messages.size()+" mail!");

        messages.forEach(
                message -> {
                    //ricerca di una probabile pratica in cui inserire il messaggio
                    Request request = playgroundRequestSearch.search(message);

                    //sottopongo il risultato della ricerca all'AI
                    List<DecisionOption> options = playgroundAiAnalyzer.analyzeMessage(message, request);

                    //creo e persisto la "proposta" per una verifica dell'operatore tramite Dashboard
                    // PendingDecision proposta = new PendingDecision()
                    // pendingDecisionRepository.save()

                    //sposto la mail relativa al message corrente in modo che non venga analizzata ancora

                }
        );

    }
}