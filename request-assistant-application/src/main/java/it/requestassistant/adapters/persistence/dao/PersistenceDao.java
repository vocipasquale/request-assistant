package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.adapters.persistence.mapper.PersistenceDomainMappers;
import it.requestassistant.adapters.persistence.mapper.PersistenceRowMappers;
import it.requestassistant.adapters.persistence.row.*;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static it.requestassistant.adapters.persistence.mapper.PersistenceDomainMappers.parseEnum;

@Component
public class PersistenceDao implements PersistenceDaoPort {

    public Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JdbcTemplate jdbcTemplate;

    public PersistenceDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Request findRequestByConversationId(String conversationId) {
        String messageByConversationQuery = """
                SELECT *
                FROM message
                WHERE conversation_id = ?
                ORDER BY received_at ASC
                """;

        String requestByIdQuery = """
                SELECT *
                FROM request
                WHERE id = ?
                """;

        String userByIdQuery = """
                SELECT *
                FROM user_account
                WHERE id = ?
                """;

        String requestItemsByRequestIdQuery = """
                SELECT *
                FROM request_item
                WHERE request_id = ?
                ORDER BY create_at ASC
                """;

        String messagesByRequestIdQuery = """
                SELECT *
                FROM message
                WHERE request_id = ?
                ORDER BY received_at ASC
                """;

        //ricerca di tutti i message con conversationId in input
        List<MessageRow> conversationMessages = jdbcTemplate.query(
                messageByConversationQuery,
                PersistenceRowMappers.MESSAGE,
                conversationId
        );

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

        //da riconsiderare se questa ricerca può restituire più di una request
        if (requestIds.size() > 1) {
            logger.warn("ConversationId {} associato a request_id multipli: {}", conversationId, requestIds);
            return null;
        }

        //requestId trovato, ricerca della relativa request
        Long requestId = requestIds.getFirst();

        List<RequestRow> requestRows = jdbcTemplate.query(requestByIdQuery, PersistenceRowMappers.REQUEST, requestId);
        if (requestRows.isEmpty()) {
            //caso improbabile, il requestId trovato non è associato ad alcuna request
            logger.warn("Request {} non trovata partendo da conversationId {}", requestId, conversationId);
            return null;
        }

        //recupero i dati dalle tabelle figli per costruire la Request
        RequestRow requestRow = requestRows.getFirst();
        List<UserAccountRow> userRows = jdbcTemplate.query(userByIdQuery, PersistenceRowMappers.USER_ACCOUNT, requestRow.userId());
        if (userRows.isEmpty()) {
            logger.warn("User {} non trovato per requestId {}", requestRow.userId(), requestId);
            return null;
        }

        //user trovato!
        UserAccountRow userRow = userRows.getFirst();

        //requestItem
        List<RequestItemRow> requestItemRows = jdbcTemplate.query(
                requestItemsByRequestIdQuery,
                PersistenceRowMappers.REQUEST_ITEM,
                requestId
        );

        //messages
        List<MessageRow> requestMessageRows = jdbcTemplate.query(
                messagesByRequestIdQuery,
                PersistenceRowMappers.MESSAGE,
                requestId
        );

        return PersistenceDomainMappers.toDomain(requestRow, userRow, requestItemRows, requestMessageRows);
    }

    @Override
    public Request findRequestByTk(String tk) {
        return null;
    }

