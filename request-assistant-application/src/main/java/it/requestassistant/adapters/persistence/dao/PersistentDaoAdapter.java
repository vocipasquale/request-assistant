package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.adapters.persistence.mapper.PersistenceRowMappers;
import it.requestassistant.adapters.persistence.row.ActionRow;
import it.requestassistant.adapters.persistence.row.DecisionOptionRow;
import it.requestassistant.adapters.persistence.row.MessageRow;
import it.requestassistant.adapters.persistence.row.PendingDecisionRow;
import it.requestassistant.adapters.persistence.row.RequestItemRow;
import it.requestassistant.adapters.persistence.row.RequestRow;
import it.requestassistant.adapters.persistence.row.UserAccountRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class PersistentDaoAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PersistentDaoAdapter.class);
    private final JdbcTemplate jdbcTemplate;

    public PersistentDaoAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public long insertAction(ActionRow row) {
        return insert("INSERT INTO action (title, ai_response) VALUES (?, ?)", row.title(), row.aiResponse());
    }

    public Optional<ActionRow> findActionById(long id) {
        return findById("SELECT * FROM action WHERE id = ?", PersistenceRowMappers.ACTION, id);
    }

    public List<ActionRow> findAllActions() {
        return jdbcTemplate.query("SELECT * FROM action ORDER BY id", PersistenceRowMappers.ACTION);
    }

    public boolean updateAction(ActionRow row) {
        return jdbcTemplate.update("UPDATE action SET title = ?, ai_response = ? WHERE id = ?",
                row.title(), row.aiResponse(), row.id()) > 0;
    }

    public int deleteAction(long id) {
        return jdbcTemplate.update("DELETE FROM action WHERE id = ?", id);
    }

    public long insertDecisionOption(DecisionOptionRow row) {
        return insert("""
                INSERT INTO decision_option (pending_decision_id, action_id, confidence, reasons)
                VALUES (?, ?, ?, ?)
                """, row.pendingDecisionId(), row.actionId(), row.confidence(), row.reasons());
    }

    public Optional<DecisionOptionRow> findDecisionOptionById(long id) {
        return findById("SELECT * FROM decision_option WHERE id = ?", PersistenceRowMappers.DECISION_OPTION, id);
    }

    public List<DecisionOptionRow> findAllDecisionOptions() {
        return jdbcTemplate.query("SELECT * FROM decision_option ORDER BY id", PersistenceRowMappers.DECISION_OPTION);
    }

    public List<DecisionOptionRow> findDecisionOptionsByPendingDecisionId(long pendingDecisionId) {
        return jdbcTemplate.query("SELECT do.* FROM decision_option do WHERE do.pending_decision_id = ?", PersistenceRowMappers.DECISION_OPTION, pendingDecisionId);
    }

    public boolean updateDecisionOption(DecisionOptionRow row) {
        return jdbcTemplate.update("""
                        UPDATE decision_option
                        SET pending_decision_id = ?, action_id = ?, confidence = ?, reasons = ?
                        WHERE id = ?
                        """,
                row.pendingDecisionId(), row.actionId(), row.confidence(), row.reasons(), row.id()) > 0;
    }

    public boolean deleteDecisionOption(long id) {
        return jdbcTemplate.update("DELETE FROM decision_option WHERE id = ?", id) > 0;
    }

    public int deleteDecisionOptionByPendingDecisionId(long pendingDecisionId) {
        return jdbcTemplate.update("DELETE FROM decision_option WHERE pending_decision_id = ?", pendingDecisionId);
      }

    public long insertMessage(MessageRow row) {
        return insert("""
                INSERT INTO message (request_id, subject, sender_address, received_at, to_address, cc_address,
                                     body_text, entry_id, conversation_id, conversation_topic, importance,
                                     has_attachment, category)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.requestId(), row.subject(), row.senderAddress(), toSqlDateTime(row.receivedAt()),
                row.toAddress(), row.ccAddress(), row.bodyText(), row.entryId(), row.conversationId(),
                row.conversationTopic(), row.importance(), toSqlBoolean(row.hasAttachment()), row.category());
    }

    public Optional<MessageRow> findMessageById(long id) {
        return findById("SELECT * FROM message WHERE id = ?", PersistenceRowMappers.MESSAGE, id);
    }

    public List<MessageRow> findAllMessages() {
        return jdbcTemplate.query("SELECT * FROM message ORDER BY id", PersistenceRowMappers.MESSAGE);
    }

    public List<MessageRow> findMessageByConversationId(String conversationId){
        return jdbcTemplate.query("SELECT * FROM message WHERE conversation_id = ? ORDER BY received_at ASC",
                PersistenceRowMappers.MESSAGE, conversationId);
    }

    public List<MessageRow> findMessagesByRequestId(long requestId){
        return jdbcTemplate.query("SELECT * FROM message WHERE request_id = ? ORDER BY received_at ASC",
                PersistenceRowMappers.MESSAGE, requestId);
    }

    public int updateRequestIdOfMessage(long messageId, long requestId) {
        return jdbcTemplate.update("UPDATE message set request_id = ? where id = ?", requestId, messageId );
    }


    public boolean updateMessage(MessageRow row) {
        return jdbcTemplate.update("""
                        UPDATE message
                        SET request_id = ?, subject = ?, sender_address = ?, received_at = ?, to_address = ?,
                            cc_address = ?, body_text = ?, entry_id = ?, conversation_id = ?,
                            conversation_topic = ?, importance = ?, has_attachment = ?, category = ?
                        WHERE id = ?
                        """,
                row.requestId(), row.subject(), row.senderAddress(), toSqlDateTime(row.receivedAt()),
                row.toAddress(), row.ccAddress(), row.bodyText(), row.entryId(), row.conversationId(),
                row.conversationTopic(), row.importance(), toSqlBoolean(row.hasAttachment()), row.category(),
                row.id()) > 0;
    }

    public int refreshEntryIdMessage(String entryIdOld, String entryIdNew) {
        return jdbcTemplate.update("UPDATE message set entry_id = ? where entry_id = ?", entryIdNew, entryIdOld);
    }
    public boolean deleteMessage(long id) {
        return jdbcTemplate.update("DELETE FROM message WHERE id = ?", id) > 0;
    }

    public int deleteMessageByIdOnlyNotReferenced(long id){
        String query = """
                DELETE FROM message
                WHERE id = ?
                AND NOT EXISTS (SELECT 1 FROM request WHERE id = request_id)
                """;
        return jdbcTemplate.update(query, id);
    }

    public long insertPendingDecision(PendingDecisionRow row) {
        return insert("""
                INSERT INTO pending_decision (created_at, type, target, message_id, request_id)
                VALUES (?, ?, ?, ?, ?)
                """, toSqlDateTime(row.createdAt()), row.type(), row.target(),
                nullableId(row.messageId()), nullableId(row.requestId()));
    }

    public Optional<PendingDecisionRow> findPendingDecisionById(long id) {
        return findById("SELECT * FROM pending_decision WHERE id = ?", PersistenceRowMappers.PENDING_DECISION, id);
    }

    public List<PendingDecisionRow> findAllPendingDecisions() {
        return jdbcTemplate.query("SELECT * FROM pending_decision ORDER BY created_at DESC", PersistenceRowMappers.PENDING_DECISION);
    }

    public List<PendingDecisionRow> findPendingDecisionByType(String type){
        String queryPd = """
                SELECT pd.*
                FROM pending_decision pd
                WHERE pd.type = ?
                ORDER BY created_at DESC
                """;
        //recupero le pending decision...
        return jdbcTemplate.query(queryPd, PersistenceRowMappers.PENDING_DECISION, type);
    }

    public boolean updatePendingDecision(PendingDecisionRow row) {
        return jdbcTemplate.update("""
                        UPDATE pending_decision
                        SET created_at = ?, type = ?, target = ?, message_id = ?, request_id = ?
                        WHERE id = ?
                        """,
                toSqlDateTime(row.createdAt()), row.type(), row.target(), nullableId(row.messageId()),
                nullableId(row.requestId()), row.id()) > 0;
    }

    public int deletePendingDecision(long id) {
        return jdbcTemplate.update("DELETE FROM pending_decision WHERE id = ?", id);
    }

    public long insertRequestItem(RequestItemRow row) {
        return insert("""
                INSERT INTO request_item (request_id, type, create_at, update_at, dettaglio, nota, ambiente, status, ticket)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, row.requestId(), row.type(), toSqlDateTime(row.createAt()), toSqlDateTime(row.updateAt()),
                row.dettaglio(), row.nota(), row.ambienteRaw(), row.status(), row.ticket());
    }

    public Optional<RequestItemRow> findRequestItemById(long id) {
        return findById("SELECT * FROM request_item WHERE id = ?", PersistenceRowMappers.REQUEST_ITEM, id);
    }

    public List<RequestItemRow> findAllRequestItems() {
        return jdbcTemplate.query("SELECT * FROM request_item ORDER BY id", PersistenceRowMappers.REQUEST_ITEM);
    }

    public List<RequestItemRow> findRequestItemByRequestId(long requestId){
        return jdbcTemplate.query("SELECT * FROM request_item WHERE request_id = ? ORDER BY create_at ASC", PersistenceRowMappers.REQUEST_ITEM, requestId);
    }

    public boolean updateRequestItem(RequestItemRow row) {
        return jdbcTemplate.update("""
                        UPDATE request_item
                        SET request_id = ?, type = ?, create_at = ?, update_at = ?, dettaglio = ?, nota = ?,
                            ambiente = ?, status = ?, ticket = ?
                        WHERE id = ?
                        """,
                row.requestId(), row.type(), toSqlDateTime(row.createAt()), toSqlDateTime(row.updateAt()),
                row.dettaglio(), row.nota(), row.ambienteRaw(), row.status(), row.ticket(), row.id()) > 0;
    }

    public boolean deleteRequestItem(long id) {
        return jdbcTemplate.update("DELETE FROM request_item WHERE id = ?", id) > 0;
    }

    public long insertRequest(RequestRow row) {
        return insert("""
                INSERT INTO request (create_at, update_at, title, status, note, user_id)
                VALUES (?, ?, ?, ?, ?, ?)
                """, toSqlDateTime(row.createAt()), toSqlDateTime(row.updateAt()), row.title(), row.status(),
                row.note(), row.userId());
    }

    public Optional<RequestRow> findRequestById(long id) {
        return findById("SELECT * FROM request WHERE id = ?", PersistenceRowMappers.REQUEST, id);
    }

    public List<RequestRow> findAllRequests() {
        return jdbcTemplate.query("SELECT * FROM request ORDER BY id", PersistenceRowMappers.REQUEST);
    }

    public RequestRow findRequestByTk(String ticket){
        String query = """
                SELECT r.*
                FROM request r
                JOIN request_item ri ON ri.request_id = r.id
                WHERE ri.ticket = ?
                """;

        Optional<RequestRow> requestRowsOptional = jdbcTemplate.query(query, PersistenceRowMappers.REQUEST, ticket).stream().findFirst();
        if (!requestRowsOptional.isPresent()) {
            logger.debug("Nessuna richiesta trovata per ticket {}", ticket);
            return null;
        }

        return requestRowsOptional.get();
    }

    public List<RequestRow> findRequestInProgress(){
        String query = """
                SELECT r.*
                FROM request r
                LEFT JOIN pending_decision pd
                  ON pd.request_id = r.id
                WHERE r.status = 'IN_PROGRESS'
                AND pd.id IS NULL
                """;
        return jdbcTemplate.query(query, PersistenceRowMappers.REQUEST);
    }

    public boolean updateRequest(RequestRow row) {
        return jdbcTemplate.update("""
                        UPDATE request
                        SET update_at = ?, title = ?, status = ?, note = ?, user_id = ?
                        WHERE id = ?
                        """,
                toSqlDateTime(row.updateAt()), row.title(), row.status(),
                row.note(), row.userId(), row.id()) > 0;
    }

    public boolean deleteRequest(long id) {
        return jdbcTemplate.update("DELETE FROM request WHERE id = ?", id) > 0;
    }

    public long insertUserAccount(UserAccountRow row) {
        return insert("""
                INSERT INTO user_account (cognome, nome, codice_fiscale, email, utenza)
                VALUES (?, ?, ?, ?, ?)
                """, row.cognome(), row.nome(), row.codiceFiscale(), row.email(), row.utenza());
    }

    public Optional<UserAccountRow> findUserAccountById(long id) {
        return findById("SELECT * FROM user_account WHERE id = ?", PersistenceRowMappers.USER_ACCOUNT, id);
    }

    public List<UserAccountRow> findAllUserAccounts() {
        return jdbcTemplate.query("SELECT * FROM user_account ORDER BY id", PersistenceRowMappers.USER_ACCOUNT);
    }

    public Optional<UserAccountRow> findExistsUserAccount(UserAccountRow userAccountRow){
        String query = "SELECT u.* FROM user_account u WHERE u.cognome = ? AND u.nome = ?";

        return jdbcTemplate.query(query, PersistenceRowMappers.USER_ACCOUNT, userAccountRow.cognome(), userAccountRow.nome())
                .stream()
                .findFirst();
    }

    public boolean updateUserAccount(UserAccountRow row) {
        return jdbcTemplate.update("""
                        UPDATE user_account
                        SET cognome = ?, nome = ?, codice_fiscale = ?, email = ?, utenza = ?
                        WHERE id = ?
                        """,
                row.cognome(), row.nome(), row.codiceFiscale(), row.email(), row.utenza(), row.id()) > 0;
    }

    public boolean deleteUserAccount(long id) {
        return jdbcTemplate.update("DELETE FROM user_account WHERE id = ?", id) > 0;
    }

    public long insert(String sql, Object... values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            return statement;
        }, keyHolder);

        Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("Il database non ha restituito l'id generato per l'inserimento.");
        }
        logger.debug("Inserimento completato con id {}", generatedKey.longValue());
        return generatedKey.longValue();
    }

    private <T> Optional<T> findById(String sql, RowMapper<T> rowMapper, long id) {
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    private static String toSqlDateTime(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    private static Integer toSqlBoolean(Boolean value) {
        return value == null ? null : value ? 1 : 0;
    }

    private static Long nullableId(long id) {
        return id <= 0 ? null : id;
    }
}
