package it.requestassistant.adapters.persistence.mapper;

import it.requestassistant.adapters.persistence.row.DecisionOptionRow;
import it.requestassistant.adapters.persistence.row.MessageRow;
import it.requestassistant.adapters.persistence.row.PendingDecisionRow;
import it.requestassistant.adapters.persistence.row.RequestItemRow;
import it.requestassistant.adapters.persistence.row.RequestRow;
import it.requestassistant.adapters.persistence.row.UserAccountRow;
import it.requestassistant.domain.model.Action;
import it.requestassistant.adapters.persistence.row.ActionRow;
import it.requestassistant.adapters.persistence.row.ActionStepRow;
import it.requestassistant.domain.model.DecisionOption;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.PendingDecision;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class PersistenceDomainMappers {

    public static Logger logger = LoggerFactory.getLogger(PersistenceDomainMappers.class);

    private PersistenceDomainMappers() {
    }

    public static Message toDomain(MessageRow row) {
        return new Message(
                row.subject(),
                row.senderAddress(),
                row.receivedAt(),
                row.toAddress(),
                row.ccAddress(),
                row.bodyText(),
                row.entryId(),
                row.conversationId(),
                row.conversationTopic(),
                row.importance(),
                row.hasAttachment(),
                row.category()
        );
    }

    public static User toDomain(UserAccountRow row) {
        User user = new User();
        user.setCognome(row.cognome());
        user.setNome(row.nome());
        user.setCodiceFiscale(row.codiceFiscale());
        user.setEmail(row.email());
        user.setUtenza(row.utenza());
        return user;
    }

    public static RequestItem toDomain(RequestItemRow row) {
        RequestItem item = new RequestItem();
        item.setType(parseEnum(RequestItem.Type.class, row.type()));
        item.setCreateAt(row.createAt());
        item.setUpdateAt(row.updateAt());
        item.setDettaglio(row.dettaglio());
        item.setNota(row.nota());
        item.setStatus(parseEnum(RequestItem.Status.class, row.status()));
        item.setTicket(row.ticket());

        List<RequestItem.Ambiente> ambienti = row.ambienteList().stream()
                .map(value -> parseEnum(RequestItem.Ambiente.class, value))
                .filter(Objects::nonNull)
                .toList();
        item.setAmbiente(ambienti);

        return item;
    }

    public static Request toDomain(RequestRow requestRow, UserAccountRow userRow, List<RequestItemRow> requestItemRows,
            List<MessageRow> messageRows
    ) {
        if(Objects.isNull(requestRow)){
            return null;
        }

        Request request = new Request();
        request.setId(requestRow.id());
        request.setCreateAt(requestRow.createAt());
        request.setUpdateAt(requestRow.updateAt());
        request.setTitle(requestRow.title());
        request.setStatus(parseEnum(Request.Status.class, requestRow.status()));
        request.setNote(requestRow.note());
        request.setUser(userRow == null ? null : toDomain(userRow));

        List<RequestItem> items = requestItemRows == null
                ? Collections.emptyList()
                : requestItemRows.stream().map(PersistenceDomainMappers::toDomain).toList();
        request.setItems(items);

        List<Message> messages = messageRows == null
                ? Collections.emptyList()
                : messageRows.stream().map(PersistenceDomainMappers::toDomain).toList();
        request.setMessages(messages);

        return request;
    }

    public static DecisionOption toDomain(DecisionOptionRow row, ActionRow actionRow, List<ActionStepRow> actionStepRows) {
        return new DecisionOption(
                row.id(),
                PersistenceDomainMappers.toDomain(actionRow, actionStepRows),
                row.confidence() == null ? 0.0d : row.confidence(),
                row.reasons()
        );
    }

    public static Action toDaman(ActionRow row, List<String> steps){
        return new Action(row.title(), steps);
    }

    public static String toDomain(ActionStepRow row){
        return  row.stepDescription();
    }

    public static PendingDecision toDomain(PendingDecisionRow row, List<DecisionOptionRow> optionRows,
                                           MessageRow messageRow, RequestRow requestRow, List<RequestItemRow> requestItemRowList,
                                           UserAccountRow userAccountRow, List<MessageRow> messagesRequestRowList,
                                           ActionRow actionRow, List<ActionStepRow> actionStepRows) {


        List<DecisionOption> options = new ArrayList<>(optionRows == null
                ? Collections.emptyList()
                : optionRows.stream()
                .map(optionRow ->
                        PersistenceDomainMappers.toDomain(optionRow, actionRow, actionStepRows)).toList());


        logger.debug("Mapping PendingDecisionRow to PendingDecision with {} options", options.size());

        Message message = Objects.isNull(messageRow)
            ? null
            : PersistenceDomainMappers.toDomain(messageRow);
        logger.debug("Mapping message id {} for  PendingDecision id {}", message.entryId(), row.id());

        Request request = Objects.isNull(PersistenceDomainMappers.toDomain(requestRow, userAccountRow, requestItemRowList, messagesRequestRowList))
                ? null
                : PersistenceDomainMappers.toDomain(requestRow, userAccountRow, requestItemRowList, messagesRequestRowList);
        logger.debug("Mapping request for  PendingDecision id {}", row.id());

        return new PendingDecision(
                row.id(),
                row.createdAt(),
                parseEnum(PendingDecision.Type.class, row.type()),
                row.target(),
                options,
                message,
                request
        );
    }

    public static <T extends Enum<T>> T parseEnum(Class<T> enumClass, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return Enum.valueOf(enumClass, rawValue.trim().toUpperCase(Locale.ROOT));
    }

    // ============ Domain -> Row Mapping ============

    public static MessageRow toRow(Message message) {
        return new MessageRow(
                0L,    // id generato dal DB
                0L,  // requestId da settare se necessario
                message.subject(),
                message.senderAddress(),
                message.receivedAt(),
                message.to(),
                message.cc(),
                message.bodyText(),
                message.entryId(),
                message.conversationId(),
                message.conversationTopic(),
                message.importance(),
                message.hasAttachment(),
                message.category()
        );
    }

    public static UserAccountRow toRow(User user) {
        return new UserAccountRow(
                0L,    // id generato dal DB
                user.getCognome(),
                user.getNome(),
                user.getCodiceFiscale(),
                user.getEmail(),
                user.getUtenza()
        );
    }

    public static RequestItemRow toRow(RequestItem item) {
        String ambienteRaw = null;
        if (item.getAmbiente() != null && !item.getAmbiente().isEmpty()) {
            ambienteRaw = item.getAmbiente().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.joining(";"));
        }
        return new RequestItemRow(
                0L,    // id generato dal DB
                null,  // requestId da settare se necessario
                item.getType() != null ? item.getType().name() : null,
                item.getCreateAt(),
                item.getUpdateAt(),
                item.getDettaglio(),
                item.getNota(),
                ambienteRaw,
                item.getStatus() != null ? item.getStatus().name() : null,
                item.getTicket()
        );
    }

    public static RequestRow toRow(Request request) {
        return new RequestRow(
                request.getId(),
                request.getCreateAt(),
                request.getUpdateAt(),
                request.getTitle(),
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getNote(),
                request.getUser() != null ? toRow(request.getUser()).id() : 0L
        );
    }

    public static DecisionOptionRow toRow(DecisionOption option, long pendingDecisionId, long actionId) {
        return new DecisionOptionRow(
                0L,    // id generato dal DB in INSERT
                pendingDecisionId,
                actionId,
                option.confidence(),
                option.reasons()
        );
    }

    public static PendingDecisionRow toRow(PendingDecision pendingDecision) {
        //usato SOLO dalla INSERT
        return new PendingDecisionRow(
                0L, // id generato dal DB
                pendingDecision.getCreatedAt(),
                pendingDecision.getType() != null ? pendingDecision.getType().name() : null,
                pendingDecision.getTarget(),
                0L,
                Objects.isNull(pendingDecision.getRequest())?0:pendingDecision.getRequest().getId()
        );
    }

    public static Action toDomain(ActionRow row, List<ActionStepRow> stepRows) {
        if (row == null) {
            return null;
        }
        List<String> steps = stepRows == null
                ? Collections.emptyList()
                : stepRows.stream().map(ActionStepRow::stepDescription).toList();
        return new Action(row.title(), steps);
    }

    public static ActionRow toRow(Action action) {
        return new ActionRow(0L, action.title());
    }

    public static ActionStepRow toRow(long actionId, String stepDescription) {
        return new ActionStepRow(
                actionId,
                stepDescription
        );
    }
}
