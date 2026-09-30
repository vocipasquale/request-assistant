package it.requestassistant.adapters.perform;

import it.requestassistant.application.port.out.ActionPerformerPort;
import it.requestassistant.application.port.out.MessagePort;
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

@Component
public class ActionPerformAdapter implements ActionPerformerPort {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MessagePort messagePort;
    private final PersistenceDaoPort persistenceDaoPort;

    public ActionPerformAdapter(MessagePort messagePort, PersistenceDaoPort persistenceDaoPort) {
        this.messagePort = messagePort;
        this.persistenceDaoPort = persistenceDaoPort;
    }

    /**
     * Esegue le azioni validate dall'operatore. Le "bozze" di message e request validate dall'operatore si trovano in aiResponse,
     * mentre message e request originari si trovano in pendingDecision.
     *
     * - RISPONDI_A_MAIL: invia la mail di risposta al mittente (maggiori informazioni o riscontro su avanzamento richiesta)
     * - INVIA_RICHIESTA: invia mail di richiesta al focal point
     * - INVIA_SOLLECITO: invia mail di sollecito al focal point o all'utente
     * - NUOVA_RICHIESTA: crea una nuova richiesta
     * - MODIFICA_RICHIESTA: modifica una richiesta esistente
     *
     * @param pendingDecision
     * @param decisionOption
     * @param dataAction
     * @throws Exception
     */
    @Override
    @Transactional
    public void perform(PendingDecision pendingDecision, DecisionOption decisionOption, DataAction dataAction) throws Exception {
        Action action = decisionOption.getAction();
        logger.debug("Esecuzione azione {}: ", action.getTitle());

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
            case INVIA_RICHIESTA -> {
                /**
                 * aiResponse:
                 * * "message": mail corretta/accettata dall'operatore
                 * * "reuest": null
                 *
                 *  la request originaria è in pendingDecision !
                 */
                logger.info("Eseguo azione INVIA_RICHIESTA per la pending decision {} con id {}",
                        pendingDecision.getId(), decisionOption.getId());

                //ilvia la mail e la sposta in in_lavorazione
                Message sendedMessage =
                        messagePort.sendMessage(dataAction.message(), true, pendingDecision.getRequest().getId());

                //dopa aver inviato la mail, aggiorno la request
                if(pendingDecision.getRequest() != null){
                    logger.debug("Aggiorno request {} della pending decision {}",
                            pendingDecision.getRequest().getId(), pendingDecision.getId());
                    LocalDateTime now = LocalDateTime.now();
                    pendingDecision.getRequest().setUpdateAt(now);
                    //pendingDecision.getRequest().getMessages().add(messageSended);
                    //non posso associare la mail appena inviata
                    pendingDecision.getRequest().getItems().forEach(item -> {
                        item.setUpdateAt(now);
                        item.setStatus(RequestItem.Status.RICHIESTO);
                    });

                    //associo il messaggio inviato alla request...
                    if(sendedMessage != null){
                        //prima lo salvo sul db (entryId aggiornato dopo eventuale spostamento in in_lavorazione
                        persistenceDaoPort.insertMessage(sendedMessage);
                        ArrayList<Message> messages = new ArrayList<>();
                        if(pendingDecision.getRequest().getMessages() != null){
                            messages.addAll(pendingDecision.getRequest().getMessages());
                        }
                        messages.add(sendedMessage);
                        //poi lo associo alla request
                        pendingDecision.getRequest().setMessages(messages);
                    }

                    persistenceDaoPort.updateRequest(pendingDecision.getRequest());
                    logger.debug("Request {} aggiornata", pendingDecision.getRequest().getId());
                }
                //break;
            }
            case INVIA_SOLLECITO -> {
                /**
                 * direttamente dal batch...
                 */
                logger.info("Eseguo azione INVIA_SOLLECITO per la pending decision {} con id {}",
                        pendingDecision.getId(), decisionOption.getId());
                messagePort.sendMessage(pendingDecision.getMessage(), false, null); //la mail rimane in"inviate"
            }
            case NUOVA_RICHIESTA -> {
                /**
                 * aiResponse:
                 * * "message": null
                 * * "request": una bozza con tutti i dati prelevati dal contesto di "message"
                 */
                logger.info("Eseguo azione NUOVA_RICHIESTA per la pending decision {} con id {}", pendingDecision.getId(), decisionOption.getId());

                //creo Request in stato NEW con items in stato DA_RICHIEDERE:
                dataAction.request().setStatus(Request.Status.IN_PROGRESS);
                dataAction.request().setCreateAt(LocalDateTime.now());
                dataAction.request().setUpdateAt(LocalDateTime.now());
                dataAction.request().getItems().forEach(item -> {
                    item.setStatus(RequestItem.Status.DA_RICHIEDERE);
                    item.setCreateAt(LocalDateTime.now());
                    item.setUpdateAt(LocalDateTime.now());
                });
                //non sostituire con lista immutabile List.of()
//                ArrayList<Message> messages = new ArrayList<>();
//                messages.add(pendingDecision.getMessage());
//                dataAction.request().setMessages(messages);//messaggio originario
//
//                dataAction.request().getMessages().forEach(message -> {
//                    logger.debug("Associo messaggio {} alla nuova request id {}",
//                            message.getEntryId(), dataAction.request().getId());
//                });

                //il messaggio originario deve essere inserito in bozza request dall'operatore
                //se la bozza request contiene il messaggio nel db bisogna aggiungere il riferimento
                //in message.requestId @TODO

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
