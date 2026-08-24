package it.requestassistant.adapters.persistence.row;

import java.time.LocalDateTime;

public record RequestRow(
        long id,
        LocalDateTime createAt,
        LocalDateTime updateAt,
        String title,
        String status,
        String note,
        long userId
) {}
