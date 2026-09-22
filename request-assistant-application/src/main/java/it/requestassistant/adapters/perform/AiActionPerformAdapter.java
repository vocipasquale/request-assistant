package it.requestassistant.adapters.perform;

import com.fasterxml.jackson.core.type.TypeReference;
import it.requestassistant.application.port.out.AiActionPerformerPort;
import it.requestassistant.application.port.out.MessagePort;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.application.service.JsonService;
import it.requestassistant.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
public class AiActionPerformAdapter implements AiActionPerformerPort {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MessagePort messagePort;
    private final PersistenceDaoPort persistenceDaoPort;

    public AiActionPerformAdapter(MessagePort messagePort, PersistenceDaoPort persistenceDaoPort) {
        this.messagePort = messagePort;
        this.persistenceDaoPort = persistenceDaoPort;
    }

    @Override
    @Transactional
    public void perform(PendingDecision pendingDecision, DecisionOption decisionOption, DataAction dataAction) throws Exception {
        logger.debug("Esecuzione azioni decision id {}: " + decisionOption.getId());
        Action action = decisionOption.getAction();


        switch (action.getTitle()) {
            case RISPONDI_A_MAIL -> {
                /**
                 * aiResponse:
                 * * "message": una bozza per ottenere le informazioni mancanti
                 * * "reuest": null
                 */
                logger.info("Eseguo azione RISPONDI_A_MAIL per la pending decision {} con decision id {}"
                        , pendingDecision.getId(), decisionOption.getId());

                if (Objects.isNull(pendingDecision.getMessage())) {
                    throw new Exception(String.format("Impossibile inviare mail per pending decision %d con decision id %d",
                            pendingDecision.getId(), decisionOption.getId()));
                }

                logger.debug("Invio mail per pending decision {} con decision id {}. Contenuto mail: {}",
                        pendingDecision.getId(), decisionOption.getId(), pendingDecision.getMessage().getBodyText());
                messagePort.replyToMessage(pendingDecision.getMessage(), dataAction.message());
                messagePort.moveMessageInDone(pendingDecision.getMessage());
                //break;
            }
//            case INOLTRA_MAIL -> {
//                logger.info("Eseguo azione INOLTRA_MAIL per la pending decision {} con id {}",
//                        pendingDecision.getId(), decisionOption.getId());
//                messagePort.forwardMessage(pendingDecision.getMessage(), dataAction.message());
//                messagePort.moveMessageInDone(pendingDecision.getMessage());
//                //break;
//            }
            case INVIA_SOLLECITO -> {
                /**
                 * direttamente dal batch...
                 */
                logger.info("Eseguo azione INVIA_SOLLECITO per la pending decision {} con id {}",
                        pendingDecision.getId(), decisionOption.getId());
                messagePort.sendMessage(pendingDecision.getMessage());
                //la mail rimane in"inviate"
            }
            case NUOVA_RICHIESTA -> {
                /**
                 * aiResponse:
                 * * "message": null
                 * * "request": una bozza con tutti i dati prelevati dal contesto di "message"
                 */
                logger.info("Eseguo azione NUOVA_RICHIESTA per la pending decision {} con id {}", pendingDecision.getId(), decisionOption.getId());

                //creo Request in stato NEW con items in stato DA_RICHIEDERE:
                dataAction.request().setStatus(Request.Status.NEW);
                dataAction.request().setCreateAt(LocalDateTime.now());
                dataAction.request().setUpdateAt(LocalDateTime.now());
                dataAction.request().getItems().forEach(item -> {
                    item.setStatus(RequestItem.Status.DA_RICHIEDERE);
                    item.setCreateAt(LocalDateTime.now());
                    item.setUpdateAt(LocalDateTime.now());
                });
                long newRequestId = persistenceDaoPort.insertRequest(dataAction.request());
                logger.info("Salvata nuova richiesta id {}:", newRequestId);
        }
            case MODIFICA_RICHIESTA -> {
                /**
                 * aiResponse:
                 * * "message": null
                 * * "request": la "request" ricevuta in input con i dati modificati (per es. RquestItem.Status)
                 */
                logger.info("Eseguo azione MODIFICA_RICHIESTA per la pending decision {} con id {}", pendingDecision.getId(), decisionOption.getId());
                /**
                 * 1. mergiare i dati di dataAction.request in pendingDecision.getRequest...
                 * 2. associare pendingDecision.getMessage a pendingDecision.getRequest
                 * 3. eseguire update di pendingDecision.getRequest
                 * 4. spostare pendingDecision.getMessage in "done"
                 */

            }
        }
    }
}
