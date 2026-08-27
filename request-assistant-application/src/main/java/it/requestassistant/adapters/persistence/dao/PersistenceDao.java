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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        String queryPd = """
                SELECT pd.*
                FROM pending_decision pd JOIN decision_option do ON pd.id = do.pending_decision_id
                WHERE pd.type like 'MESSAGE_CLASSIFICATION'
                ORDER BY created_at DESC
                """;
        String queryDo = """
                SELECT do.* FROM decision_option do
                WHERE do.pending_decision_id = ?
                """;
        String queryMs = """
                SELECT m.* FROM message m
                WHERE m.id = ?
                """;


        List<PendingDecisionRow> pendingDecisionsRow = jdbcTemplate.query(
                queryPd,
                PersistenceRowMappers.PENDING_DECISION);
        logger.debug("Found {} pending decisions of type {}", pendingDecisionsRow.size(), type);


        pendingDecisionsRow.forEach(pdRow -> {;
            MessageRow messageRow = jdbcTemplate.queryForObject(
                queryMs,
                PersistenceRowMappers.MESSAGE,
                    pdRow.messageId());

            List<DecisionOptionRow> decisionOptionRows =
                    jdbcTemplate.query(queryDo, PersistenceRowMappers.DECISION_OPTION, pdRow.id());

              result.add(PersistenceDomainMappers
                      .toDomain(pdRow, decisionOptionRows, messageRow, null));
        });

        return result;
    }

    @Override
    public void deletePendingDecision(long id) {
        // TODO: implementare
    }

    @Override
    public void deleteDecisionOption(long id) {
        // TODO: implementare
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
                ps.setLong(4, pendingDecision.getId());
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

        jdbcTemplate.update(query,
                requestRow.createAt() != null ? requestRow.createAt().toString() : null,
                requestRow.updateAt() != null ? requestRow.updateAt().toString() : null,
                requestRow.title(),
                requestRow.status(),
                requestRow.note(),
                requestRow.userId()
        );
        //@TODO: implementare KeyHolder
        long requestId = -1;

        request.getItems().forEach(item -> {
            insertRequestItem(item, requestId);
        });

        //utente
        insertUserAccount(request.getUser());

        return requestId;
    }

    private void insertDecisionOption(DecisionOption decisionOption, long pendingDecisionId) {
        String query = """
                INSERT INTO decision_option (pending_decision_id, action, confidence, reasons)
                VALUES (?, ?, ?, ?)
                """;
        DecisionOptionRow row = PersistenceDomainMappers.toRow(decisionOption, pendingDecisionId);
        jdbcTemplate.update(query,
                row.pendingDecisionId(),
                row.action(),
                row.confidence(),
                row.reasons()
        );
        logger.debug("Decision option saved for pending decision id:{}", pendingDecisionId);
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
}
