package it.requestassistant.adapters.ai;

import it.requestassistant.application.port.out.AiAnalyzerPort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;
import it.requestassistant.domain.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class AiAnalyzer implements AiAnalyzerPort {
    public Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public PendingDecision analyzeMessage(Message message, Request candidate) {
        logger.info("AI: analyzing Message...");
        List<DecisionOption> options = generateDecisionOptions(message, candidate);

        logger.debug("building pending decision...");
        PendingDecision pendingDecision = new PendingDecision(
                0,
                LocalDateTime.now(),
                PendingDecision.Type.MESSAGE_CLASSIFICATION,
                "Gestire messaggio in arrivo",
                options,
                message,
                candidate);

        return pendingDecision;
    }

    // METODO CHE SIMULA IL LAVORO CHE ESEGUIRà IL MOTORE AI: GENERA LE DECISIONI CHE DOVRà PRENDERE L'OPERATORE
    private List<DecisionOption> generateDecisionOptions(Message message, Request candidate) {
        List<DecisionOption> options = new ArrayList<>();

        if (Objects.isNull(candidate)) {
            //la request candidata è null: non è stata trovata per conversatioId o per tk
            logger.debug("AI: No request found in the database for message with id: " + message.entryId());
            options.add(new DecisionOption(0L, "Crea una nuova richiesta", 90.00, "Nessuna richiesta trovata nel database attinente al messaggio."));
        } else {
            //la request candidata non è null: è stata trovata per conversatioId o per tk
            logger.debug("AI: Request found in the database with id: " + candidate.getId() + " for message with id: " + message.entryId());
            options.add(new DecisionOption(0L, "Aggiungi il messaggio alla richiesta id:" + candidate.getId(), 10.00, "Trovata richiesta id " + candidate.getId() + " attinente al messaggio."));
        }

        return options;
    }
}
