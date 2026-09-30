package it.requestassistant.dashboard.util;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.Request;
import it.requestassistant.domain.model.RequestItem;
import it.requestassistant.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ActionPaneDraftHelper {

    private ActionPaneDraftHelper() {
    }

    public static String formatAmbienti(List<RequestItem.Ambiente> ambienti) {
        if (ambienti == null || ambienti.isEmpty()) {
            return "";
        }
        return ambienti.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    public static String safeText(String value) {
        return value != null ? value : "";
    }

    public static Message copyMessage(Message source) {
        Message target = new Message();
        if (source == null) {
            return target;
        }

        target.setId(source.getId());
        target.setSubject(source.getSubject());
        target.setSenderAddress(source.getSenderAddress());
        target.setReceivedAt(source.getReceivedAt());
        target.setTo(source.getTo());
        target.setCc(source.getCc());
        target.setBodyText(source.getBodyText());
        target.setEntryId(source.getEntryId());
        target.setConversationId(source.getConversationId());
        target.setConversationTopic(source.getConversationTopic());
        target.setImportance(source.getImportance());
        target.setHasAttachment(source.getHasAttachment());
        target.setCategory(source.getCategory());
        return target;
    }

    public static Request copyRequest(Request source) {
        Request target = new Request();
        if (source == null) {
            return target;
        }

        target.setId(source.getId());
        target.setCreateAt(source.getCreateAt());
        target.setUpdateAt(source.getUpdateAt());
        target.setTitle(source.getTitle());
        target.setStatus(source.getStatus());
        target.setUser(copyUser(source.getUser()));
        target.setNote(source.getNote());
        target.setItems(source.getItems() != null ? new ArrayList<>(source.getItems()) : new ArrayList<>());
        target.setMessages(source.getMessages() != null ? new ArrayList<>(source.getMessages()) : new ArrayList<>());
        return target;
    }

    public static User copyUser(User source) {
        User target = new User();
        if (source == null) {
            return target;
        }

        target.setCognome(source.getCognome());
        target.setNome(source.getNome());
        target.setCodiceFiscale(source.getCodiceFiscale());
        target.setEmail(source.getEmail());
        target.setUtenza(source.getUtenza());
        return target;
    }

    public static RequestItem copyRequestItem(RequestItem source) {
        RequestItem target = new RequestItem();
        if (source == null) {
            return target;
        }

        target.setId(source.getId());
        target.setType(source.getType());
        target.setCreateAt(source.getCreateAt());
        target.setUpdateAt(source.getUpdateAt());
        target.setDettaglio(source.getDettaglio());
        target.setNota(source.getNota());
        target.setAmbiente(source.getAmbiente() != null ? new ArrayList<>(source.getAmbiente()) : new ArrayList<>());
        target.setStatus(source.getStatus());
        target.setTicket(source.getTicket());
        return target;
    }

    public static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String rawValue, String fieldName) {
        String normalizedValue = normalizeEnumValue(rawValue);
        if (normalizedValue.isBlank()) {
            return null;
        }

        for (E enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(normalizedValue)) {
                return enumValue;
            }
        }

        throw new IllegalStateException("Valore non valido per " + fieldName + ": " + rawValue);
    }

    public static List<RequestItem.Ambiente> parseAmbienti(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return new ArrayList<>();
        }

        List<RequestItem.Ambiente> ambienti = new ArrayList<>();
        for (String token : rawValue.split(",")) {
            String normalizedValue = normalizeEnumValue(token);
            if (!normalizedValue.isBlank()) {
                ambienti.add(parseEnum(RequestItem.Ambiente.class, normalizedValue, "ambiente request item"));
            }
        }
        return ambienti;
    }

    public static String normalizeEnumValue(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace(' ', '_')
                .replace('-', '_')
                .toUpperCase();
    }
}

