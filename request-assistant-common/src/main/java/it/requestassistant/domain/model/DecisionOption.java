package it.requestassistant.domain.model;

import java.util.Objects;

public record DecisionOption(
        long id,
        Action action,
        double confidence,
        String reasons
) {
    public DecisionOption {
        Objects.requireNonNull(action);
    }
}
