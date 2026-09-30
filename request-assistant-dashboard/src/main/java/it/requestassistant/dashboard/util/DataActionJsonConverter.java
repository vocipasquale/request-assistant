package it.requestassistant.dashboard.util;

import it.requestassistant.domain.model.DataAction;
import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class DataActionJsonConverter {

    private static final Logger logger = LoggerFactory.getLogger(DataActionJsonConverter.class);
    private static final JsonParser JSON_PARSER = JsonParserFactory.getJsonParser();

    private DataActionJsonConverter() {
    }

    public static DataAction fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        logger.info("Conversione JSON: {}",json);

        try {
            Map<String, Object> root = JSON_PARSER.parseMap(json);
            Message message = toMessage(asMap(root.get("message")));
            Request request = toRequest(asMap(root.get("request")));
            return new DataAction(message, request);
        } catch (IllegalArgumentException e) {
            logger.error("Errore durante la conversione del JSON in DataAction", e);
            throw new IllegalArgumentException("Impossibile convertire il JSON in DataAction", e);
        }
    }

    private static Message toMessage(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        Message message = new Message();
     //   message.setId(asLong(source.get("id")));
        message.setSubject(asString(source.get("subject")));
        message.setSenderAddress(asString(source.get("senderAddress")));
     //   message.setReceivedAt(asLocalDateTime(source.get("receivedAt")));
        message.setTo(asString(source.get("to")));
        message.setCc(asString(source.get("cc")));
        message.setBodyText(asString(source.get("bodyText")));
    //    message.setEntryId(asString(source.get("entryId")));
    //    message.setConversationId(asString(source.get("conversationId")));
    //    message.setConversationTopic(asString(source.get("conversationTopic")));
    //    message.setImportance(asInteger(source.get("importance")));
    //    message.setHasAttachment(asBoolean(source.get("hasAttachment")));
    //    message.setCategory(asString(source.get("category")));
        return message;
    }

    private static Request toRequest(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        Request request = new Request();
     //   request.setId(asLong(source.get("id")));
     //   request.setCreateAt(asLocalDateTime(source.get("createAt")));
     //   request.setUpdateAt(asLocalDateTime(source.get("updateAt")));
        request.setTitle(asString(source.get("title")));
        request.setStatus(asEnum(Request.Status.class, source.get("status")));
        request.setUser(toUser(asMap(source.get("user"))));
        request.setNote(asString(source.get("note")));
        request.setItems(asList(source.get("items")).stream()
                .map(DataActionJsonConverter::asMap)
                .map(DataActionJsonConverter::toRequestItem)
                .toList());
        request.setMessages(asList(source.get("messages")).stream()
                .map(DataActionJsonConverter::asMap)
                .map(DataActionJsonConverter::toMessage)
                .toList());
        return request;
    }

    private static User toUser(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        User user = new User();
        user.setCognome(asString(source.get("cognome")));
        user.setNome(asString(source.get("nome")));
        user.setCodiceFiscale(asString(source.get("codiceFiscale")));
        user.setEmail(asString(source.get("email")));
        user.setUtenza(asString(source.get("utenza")));
        return user;
    }

    private static RequestItem toRequestItem(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        RequestItem requestItem = new RequestItem();
    //    requestItem.setId(asLong(source.get("id")));
        requestItem.setType(asEnum(RequestItem.Type.class, source.get("type")));
    //    requestItem.setCreateAt(asLocalDateTime(source.get("createAt")));
    //    requestItem.setUpdateAt(asLocalDateTime(source.get("updateAt")));
        requestItem.setDettaglio(asString(source.get("dettaglio")));
        requestItem.setNota(asString(source.get("nota")));
        requestItem.setAmbiente(asList(source.get("ambiente")).stream()
                .map(value -> asEnum(RequestItem.Ambiente.class, value))
                .toList());
        requestItem.setStatus(asEnum(RequestItem.Status.class, source.get("status")));
        requestItem.setTicket(asString(source.get("ticket")));
        return requestItem;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        return value instanceof List<?> list ? (List<Object>) list : List.of();
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static long asLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private static Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.valueOf(String.valueOf(value));
    }

    private static LocalDateTime asLocalDateTime(Object value) {
        String raw = asString(value);
        return raw == null || raw.isBlank() ? null : LocalDateTime.parse(raw);
    }

    private static <T extends Enum<T>> T asEnum(Class<T> enumClass, Object value) {
        String raw = asString(value);
        return raw == null || raw.isBlank() ? null : Enum.valueOf(enumClass, raw.trim());
    }
}