    @Override
    public List<PendingDecision> findPendingDecisionsByType(PendingDecision.Type type) {
        List<PendingDecision> result = new ArrayList<>();
        List<PendingDecisionRow> pendingDecisionsRow = null;

        String queryPd = """
                SELECT pd.*
                FROM pending_decision pd
                WHERE pd.type like 'MESSAGE_CLASSIFICATION'
                ORDER BY created_at DESC
                """;

        //get pending decision...
        pendingDecisionsRow = jdbcTemplate.query(
                queryPd,
                PersistenceRowMappers.PENDING_DECISION);
        logger.debug("Found {} pending decisions of type {}", pendingDecisionsRow.size(), type);

        //relations...
        pendingDecisionsRow.forEach(pdRow -> {;
            MessageRow messageRow = null; //message della pending decision...
            RequestRow requestRow = null; //request
            List<RequestItemRow> requestItemRowList = null; //items della request...
            List<MessageRow> messagesRequestRowList =null; //messages della request...
            UserAccountRow userAccountRow = null; //user della request...

            //message of pending decision...
            Optional<MessageRow> messageRowOptional = findMessageById(pdRow.messageId());
            if(messageRowOptional.isPresent()) {
                messageRow = messageRowOptional.get();
                logger.debug("Found message id {} of pending decision id {}", messageRow.id(), pdRow.id());
            }else {
                logger.debug("Pending decision id {} has no user. ", pdRow.id());
            }

            ///////////////////////////////////////////////////////////////////////////////////////////
            //request...
            Optional<RequestRow> requestRowOptional = findRequestById(pdRow.requestId());
            if(requestRowOptional.isPresent()){
               requestRow = requestRowOptional.get();
               logger.debug("Found request id {} of pending decision id {}", requestRow.id(), pdRow.id());

                //request item..
                requestItemRowList = findRequestItemByRequestId(requestRow.id());
                logger.debug("Found {} request items of request id {}", requestItemRowList.size(), requestRow.id());

                //messages of request...
                messagesRequestRowList = findMessagesByRequestId(requestRow.id());
                logger.debug("Found {} messages of request id {}", messagesRequestRowList.size(), requestRow.id());

                //user..
                Optional<UserAccountRow> userAccountRowOptional = findUserAccountById(requestRow.userId());
                if (userAccountRowOptional.isPresent()){
                    userAccountRow = userAccountRowOptional.get();
                    logger.debug("Found user id {} of request id {}", userAccountRow.id(), requestRow.id());
                }else{
                    logger.debug("Request id {} has no user. ", requestRow.id());
                }
            }else {
                logger.debug("Pending decision id {} has no request. ", pdRow.id());
            }
            ///////////////////////////////////////////////////////////////////////////////////////////////

            ///////////////////////////////////////////////////////////////////////////////////////////////
            //decision options...
            List<DecisionOption> decisionOption = findDecisionOptionsByPendingDecisionId(pdRow.id());
            logger.debug("Found {} decisions options for pending decision id {}", decisionOption.size(), pdRow.id());
            ///////////////////////////////////////////////////////////////////////////////////////////////

            result.add(new PendingDecision(
                    pdRow.id(),
                    pdRow.createdAt(),
                    parseEnum(PendingDecision.Type.class, pdRow.type()),
                    pdRow.target(),
                    decisionOption,
                    PersistenceDomainMappers.toDomain(messageRow),
                    PersistenceDomainMappers.toDomain(requestRow, userAccountRow, requestItemRowList, messagesRequestRowList)
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
        deleteDecisionOptionByPendingDecisionId(pendingDecision.getId());

        //delete message
        deleteMessageById(pendingDecision.getId());

        //delete pending decision
        logger.debug("Deleting pending decision by id:{}",pendingDecision.getId());
        String query = """
                DELETE FROM pending_decision
                WHERE id = ?
                """;
        int deleted = jdbcTemplate.update(query, pendingDecision.getId());
        logger.info("Deleted {} pending decision with id:{}",deleted, pendingDecision.getId());
    }

    @Override
    public void deleteDecisionOption(long id) {
        // TODO: implementare
    }

    @Override
    public void refreshEntryIdMessage(String entryIdOld, String entryIdNew) {
        String query = """
                UPDATE message
                  set entry_id = ?
                where entry_id = ?                
                """;
        jdbcTemplate.update(query, entryIdNew, entryIdOld);
    }

    @Override
    @Transactional
    public void updateRequest(Request existingRequest) {
        String queryR = """
                UPDATE request
                  set update_at = ?,
                      title = ?,
                      status = ?,
                      note = ?,
                      user_id = ?
                where id = ?                
                """;
        RequestRow requestRow = PersistenceDomainMappers.toRow(existingRequest);

        jdbcTemplate.update(queryR,
                requestRow.updateAt() != null ? requestRow.updateAt().toString() : null,
                requestRow.title(),
                requestRow.status(),
                requestRow.note(),
                requestRow.userId(),
                requestRow.id()
        );

        //update message of request...
        existingRequest.getMessages().forEach(message -> {

            updateRequestIdOfMessage(message, existingRequest.getId());
        });

        //update items of request...
        existingRequest.getItems().forEach(item -> {
            String queryI = """
                UPDATE request_item
                  set request_id = ?,
                      type = ?,
                      create_at = ?,
                      update_at = ?,
                      dettaglio = ?,
                      nota = ?,
                      ambiente = ?,
                      status = ?,
                      ticket = ?
                where id = ?                
                """;
            RequestItemRow requestItemRow = PersistenceDomainMappers.toRow(item);

            jdbcTemplate.update(queryI,
                    requestItemRow.requestId(),
                    requestItemRow.type(),
                    requestItemRow.createAt() != null ? requestItemRow.createAt().toString() : null,
                    requestItemRow.updateAt() != null ? requestItemRow.updateAt().toString() : null,
                    requestItemRow.dettaglio(),
                    requestItemRow.nota(),
                    requestItemRow.ambienteRaw(),
                    requestItemRow.status(),
                    requestItemRow.ticket(),
                    requestItemRow.id()
            );
        });

    }

    @Override
    public List<Request> findRequestsToProcess() {
        List<Request> result = new ArrayList<>();

        String query = """
                SELECT r.*
                FROM request r
                LEFT JOIN pending_decision pd
                  ON pd.request_id = r.id
                WHERE r.status = 'IN_PROGRESS'
                AND pd.id IS NULL
                """;
        List<RequestRow> requestRows = jdbcTemplate.query(query, PersistenceRowMappers.REQUEST);
        if(requestRows.isEmpty()){
            logger.info("No requests to process found.");
            return result;
        }

        requestRows.stream().forEach(requestRow -> {
            //request item..
            List<RequestItemRow> requestItemRowList = findRequestItemByRequestId(requestRow.id());
            logger.debug("Found {} request items of request id {}", requestItemRowList.size(), requestRow.id());

            //messages of request...
            List<MessageRow> messagesRequestRowList = findMessagesByRequestId(requestRow.id());
            logger.debug("Found {} messages of request id {}", messagesRequestRowList.size(), requestRow.id());

            //user..
            Optional<UserAccountRow> userAccountRowOptional = findUserAccountById(requestRow.userId());
            UserAccountRow userAccountRow = null;
            if (userAccountRowOptional.isPresent()){
                userAccountRow = userAccountRowOptional.get();
                logger.debug("Found user id {} of request id {}", userAccountRow.id(), requestRow.id());
            }else{
                logger.debug("Request id {} has no user. ", requestRow.id());
            }

            result.add(PersistenceDomainMappers
                    .toDomain(requestRow, userAccountRow, requestItemRowList, messagesRequestRowList));
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

        String queryPd = "";

        //persist message
        long messageId = insertMessage(pendingDecision.getMessage());

        //la request NON va salvata/aggiornata ora...
        if(Objects.isNull(pendingDecision.getRequest())){
            logger.debug("Request absent in pending decision...");
            queryPd = """
                INSERT INTO pending_decision (created_at, type, target, message_id)
                VALUES (?, ?, ?, ?)
                """;
        }else{
            //ATTENZIONE AL ps.setLong(4, pendingDecision.getId());
            logger.debug("Request already saved in the database...");
            queryPd = """
                INSERT INTO pending_decision (created_at, type, target, message_id, request_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        }

        //Persist pending decision...
        PendingDecisionRow pendingDecisionRow = PersistenceDomainMappers.toRow(pendingDecision);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        String finalQueryPd = queryPd;
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(finalQueryPd, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, pendingDecisionRow.createdAt() != null ? pendingDecisionRow.createdAt().toString() : null);
            ps.setString(2, pendingDecisionRow.type());
            ps.setString(3, pendingDecisionRow.target());
            ps.setLong(4, messageId);
            if(!Objects.isNull(pendingDecision.getRequest())){//ATTENZIONE!!!
                ps.setLong(5, pendingDecision.getId());
            }
            return ps;
        }, keyHolder);
        long pendingDecisionId = keyHolder.getKey().longValue();
        logger.debug("Pending decision saved with id:{}", pendingDecisionId);

        //persist decision options
        pendingDecision.getOptions()
                .forEach(option ->
                        insertDecisionOption(option, pendingDecisionId));

        return pendingDecisionId;
    }

    @Override
    @Transactional
    public long insertRequest(Request request) {
        String query = """
                INSERT INTO request (create_at, update_at, title, status, note, user_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        RequestRow requestRow = PersistenceDomainMappers.toRow(request);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, requestRow.createAt() != null ? requestRow.createAt().toString() : null);
            ps.setString(2, requestRow.updateAt() != null ? requestRow.updateAt().toString() : null);
            ps.setString(3, requestRow.title());
            ps.setString(4, requestRow.status());
            ps.setString(5, requestRow.note());
            ps.setLong(6, requestRow.userId());

            return ps;
        }, keyHolder);

        long requestId = keyHolder.getKey().longValue();
        logger.debug("Request saved with id:{}", requestId);


        //insert items...
        request.getItems().forEach(item -> {
            insertRequestItem(item, requestId);
        });

        //associating messages at request...
        request.getMessages().forEach(message -> {

            updateRequestIdOfMessage(message, requestId);
        });

        //utente
        insertUserAccount(request.getUser());

        return requestId;
    }

    private void insertDecisionOption(DecisionOption decisionOption, long pendingDecisionId) {
        //insert Action...
        long actionId = insertAction(decisionOption.getAction());

        //insert action steps...
        decisionOption.getAction().getSteps().forEach(step -> {
            insertActionStep(step, actionId);
        });

        //insert decision options...
        String query = """
                INSERT INTO decision_option (pending_decision_id, action_id, confidence, reasons)
                VALUES (?, ?, ?, ?)
                """;
        DecisionOptionRow row = PersistenceDomainMappers.toRow(decisionOption, pendingDecisionId, actionId);
        jdbcTemplate.update(query,
                row.pendingDecisionId(),
                row.actionId(),
                row.confidence(),
                row.reasons()
        );
        logger.debug("Decision option saved for pending decision id:{}", pendingDecisionId);
    }

    private void insertActionStep(String step, long actionId) {
        String query = """
                INSERT INTO action_step (action_id, step_description)
                VALUES (?, ?)
                """;
        ActionStepRow row = PersistenceDomainMappers.toRow(actionId, step);
        jdbcTemplate.update(query,
                row.actionId(),
                row.stepDescription()
        );
        logger.debug("Action step saved for action id:{}", actionId);
    }

    private long insertAction(Action action) {
        String queryAct = "INSERT INTO action (title) VALUES (?)";

        ActionRow actionRow = PersistenceDomainMappers.toRow(action);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(queryAct, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, actionRow.title());
            return ps;
        }, keyHolder);
        long actionId = keyHolder.getKey().longValue();
        logger.info("Saved action id:{}", actionId);

        return actionId;
    }

    private void insertRequestItem(RequestItem requestItem, long requestId) {
        String query = """
                INSERT INTO request_item (request_id, type, create_at, update_at, dettaglio, nota, ambiente, status, ticket)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        RequestItemRow requestItemRow = PersistenceDomainMappers.toRow(requestItem);

        jdbcTemplate.update(query,
                requestItemRow.requestId(),
                requestItemRow.type(),
                requestItemRow.createAt() != null ? requestItemRow.createAt().toString() : null,
                requestItemRow.updateAt() != null ? requestItemRow.updateAt().toString() : null,
                requestItemRow.dettaglio(),
                requestItemRow.nota(),
                requestItemRow.ambienteRaw(),
                requestItemRow.status(),
                requestItemRow.ticket()
        );
    }

    private long insertMessage(Message message) {
        String query = """
                INSERT INTO message (request_id, subject, sender_address, received_at, to_address, cc_address, body_text, entry_id, conversation_id, conversation_topic, importance, has_attachment, category)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        MessageRow messageRow = PersistenceDomainMappers.toRow(message);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, messageRow.requestId());
            ps.setString(2, messageRow.subject());
            ps.setString(3, messageRow.senderAddress());
            ps.setString(4, messageRow.receivedAt() != null ? messageRow.receivedAt().toString() : null);
            ps.setString(5, messageRow.toAddress());
            ps.setString(6, messageRow.ccAddress());
            ps.setString(7, messageRow.bodyText());
            ps.setString(8, messageRow.entryId());
            ps.setString(9, messageRow.conversationId());
            ps.setString(10, messageRow.conversationTopic());
            ps.setInt(11, messageRow.importance());
            ps.setInt(12, messageRow.hasAttachment() != null ? (messageRow.hasAttachment() ? 1 : 0) : null);
            ps.setString(13, messageRow.category());

            return ps;
        }, keyHolder);
        long messageId = keyHolder.getKey().longValue();
        logger.info("Saved message id:{}", messageId);

        return messageId;
    }

