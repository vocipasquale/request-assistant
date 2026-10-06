package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.adapters.persistence.mapper.PersistenceDomainMappers;
import it.requestassistant.adapters.persistence.mapper.PersistenceRowMappers;
import it.requestassistant.adapters.persistence.row.*;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.domain.model.*;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

import static it.requestassistant.adapters.persistence.mapper.PersistenceDomainMappers.parseEnum;

@Service
public class PersistenceDaoService implements PersistenceDaoPort {

    public Logger logger = LoggerFactory.getLogger(this.getClass());
    //private final JdbcTemplate jdbcTemplate;
    private final PersistentDaoAdapter persistentDaoAdapter;

    public PersistenceDaoService(PersistentDaoAdapter persistentDaoAdapter) {
        //this.jdbcTemplate = jdbcTemplate;
        this.persistentDaoAdapter = persistentDaoAdapter;
    }


    @Override
    public Request findRequestByConversationId(String conversationId) {
        //cerco i messaggi per conversationId
        List<MessageRow> conversationMessages = persistentDaoAdapter.findMessageByConversationId(conversationId);
        if (conversationMessages.isEmpty()) {
            logger.debug("Nessun messaggio trovato per conversationId {}", conversationId);
            return null;
        }

        //distinct su requestId
        List<Long> requestIds = conversationMessages.stream()
                .map(MessageRow::requestId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (requestIds.isEmpty()) {
            logger.debug("Messaggi trovati per conversationId {}, ma nessun request_id associato", conversationId);
            return null;
        }

        //@TODO: da riconsiderare se questa ricerca può restituire più di una request
        if (requestIds.size() > 1) {
            logger.warn("ConversationId {} associato a request_id multipli: {}", conversationId, requestIds);
            return null;
        }

        //requestId trovato, ricerca della relativa request
        Long requestId = requestIds.getFirst();

        Optional<RequestRow> requestRowOptional = persistentDaoAdapter.findRequestById(requestId);
        if (!requestRowOptional.isPresent()) {
            //caso improbabile, il requestId trovato non è associato ad alcuna request
            logger.warn("Richiesta {} non trovata partendo da conversationId {}", requestId, conversationId);
            return null;
        }

        return buildCompleteRequestByRow(requestRowOptional.get());
    }



    /**
     * Restituisce la request che ha almeno un requestItem con ticket passato in input.
     *
     * @param tk
     * @return
     */
    @Override
    public Request findRequestByTk(String tk) {
        RequestRow requestRow = persistentDaoAdapter.findRequestByTk(tk);
        if (Objects.isNull(requestRow)) {
            logger.debug("Nessuna richiesta trovata per ticket {}", tk);
            return null;
        }

        return buildCompleteRequestByRow(requestRow);
    }

    @Override
    public List<PendingDecision> findPendingDecisionsByType(PendingDecision.Type type) {
        List<PendingDecision> result = new ArrayList<>();
        List<PendingDecisionRow> pendingDecisionsRow = null;

        //recupero le pending decision...
        pendingDecisionsRow = persistentDaoAdapter.findPendingDecisionByType(type.name());
        logger.debug("Trovate {} pending decision di tipo {}", pendingDecisionsRow.size(), type.name());

        //relazioni...
        pendingDecisionsRow.forEach(pdRow -> {
            Request request = null;
            MessageRow messageRow = null; //message della pending decision...
            RequestRow requestRow = null; //request

            //messaggio della pending decision...
            Optional<MessageRow> messageRowOptional = persistentDaoAdapter.findMessageById(pdRow.messageId());
            if(messageRowOptional.isPresent()) {
                messageRow = messageRowOptional.get();
                logger.debug("Trovato il messaggio id {} della pending decision id {}", messageRow.id(), pdRow.id());
            }else {
                logger.debug("La pending decision id {} non ha messaggi. ", pdRow.id());
            }

            ///////////////////////////////////////////////////////////////////////////////////////////
            //request...
            Optional<RequestRow> requestRowOptional = persistentDaoAdapter.findRequestById(pdRow.requestId());
            if(requestRowOptional.isPresent()){
               request = buildCompleteRequestByRow(requestRow);
            }else {
                logger.debug("La pending decision id {} non ha request. ", pdRow.id());
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////

            ///////////////////////////////////////////////////////////////////////////////////////////////
            //decision option...
            List<DecisionOption> decisionOption = findDecisionOptionsByPendingDecisionId(pdRow.id());
            logger.debug("Trovate {} decision option per la pending decision id {}", decisionOption.size(), pdRow.id());
            ///////////////////////////////////////////////////////////////////////////////////////////////

            result.add(new PendingDecision(
                    pdRow.id(),
                    pdRow.createdAt(),
                    parseEnum(PendingDecision.Type.class, pdRow.type()),
                    pdRow.target(),
                    decisionOption,
                    PersistenceDomainMappers.toDomain(messageRow),
                    request
            ));
        });

        return result;
    }

    @Override
    public List<PendingDecision> findAllPendingDecisions() {
        List<PendingDecision> result = new ArrayList<>();
        List<PendingDecisionRow> pendingDecisionsRow = persistentDaoAdapter.findAllPendingDecisions();

        logger.debug("Trovate {} pending decision", pendingDecisionsRow.size());

        //relazioni...
        pendingDecisionsRow.forEach(pdRow -> {;
            MessageRow messageRow = null; //message della pending decision...
            RequestRow requestRow = null; //request
            Request request = null;

            //messaggio della pending decision...
            Optional<MessageRow> messageRowOptional = persistentDaoAdapter.findMessageById(pdRow.messageId());
            if(messageRowOptional.isPresent()) {
                messageRow = messageRowOptional.get();
                logger.debug("Trovato il messaggio id {} della pending decision id {}", messageRow.id(), pdRow.id());
            }else {
                logger.debug("La pending decision id {} non ha messaggio.");
            }

            ///////////////////////////////////////////////////////////////////////////////////////////
            //request...
            Optional<RequestRow> requestRowOptional = persistentDaoAdapter.findRequestById(pdRow.requestId());
            if(requestRowOptional.isPresent()){
                requestRow = requestRowOptional.get();
                logger.debug("Trovata la request id {} della pending decision id {}", requestRow.id(), pdRow.id());
                request = buildCompleteRequestByRow(requestRow);
            }else {
                logger.debug("La pending decision id {} non ha request. ", pdRow.id());
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////

            ///////////////////////////////////////////////////////////////////////////////////////////////
            //decision option...
            List<DecisionOption> decisionOption = findDecisionOptionsByPendingDecisionId(pdRow.id());
            logger.debug("Trovate {} decision option per la pending decision id {}", decisionOption.size(), pdRow.id());
            ///////////////////////////////////////////////////////////////////////////////////////////////

            result.add(new PendingDecision(
                    pdRow.id(),
                    pdRow.createdAt(),
                    parseEnum(PendingDecision.Type.class, pdRow.type()),
                    pdRow.target(),
                    decisionOption,
                    messageRow == null ? null : PersistenceDomainMappers.toDomain(messageRow),
                    request
            ));
        });

        return result;
    }

    @Override
    @Transactional
    public void deletePendingDecision(PendingDecision pendingDecision) {
        //delete action e action steps...
        deleteActionAndActionSteps(pendingDecision);

        //delete decision_option
        int deleted = persistentDaoAdapter.deleteDecisionOptionByPendingDecisionId(pendingDecision.getId());
        logger.debug("Cancellate {} decision option per la pending decision id {}", deleted, pendingDecision.getId());

        //delete message
        deleted = persistentDaoAdapter.deleteMessageByIdOnlyNotReferenced(pendingDecision.getId());
        logger.debug("Cancellato {} messaggio per la pending decision id {}", deleted, pendingDecision.getId());

        //delete pending decision
        deleted = persistentDaoAdapter.deletePendingDecision(pendingDecision.getId());
        logger.info("Eliminate {} decisioni pendenti con id:{}",deleted, pendingDecision.getId());
    }

    @Override
    public boolean refreshEntryIdMessage(String entryIdOld, String entryIdNew) {
        return persistentDaoAdapter.refreshEntryIdMessage(entryIdOld, entryIdNew) > 0;
    }

    @Override
    @Transactional
    public void updateRequest(Request existingRequest) {
        RequestRow requestRow = PersistenceDomainMappers.toRow(existingRequest);
        persistentDaoAdapter.updateRequest(requestRow);

        //update message of request...
        existingRequest.getMessages().forEach(message -> {
            persistentDaoAdapter.updateRequestIdOfMessage(message.getId(), existingRequest.getId());
        });

        //update items of request...
        existingRequest.getItems().forEach(item -> {
            RequestItemRow requestItemRow = PersistenceDomainMappers.toRow(item);
            persistentDaoAdapter.updateRequestItem(requestItemRow);
        });

    }

    @Override
    public List<Request> findRequestsToProcess() {
        List<Request> result = new ArrayList<>();

        List<RequestRow> requestRows = persistentDaoAdapter.findRequestInProgress();
        if(requestRows.isEmpty()){
            logger.info("Nessuna richiesta da elaborare trovata.");
            return result;
        }

        requestRows.stream().forEach(requestRow -> {
            result.add(buildCompleteRequestByRow(requestRow));
        });

        return result;
    }


    @Override
    @Transactional
    public long insertPendingDecision(PendingDecision pendingDecision) {
        /**
         * Questo metodo viene richiamato soltanto dal batch (orchestratore dopo
         * che ha "ricevuto la "PendingDecision" da parete del motore AI.
         * Quindi bisogna salvare la PendingDecision (e i figli) in modo che
         * in un secondo tempo l'operatore la possa gestire dalla dashboard.
         */

        long messageId;
        //persisto il messaggio se esiste
        if(pendingDecision.getMessage() != null){
            messageId = insertMessage(pendingDecision.getMessage());
        } else {
            messageId = -1;
        }

        //la request NON va salvata/aggiornata ora...
        long pendingDecisionId = persistentDaoAdapter.insertPendingDecision(PersistenceDomainMappers.toRow(pendingDecision, messageId));
        logger.debug("Pending decision salvata con id:{}", pendingDecisionId);

        //persisto le decision option
        pendingDecision.getOptions()
                .forEach(option ->
                        insertDecisionOption(option, pendingDecisionId));

        return pendingDecisionId;
    }

    @Override
    @Transactional
    public long insertRequest(Request request) {
        //utente
        long userId = insertUserAccount(request.getUser());
        request.getUser().setId(userId);

        //request
        long requestId = persistentDaoAdapter.insertRequest(PersistenceDomainMappers.toRow(request));
        logger.debug("Richiesta salvata con id:{}", requestId);

        //inserisco gli elementi...
        request.getItems().forEach(item -> {
            persistentDaoAdapter.insertRequestItem(PersistenceDomainMappers.toRow(item, requestId));
        });

        //associo i messaggi alla request...
        request.getMessages().forEach(message -> {
            persistentDaoAdapter.updateRequestIdOfMessage(message.getId(), requestId);
        });

        return requestId;
    }

    private Request buildCompleteRequestByRow(RequestRow requestRow) {
        Optional<UserAccountRow> userAccountRowOptional = persistentDaoAdapter.findUserAccountById(requestRow.userId());
        if (!userAccountRowOptional.isPresent()) {
            logger.warn("Utente {} non trovato per requestId {}", requestRow.userId(), requestRow.userId());
            return null;
        }

        //user trovato!
        UserAccountRow userRow = userAccountRowOptional.get();
        List<RequestItemRow> requestItemRows = persistentDaoAdapter.findRequestItemByRequestId(requestRow.userId());

        //messaggi della request
        List<MessageRow> requestMessageRows = persistentDaoAdapter.findMessagesByRequestId(requestRow.userId());
        return PersistenceDomainMappers.toDomain(requestRow, userRow, requestItemRows, requestMessageRows);
    }

    private void insertDecisionOption(DecisionOption decisionOption, long pendingDecisionId) {
        //inserisco la action...
        long actionId = persistentDaoAdapter.insertAction(PersistenceDomainMappers.toRow(decisionOption.getAction()));
        //inserisco le decision option...
        persistentDaoAdapter.insertDecisionOption(PersistenceDomainMappers.toRow(decisionOption, pendingDecisionId, actionId));
        logger.debug("Decision option salvata per la pending decision id:{}", pendingDecisionId);
    }

    @Override
    public long insertMessage(Message message) {
        return persistentDaoAdapter.insertMessage(PersistenceDomainMappers.toRow(message));
    }

    private long insertUserAccount(User user) {
        //ricerca se l'utente esiste già
        Optional<UserAccountRow> userTrovato = persistentDaoAdapter.findExistsUserAccount(PersistenceDomainMappers.toRow(user));

        if(!userTrovato.isPresent()){
            //inserisce l'utente
            logger.info("Utente non trovato, inserisco nuovo utente: {}", user.getCognome()+ " "+user.getNome());
            long userId = persistentDaoAdapter.insertUserAccount(PersistenceDomainMappers.toRow(user));
            logger.info("UserAccount salvato con id:{}", userId);
            return userId;
        }else{
            //merge dei dati
            logger.info("Utente già esistente, aggiorno i dati dell'utente: {}", user.getCognome()+ " "+user.getNome());

            String utenza = Strings.isNotEmpty(user.getUtenza()) ? user.getUtenza().toUpperCase() : userTrovato.get().utenza();
            String cf = Strings.isNotEmpty(user.getCodiceFiscale()) ? user.getCodiceFiscale().toUpperCase() : userTrovato.get().codiceFiscale();
            String email = Strings.isNotEmpty(user.getEmail()) ? user.getEmail() : userTrovato.get().email();

            //nuovo UserAccountRow con i dati mergiati...
            persistentDaoAdapter.updateUserAccount(new UserAccountRow(
                    userTrovato.get().id(),
                    user.getCognome(),
                    user.getNome(),
                    cf,
                    email,
                    utenza));
            return userTrovato.get().id();
        }
    }

    private List<DecisionOption> findDecisionOptionsByPendingDecisionId(long pendingDecisionId){
        List<DecisionOption> result = new ArrayList<>();

        List<DecisionOptionRow> decisionOptionList =
                persistentDaoAdapter.findDecisionOptionsByPendingDecisionId(pendingDecisionId);

        //action e action step
        for(DecisionOptionRow decisionOptionRow:decisionOptionList){
            Optional<ActionRow> actionRowOptional = persistentDaoAdapter.findActionById(decisionOptionRow.actionId());
            ActionRow actionRow=null;

            if(actionRowOptional.isPresent()){
                actionRow = actionRowOptional.get();
            }else{
               actionRow = new ActionRow(0L, "Nessuna azione proposta.", "");
            }

            result.add(new DecisionOption(
                    decisionOptionRow.id(),
                    PersistenceDomainMappers.toDomain(actionRow),
                    decisionOptionRow.confidence(),
                    decisionOptionRow.reasons()
            ));
        }
        return result;
    }


    private void deleteActionAndActionSteps(PendingDecision pendingDecision) {
        //recupero le decision options della pending decision...
        List<DecisionOptionRow> decisionOptionList =
                persistentDaoAdapter.findDecisionOptionsByPendingDecisionId(pendingDecision.getId());

        decisionOptionList.forEach(option -> {
            long actionId = option.actionId();
            logger.debug("Elimino l'azione con id:{}", actionId);
            int deletedActions = persistentDaoAdapter.deleteAction(actionId);
            logger.info("Eliminate {} azioni con id:{}", deletedActions, actionId);
        });
    }

}
