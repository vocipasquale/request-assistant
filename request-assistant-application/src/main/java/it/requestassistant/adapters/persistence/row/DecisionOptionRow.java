package it.requestassistant.adapters.persistence.row;

public record DecisionOptionRow(
        long id,
        Long pendingDecisionId,
        long actionId,
        Double confidence,
        String reasons
) {}