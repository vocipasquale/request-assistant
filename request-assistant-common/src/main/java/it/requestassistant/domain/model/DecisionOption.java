package it.requestassistant.domain.model;

public record DecisionOption(
        long id,
        String action,
        double confidence,
        String reasons
) {
    public DecisionOption {

    }
}
