package it.requestassistant.adapters.persistence.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public final class PersistenceConverters {
    private PersistenceConverters() {}

    public static LocalDateTime toLocalDateTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    public static Boolean toBoolean01(Integer value) {
        if (value == null) return null;
        return value != 0;
    }

    public static Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    public static List<String> splitSemicolon(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return List.of(value.split(";"));
    }
}