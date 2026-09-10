package it.requestassistant.adapters.ai;

import it.requestassistant.application.port.out.AiEnginePort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
public class AiEngineAdapter implements AiEnginePort {
    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PersistenceDaoPort persistenceDaoPort;

    public AiEngineAdapter(PersistenceDaoPort persistenceDaoPort) {
        this.persistenceDaoPort = persistenceDaoPort;
    }


    @Override
    public PendingDecision analyzeMessage(Message message, Request candidate) {
        logger.info("AI: analisi messaggio...");
        List<DecisionOption> options = generateDecisionOptions(message, candidate);

        logger.debug("costruisco pending decision...");
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

    @Override
    public PendingDecision analyzeRequest(Request request) {
        logger.info("AI: analisi richiesta...");
        List<DecisionOption> options = generateDecisionOptions(request);

        logger.debug("costruisco pending decision...");
        PendingDecision pendingDecision = new PendingDecision(
                0,
                LocalDateTime.now(),
                PendingDecision.Type.REQUEST_ANALYSIS,
                "Inviare sollecito al cliente",
                options,
                null,
                request);

        return pendingDecision;
    }


    // METODO CHE SIMULA IL LAVORO CHE ESEGUIRà IL MOTORE AI: GENERA LE DECISIONI CHE DOVRà PRENDERE L'OPERATORE
    private List<DecisionOption> generateDecisionOptions(Message message, Request candidate) {
        List<DecisionOption> options = new ArrayList<>();

        if (Objects.isNull(candidate)) {
            //la request candidata è null: non è stata trovata per conversatioId o per tk
            logger.debug("AI: No request found in the database for message with id: " + message.getEntryId());

            List<String> steps = new ArrayList<>();
            steps.add("Crea una nuova Request e persistila nel database");
            steps.add("Titolo proposto per la nuova Request: Reset pwd dominio esterni");
            steps.add("User proposto per la nuova Request: u00000 Mario Rossi");
            steps.add("Status per la nuova Request: IN_PROGRES");
            //...
            steps.add("Associa Message Id relativo alla mail corrente alla nuova request");
            steps.add("Creare nuova Mail con oggetto: Reset pwd dominio esterni per Mario Rossi u00000");
            steps.add("Contenuto della nuova Mail: Si richiede reset pwd dominio esterni per Mario Rossi u00000.");
            //.... ALLEGATI ????   To, Cc, ...
            steps.add("Invia la mail");
            steps.add("Sposta la mail inviata in in progress folder...");


            Action action = new Action(Action.Title.NUOVA_RICHIESTA, steps);

            options.add(new DecisionOption(0L, action, 90.00, "Nessuna richiesta trovata nel database attinente al messaggio."));
        } else {
            //la request candidata non è null: è stata trovata per conversatioId o per tk
            logger.debug("AI: Request found in the database with id: " + candidate.getId() + " for message with id: " + message.getEntryId());

            List<String> steps = new ArrayList<>();
            steps.add("Aggiungi (update) il Message alla richiesta id:" + candidate.getId());
            steps.add("Sposta la mail corrente in in progress folder...");

            Action action = new Action(Action.Title.MODIFICA_RICHIESTA, steps);
            options.add(new DecisionOption(0L, action, 10.00, "Trovata richiesta id " + candidate.getId() + " attinente al messaggio."));
        }

        return options;
    }


    // METODO CHE SIMULA IL LAVORO CHE ESEGUIRà IL MOTORE AI: GENERA LE DECISIONI CHE DOVRà PRENDERE L'OPERATORE
    private List<DecisionOption> generateDecisionOptions(Request request) {
        List<DecisionOption> options = new ArrayList<>();

        if (Objects.isNull(request)) {
            logger.warn("AI: nessuna richiesta da analizzare!");
            return options;
        }

        List<String> steps = new ArrayList<>();
        steps.add("Invia una mail di sollecito al cliente per tk {}:" + request.getItems().stream().findFirst().map(RequestItem::getTicket).orElse("N/A"));

        Action action = new Action(Action.Title.INVIA_MAIL, steps);
        options.add(new DecisionOption(0L, action, 90.00,
                String.format("Nessuna risposta da parte del cliente alla mail (id {}) per richiesta {}",
                        request.getMessages().stream().findFirst().map(Message::getEntryId).orElse("N/A"),
                        request.getItems().stream().findFirst().map(RequestItem::getDettaglio).orElse("N/A"))));

        return options;
    }
}
