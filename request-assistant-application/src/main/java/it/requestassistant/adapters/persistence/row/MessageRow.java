package it.requestassistant.adapters.persistence.row;

import java.time.LocalDateTime;

public record MessageRow(
        long id,
        Long requestId,
        String subject,
        String senderAddress,
        LocalDateTime receivedAt,
        String toAddress,
        String ccAddress,
        String bodyText,
        String entryId,
        String conversationId,
        String conversationTopic,
        Integer importance,
        Boolean hasAttachment,
        String category
) {}
