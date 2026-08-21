package it.requestassistant.adapters.persistence.row;

public record DecisionOptionRow(
        long id,
        Long pendingDecisionId,
        String action,
        Double confidence,
        String reasons
) {}