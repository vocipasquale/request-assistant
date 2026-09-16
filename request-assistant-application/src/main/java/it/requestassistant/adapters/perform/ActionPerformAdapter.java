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
        logger.debug("Esecuzione azioni decision id {}: " + decisionOption.getId());
        Action action = decisionOption.getAction();

        logger.info("Azione {}",action.getTitle().getTitle());



        //per ora lascio questo controllo, poi capirò se toglierlo...
//        if (Objects.isNull(pendingDecision.getMessage())) {
//            logger.error("La pending decision {} non ha un messaggio associato. Impossibile eseguire le azioni!", pendingDecision.getId());
//            throw new Exception("La pending decision non ha un messaggio associato. Impossibile eseguire le azioni!");
//        }



        // Simulo due azioni:
        // 1. creo una nuova request e le associo il messaggio della pending decision
        // 2. associo il messaggio della pending decision alla rquest passata e già presente in DB

        if (Objects.isNull(pendingDecision.getRequest())) {
            logger.info("La pending decision {} non ha una request associata. Creo una nuova request e associo il messaggio.", pendingDecision.getId());
            // Simulo la creazione di una nuova request e l'associazione del messaggio
            Request newRequest = new Request();
            newRequest.setId(0L); // Simulo l'id generato
            newRequest.setTitle(decisionOption.getAction().getTitle().getTitle());
            newRequest.setStatus(Request.Status.IN_PROGRESS);
            newRequest.setCreateAt(LocalDateTime.now());
            newRequest.setUpdateAt(newRequest.getCreateAt());
            newRequest.setMessages(List.of(pendingDecision.getMessage()));
            newRequest.setItems(List.of(getRequestItem("passo")));
            newRequest.setNote("test manuale");
            newRequest.setUser(getUser(pendingDecision, decisionOption));
            persistenceDaoPort.insertRequest(newRequest);
        } else {
            logger.info("La pending decision {} ha una request associata con ID {}. Associo il messaggio a questa request.", pendingDecision.getId(), pendingDecision.getRequest().getId());
            // Simulo l'associazione del messaggio alla request esistente
            Request existingRequest = pendingDecision.getRequest();
            List<Message> updatedMessages = new ArrayList<>(existingRequest.getMessages());
            updatedMessages.add(pendingDecision.getMessage());
            existingRequest.setMessages(updatedMessages);
            existingRequest.setUpdateAt(LocalDateTime.now());
            persistenceDaoPort.updateRequest(existingRequest);
        }

        logger.info("AI: azioni completate per l'opzione decisionale: " + decisionOption.getId());
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