    private void insertUserAccount(User user) {
        String query = """
                INSERT INTO user_account (cognome, nome, codice_fiscale, email, utenza)
                VALUES (?, ?, ?, ?, ?)
                """;
        UserAccountRow userAccountRow = PersistenceDomainMappers.toRow(user);

        jdbcTemplate.update(query,
                userAccountRow.cognome(),
                userAccountRow.nome(),
                userAccountRow.codiceFiscale(),
                userAccountRow.email(),
                userAccountRow.utenza()
        );
    }

    private List<DecisionOption> findDecisionOptionsByPendingDecisionId(long pendingDecisionId){
        List<DecisionOption> result = new ArrayList<>();

        String queryDo = """
                SELECT do.* FROM decision_option do
                WHERE do.pending_decision_id = ?
                """;
        List<DecisionOptionRow> decisionOptionList =
                jdbcTemplate.query(queryDo, PersistenceRowMappers.DECISION_OPTION, pendingDecisionId);

        //action e action steps
        for(DecisionOptionRow decisionOptionRow:decisionOptionList){
            Optional<ActionRow> actionRowOptional = findActionRow(decisionOptionRow.actionId());
            ActionRow actionRow=null;
            List<ActionStepRow> actionStepRows = null;

            if(actionRowOptional.isPresent()){
                actionRow = actionRowOptional.get();
                actionStepRows = findActionSteps(actionRow.id());
            }else{
               actionRow = new ActionRow(0L, "Nessuna azione proposta.");
                actionStepRows = new ArrayList<>();
            }

            result.add(new DecisionOption(
                    decisionOptionRow.id(),
                    PersistenceDomainMappers.toDomain(actionRow, actionStepRows),
                    decisionOptionRow.confidence(),
                    decisionOptionRow.reasons()
            ));
        }
        return result;
    }

