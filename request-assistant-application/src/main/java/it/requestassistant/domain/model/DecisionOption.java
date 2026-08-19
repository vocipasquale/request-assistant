package it.requestassistant.domain.model;

import java.util.List;

public record DecisionOption(
        String action,
        double confidence,
        List<String> reasons
) {
    public DecisionOption {
        reasons = List.copyOf(reasons);
    }
}
