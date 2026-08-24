# ADR-007 – Dashboard: requisiti

## Status

Accepted

## Requisiti generali

Sulla base di quanto riportato in ADR-006, la dashboard deve essere sviluppata nel modulo **request-assistant-dashboard**.

La dashboard è l'interfaccia grafica che comunica con l'applicazione (it.requestassistant.application.port.in) per
- mostrare all'utente le "decisioni pendenti" e consentirgli di validarle o modificarle
- gestire il batch (start/stop/timing/logging)
- eseguire ricerche sulle richieste evase e mostrare il risultato

La dashboard deve mostrare:
- il titolo: Request Assistant
- le classiche tre icone in alto a destra: "Riduci a icona", "Ingrandisci" e "Chiudi"
- sotto il titolo una barra dei menu con, al momento, tre voci: "In progress", "Batch" e "Archive"
- la selezione di ogni voce di menu mostra una sottofinestra (inclusa) indipendente dalle altre (per esempio: tab, pannelli centrali sostituibili)