    private Optional<ActionRow> findActionRow(long actionId){
        String queryAc = "SELECT * FROM action WHERE id = ?";
        return jdbcTemplate.query(queryAc, PersistenceRowMappers.ACTION, actionId)
                        .stream()
                        .findFirst();
    }

    private List<ActionStepRow> findActionSteps(long actionId){
        String queryAc = "SELECT * FROM action_step WHERE action_id = ?";
        return jdbcTemplate.query(queryAc, PersistenceRowMappers.ACTION_STEP, actionId);
    }

    private Optional<MessageRow> findMessageById(long id){
        String queryMs = """
                SELECT m.* FROM message m
                WHERE m.id = ?
                """;
        return jdbcTemplate.query(queryMs, PersistenceRowMappers.MESSAGE, id)
                .stream()
                .findFirst();
    }

    private Optional<RequestRow> findRequestById(long id){
        String queryRq = """
                SELECT r.* FROM request r
                WHERE r.id = ?
                """;
        return jdbcTemplate.query(queryRq, PersistenceRowMappers.REQUEST, id)
                .stream()
                .findFirst();
    }

    private List<RequestItemRow> findRequestItemByRequestId(long id){
        String queryRqIt = """
                SELECT ri.* FROM request_item ri
                WHERE request_id = ?
                """;
        return jdbcTemplate.query(queryRqIt, PersistenceRowMappers.REQUEST_ITEM, id);
    }

