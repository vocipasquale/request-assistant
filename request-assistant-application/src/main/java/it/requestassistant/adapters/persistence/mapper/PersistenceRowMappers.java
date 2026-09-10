package it.requestassistant.adapters.persistence.mapper;

import it.requestassistant.adapters.persistence.row.*;
import it.requestassistant.adapters.persistence.util.PersistenceConverters;
import org.springframework.jdbc.core.RowMapper;

public final class PersistenceRowMappers {
    private PersistenceRowMappers() {}

    public static final RowMapper<UserAccountRow> USER_ACCOUNT = (rs, n) -> new UserAccountRow(
            rs.getLong("id"),
            rs.getString("cognome"),
            rs.getString("nome"),
            rs.getString("codice_fiscale"),
            rs.getString("email"),
            rs.getString("utenza")
    );

    public static final RowMapper<RequestRow> REQUEST = (rs, n) -> new RequestRow(
            rs.getLong("id"),
            PersistenceConverters.toLocalDateTime(rs.getString("create_at")),
            PersistenceConverters.toLocalDateTime(rs.getString("update_at")),
            rs.getString("title"),
            rs.getString("status"),
            rs.getString("note"),
            rs.getLong("user_id")
    );

    public static final RowMapper<RequestItemRow> REQUEST_ITEM = (rs, n) -> new RequestItemRow(
            rs.getLong("id"),
            PersistenceConverters.getNullableLong(rs, "request_id"),
            rs.getString("type"),
            PersistenceConverters.toLocalDateTime(rs.getString("create_at")),
            PersistenceConverters.toLocalDateTime(rs.getString("update_at")),
            rs.getString("dettaglio"),
            rs.getString("nota"),
            rs.getString("ambiente"),
            rs.getString("status"),
            rs.getString("ticket")
    );

    public static final RowMapper<MessageRow> MESSAGE = (rs, n) -> new MessageRow(
            rs.getLong("id"),
            PersistenceConverters.getNullableLong(rs, "request_id"),
            rs.getString("subject"),
            rs.getString("sender_address"),
            PersistenceConverters.toLocalDateTime(rs.getString("received_at")),
            rs.getString("to_address"),
            rs.getString("cc_address"),
            rs.getString("body_text"),
            rs.getString("entry_id"),
            rs.getString("conversation_id"),
            rs.getString("conversation_topic"),
            (Integer) rs.getObject("importance"),
            PersistenceConverters.toBoolean01((Integer) rs.getObject("has_attachment")),
            rs.getString("category")
    );

    public static final RowMapper<PendingDecisionRow> PENDING_DECISION = (rs, n) -> new PendingDecisionRow(
            rs.getLong("id"),
            PersistenceConverters.toLocalDateTime(rs.getString("created_at")),
            rs.getString("type"),
            rs.getString("target"),
            rs.getLong("message_id"),
            rs.getLong("request_id")
    );

    public static final RowMapper<DecisionOptionRow> DECISION_OPTION = (rs, n) -> new DecisionOptionRow(
            rs.getLong("id"),
            PersistenceConverters.getNullableLong(rs, "pending_decision_id"),
            rs.getLong("action_id"),
            (Double) rs.getObject("confidence"),
            rs.getString("reasons")
    );

    public static final RowMapper<ActionRow> ACTION = (rs, n) -> new ActionRow(
            rs.getLong("id"),
            rs.getString("title")
    );

    public static final RowMapper<ActionStepRow> ACTION_STEP = (rs, n) -> new ActionStepRow(
            rs.getLong("action_id"),
            rs.getString("step_description")
    );
}