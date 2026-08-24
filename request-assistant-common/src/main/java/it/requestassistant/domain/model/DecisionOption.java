package it.requestassistant.domain.model;

import java.util.List;

public record DecisionOption(
        String action,
        double confidence,
        String reasons
) {
    public DecisionOption {

    }
}