    private Optional<UserAccountRow> findUserAccountById(long id){
        String queryUser = """
                SELECT u.* FROM user_account u
                WHERE id = ?
                """;
        return jdbcTemplate.query(queryUser, PersistenceRowMappers.USER_ACCOUNT, id)
                .stream()
                .findFirst();
    }

    private List<MessageRow> findMessagesByRequestId(long id){
        String queryMsRq = """
                SELECT m.* FROM message m
                WHERE m.request_id = ?
                """;
        return jdbcTemplate.query(queryMsRq, PersistenceRowMappers.MESSAGE, id);
    }

    private void deleteDecisionOptionByPendingDecisionId(long pendingDecisionId) {
        logger.debug("Deleting decision options by pending decision id:{}",pendingDecisionId);
        String query = """
                DELETE FROM decision_option
                WHERE pending_decision_id = ?
                """;
        int deleted = jdbcTemplate.update(query, pendingDecisionId);
        logger.info("Deleted {} decision options by pending decision id:{}",deleted, pendingDecisionId);
    }

    private void deleteMessageById(long id){
        logger.debug("Deleting message by id:{}", id);
        String query = """
                DELETE FROM message
                WHERE id = ?
                """;
        int deleted = jdbcTemplate.update(query, id);
        logger.info("Deleted {} message with id:{}",deleted, id);
    }

