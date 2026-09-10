package it.requestassistant.adapters.perform;

import it.requestassistant.application.port.out.ActionPerformerPort;
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
public class ActionPerformAdapter implements ActionPerformerPort {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PersistenceDaoPort persistenceDaoPort;

    public ActionPerformAdapter(PersistenceDaoPort persistenceDaoPort) {
        this.persistenceDaoPort = persistenceDaoPort;
    }

    @Override
    @Transactional
    public void perform(PendingDecision pendingDecision, DecisionOption decisionOption) throws Exception {
        logger.info("Performing actions for decision option: " + decisionOption.getId());
        Action action = decisionOption.getAction();
        List<String> steps = action.getSteps();

        //per ora lascio questo controllo, poi capirò se toglierlo...
        if (Objects.isNull(pendingDecision.getMessage())) {
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

        if (Objects.isNull(pendingDecision.getRequest())) {
            logger.info("Pending decision {} has no associated request. Creating a new request and associating the message.", pendingDecision.getId());
            // Simulate creating a new request and associating the message
            Request newRequest = new Request();
            newRequest.setId(0L); // Simulate generated ID
            newRequest.setTitle(decisionOption.getAction().getTitle().getTitle());
            newRequest.setStatus(Request.Status.IN_PROGRESS);
            newRequest.setCreateAt(LocalDateTime.now());
            newRequest.setUpdateAt(newRequest.getCreateAt());
            newRequest.setMessages(List.of(pendingDecision.getMessage()));
            newRequest.setItems(List.of(getRequestItem("step")));
            newRequest.setNote("test manuale");
            newRequest.setUser(getUser(pendingDecision, decisionOption));
            persistenceDaoPort.insertRequest(newRequest);
        } else {
            logger.info("Pending decision {} has an associated request with ID {}. Associating the message to this request.", pendingDecision.getId(), pendingDecision.getRequest().getId());
            // Simulate associating the message to the existing request
            Request existingRequest = pendingDecision.getRequest();
            List<Message> updatedMessages = new ArrayList<>(existingRequest.getMessages());
            updatedMessages.add(pendingDecision.getMessage());
            existingRequest.setMessages(updatedMessages);
            existingRequest.setUpdateAt(LocalDateTime.now());
            persistenceDaoPort.updateRequest(existingRequest);
        }

        logger.info("AI: completed actions for decision option: " + decisionOption.getId());
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

        return new User(-1L, "Rossi", "Mario", "U" + matricola);
    }
}
