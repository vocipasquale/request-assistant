package it.requestassistant.adapters.persistence.row;

import java.time.LocalDateTime;

public record PendingDecisionRow(
        long id,
        LocalDateTime createdAt,
        String type,
        String target
) {}