    private void deleteActionAndActionSteps(PendingDecision pendingDecision) {
        String queryDo = """
                SELECT do.* FROM decision_option do
                WHERE do.pending_decision_id = ?
                """;
        List<DecisionOptionRow> decisionOptionList =
                jdbcTemplate.query(queryDo, PersistenceRowMappers.DECISION_OPTION, pendingDecision.getId());


        decisionOptionList.forEach(option -> {
            long actionId = option.actionId();
            logger.debug("Deleting action steps for action id:{}", actionId);
            String queryActionSteps = """
                    DELETE FROM action_step
                    WHERE action_id = ?
                    """;
            int deletedSteps = jdbcTemplate.update(queryActionSteps, actionId);
            logger.info("Deleted {} action steps for action id:{}", deletedSteps, actionId);

            logger.debug("Deleting action with id:{}", actionId);
            String queryAction = """
                    DELETE FROM action
                    WHERE id = ?
                    """;
            int deletedActions = jdbcTemplate.update(queryAction, actionId);
            logger.info("Deleted {} actions with id:{}", deletedActions, actionId);
        });
    }

    private void updateRequestIdOfMessage(Message message, long requestId) {
        String queryM = """
                UPDATE message
                  set request_id = ?
                where id = ?                
                """;
        MessageRow messageRow = PersistenceDomainMappers.toRow(message);

        jdbcTemplate.update(queryM,
                messageRow.requestId(),
                messageRow.id()
        );
    }
}
