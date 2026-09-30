package it.requestassistant.application.util;

import it.requestassistant.domain.model.Message;
import it.requestassistant.domain.model.RequestItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageHelper {

    public static String extractTk(Message message) {
        String subject = message.getSubject();

        int start = subject.indexOf('[');
        int end = subject.indexOf(']', start);

        if (start == -1 || end == -1) {
            return null;
        }

        return subject.substring(start + 1, end);
    }

    public static String itemsInTable(String head, List<RequestItem> items, String foot) {
        List<RequestItem> validItems = new ArrayList<>();
        if (items != null) {
            for (RequestItem item : items) {
                if (item != null) {
                    validItems.add(item);
                }
            }
        }

        StringBuilder body = new StringBuilder();
        boolean hasHead = hasText(head);
        boolean hasFoot = hasText(foot);
        boolean hasItems = !validItems.isEmpty();

        if (hasHead) {
            body.append(head);
        }

        if (hasHead && (hasItems || hasFoot)) {
            appendBlankLine(body);
        }

        if (hasItems) {
            body.append(buildItemsTable(validItems));
        }

        if (hasFoot) {
            if (hasItems) {
                appendBlankLine(body);
            }
            body.append(foot);
        }

        return body.toString();
    }

    private static String buildItemsTable(List<RequestItem> items) {
        int typeWidth = "Type".length();
        int dettaglioWidth = "Dettaglio".length();
        int ambienteWidth = "ambiente".length();
        int notaWidth = "nota".length();
        int ticketWidth = "ticket".length();

        for (RequestItem item : items) {
            typeWidth = Math.max(typeWidth, safeText(item.getType() != null ? item.getType().name() : null).length());
            dettaglioWidth = Math.max(dettaglioWidth, safeText(item.getDettaglio()).length());
            ambienteWidth = Math.max(ambienteWidth, safeText(formatAmbiente(item.getAmbiente())).length());
            notaWidth = Math.max(notaWidth, safeText(item.getNota()).length());
            ticketWidth = Math.max(ticketWidth, safeText(item.getTicket()).length());
        }

        String rowFormat = "%-" + typeWidth + "s  %-" + dettaglioWidth + "s  %-" + ambienteWidth + "s  %-" + notaWidth + "s  %-" + ticketWidth + "s";
        StringBuilder table = new StringBuilder();

        table.append("         ")
                .append(String.format(Locale.ROOT, rowFormat, "Type", "Dettaglio", "ambiente", "nota", "ticket"))
                .append('\n');

        for (RequestItem item : items) {
            table.append(String.format(Locale.ROOT, rowFormat,
                            safeText(item.getType() != null ? item.getType().name() : null),
                            safeText(item.getDettaglio()),
                            safeText(formatAmbiente(item.getAmbiente())),
                            safeText(item.getNota()),
                            safeText(item.getTicket())))
                    .append('\n');
        }

        return table.toString();
    }

    private static String formatAmbiente(List<RequestItem.Ambiente> ambienti) {
        if (ambienti == null || ambienti.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (RequestItem.Ambiente ambiente : ambienti) {
            if (ambiente == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(ambiente.name());
        }
        return builder.toString();
    }

    private static void appendBlankLine(StringBuilder body) {
        if (!body.isEmpty() && body.charAt(body.length() - 1) != '\n') {
            body.append('\n');
        }
        body.append('\n');
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String safeText(String value) {
        return value != null ? value : "";
    }
}
