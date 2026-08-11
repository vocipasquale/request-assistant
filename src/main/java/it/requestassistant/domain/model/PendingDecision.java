package it.requestassistant.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record PendingDecision(
        long id,
        LocalDateTime createdAt,
        Type type,
        Target target,
        List<DecisionOption> options
) {
    public PendingDecision {
        Objects.requireNonNull(createdAt);
        Objects.requireNonNull(type);
        Objects.requireNonNull(target);
        options = List.copyOf(options);
    }

    public enum Type {
        MESSAGE_CLASSIFICATION,
        REQUEST_ANALYSIS
    }

    public sealed interface Target permits MessageTarget, RequestTarget {
        long id();
    }

    public record MessageTarget(long id) implements Target {
    }

    public record RequestTarget(long id) implements Target {
    }
}
