package it.requestassistant.adapters.persistence.dao;

import it.requestassistant.adapters.persistence.mapper.PersistenceDomainMappers;
import it.requestassistant.adapters.persistence.mapper.PersistenceRowMappers;
import it.requestassistant.adapters.persistence.row.MessageRow;
import it.requestassistant.adapters.persistence.row.RequestItemRow;
import it.requestassistant.adapters.persistence.row.RequestRow;
import it.requestassistant.adapters.persistence.row.UserAccountRow;
import it.requestassistant.application.port.out.PersistenceDaoPort;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
    public void insertUserAccount(User user) {
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

    @Override
    public void insertRequest(Request request) {
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
    }

    @Override
    public void insertRequestItem(RequestItem requestItem) {
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

    @Override
    public void insertMessage(Message message) {
        String query = """
                INSERT INTO message (request_id, subject, sender_address, received_at, to_address, cc_address, body_text, entry_id, conversation_id, conversation_topic, importance, has_attachment, category)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        MessageRow messageRow = PersistenceDomainMappers.toRow(message);

        jdbcTemplate.update(query,
                messageRow.requestId(),
                messageRow.subject(),
                messageRow.senderAddress(),
                messageRow.receivedAt() != null ? messageRow.receivedAt().toString() : null,
                messageRow.toAddress(),
                messageRow.ccAddress(),
                messageRow.bodyText(),
                messageRow.entryId(),
                messageRow.conversationId(),
                messageRow.conversationTopic(),
                messageRow.importance(),
                messageRow.hasAttachment() != null ? (messageRow.hasAttachment() ? 1 : 0) : null,
                messageRow.category()
        );

    }
}
