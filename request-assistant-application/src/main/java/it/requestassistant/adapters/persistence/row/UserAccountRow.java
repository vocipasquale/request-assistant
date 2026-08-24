package it.requestassistant.adapters.persistence.row;

public record UserAccountRow(
        long id,
        String cognome,
        String nome,
        String codiceFiscale,
        String email,
        String utenza
) {}
