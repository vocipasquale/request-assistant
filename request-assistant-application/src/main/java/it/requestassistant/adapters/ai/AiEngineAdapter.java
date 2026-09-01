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


    @Override
    @Transactional
    public void actionsPerform(PendingDecision pendingDecision, DecisionOption decisionOption) throws Exception {
        logger.info("Performing actions for decision option: " + decisionOption.id());
        Action action = decisionOption.action();
        List<String> steps = action.steps();

        //per ora lascio questo controllo, poi capirò se toglierlo...
        if(Objects.isNull(pendingDecision.getMessage())){
            logger.error("Pending decision {} has no associated message. Cannot perform actions!", pendingDecision.getId());
            throw new Exception("Pending decision has no associated message. Cannot perform actions!");
        }

        for (String step : steps) {
            logger.info("Executing step: " + step);
            //....


        }

        // Simulo due azioni:
        // 1. creo una nuova request e le associo il messaggio della pending decision
        // 2. associo il messaggio della pending decision alla rquest passata e già presente in DB

        if(Objects.isNull(pendingDecision.getRequest())){
            logger.info("Pending decision {} has no associated request. Creating a new request and associating the message.", pendingDecision.getId());
            // Simulate creating a new request and associating the message
            Request newRequest = new Request();
            newRequest.setId(0L); // Simulate generated ID
            newRequest.setTitle(decisionOption.action().title());
            newRequest.setStatus(Request.Status.IN_PROGRESS);
            newRequest.setCreateAt(LocalDateTime.now());
            newRequest.setUpdateAt(newRequest.getCreateAt());
            newRequest.setMessages(List.of(pendingDecision.getMessage()));
            newRequest.setItems(List.of(getRequestItem("step")));
            newRequest.setNote("test manuale");
            newRequest.setUser(getUser(pendingDecision, decisionOption));
            persistenceDaoPort.insertRequest(newRequest);
        }else {
            logger.info("Pending decision {} has an associated request with ID {}. Associating the message to this request.", pendingDecision.getId(), pendingDecision.getRequest().getId());
            // Simulate associating the message to the existing request
            Request existingRequest = pendingDecision.getRequest();
            List<Message> updatedMessages = new ArrayList<>(existingRequest.getMessages());
            updatedMessages.add(pendingDecision.getMessage());
            existingRequest.setMessages(updatedMessages);
            existingRequest.setUpdateAt(LocalDateTime.now());
            persistenceDaoPort.updateRequest(existingRequest);
        }

        logger.info("AI: completed actions for decision option: " + decisionOption.id());
    }

    /**
     * genera un RequestItem generico, in futuro dovrà essere
     * generato in base alle informazioni degli steps
     *
     */
    private RequestItem getRequestItem(String step) {
        RequestItem result = new RequestItem();
        result.setType(RequestItem.Type.DOMINIO_APN_VPN);
        result.setCreateAt(LocalDateTime.now());
        result.setUpdateAt(result.getCreateAt());
        result.setDettaglio("Dettaglio generico");
        result.setAmbiente(List.of(RequestItem.Ambiente.SVILUPPO, RequestItem.Ambiente.COLLAUDO));
        result.setNota("test manuale");
        result.setStatus(RequestItem.Status.DA_RICHIEDERE);

        return result;
    }

    /**
     * Stabilisce se si tratta di user già censito nel db e lo restituisce altrimenti
     * lo crea, lo salva nel db e poi lo restituisce.
     *
     * @param pendingDecision
     * @param decisionOption
     * @return
     */
    private User getUser(PendingDecision pendingDecision, DecisionOption decisionOption) {
        Random random = new Random();
        String matricola = String.format("%06d", random.nextInt(1_000_000));

        return new User("Rossi", "Mario", "U"+matricola);
    }

    // METODO CHE SIMULA IL LAVORO CHE ESEGUIRà IL MOTORE AI: GENERA LE DECISIONI CHE DOVRà PRENDERE L'OPERATORE
    private List<DecisionOption> generateDecisionOptions(Message message, Request candidate) {
        List<DecisionOption> options = new ArrayList<>();

        if (Objects.isNull(candidate)) {
            //la request candidata è null: non è stata trovata per conversatioId o per tk
            logger.debug("AI: No request found in the database for message with id: " + message.entryId());

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


            Action action = new Action("Crea una nuova richiesta", steps);

            options.add(new DecisionOption(0L, action, 90.00, "Nessuna richiesta trovata nel database attinente al messaggio."));
        } else {
            //la request candidata non è null: è stata trovata per conversatioId o per tk
            logger.debug("AI: Request found in the database with id: " + candidate.getId() + " for message with id: " + message.entryId());

            List<String> steps = new ArrayList<>();
            steps.add("Aggiungi (update) il Message alla richiesta id:" + candidate.getId());
            steps.add("Sposta la mail corrente in in progress folder...");

            Action action = new Action("Crea una nuova richiesta", steps);
            options.add(new DecisionOption(0L, action, 10.00, "Trovata richiesta id " + candidate.getId() + " attinente al messaggio."));
        }

        return options;
    }
}
