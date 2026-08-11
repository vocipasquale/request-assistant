# ADR-004 – PendingDecision per le decisioni dell'operatore

## Status

Accepted

## Contesto

Il processo di analisi lavora in background e in un momento diverso
dalla Dashboard.

Le proposte generate dall'Analysis Engine devono quindi essere persistite
per poter essere visualizzate e gestite successivamente dall'operatore.

Una decisione pendente può riguardare un `Message` oppure una `Request`.

## Decisione

Introduciamo `PendingDecision` nell'Application Layer.

Una `PendingDecision` contiene:

- `id`;
- `createdAt`;
- `type`;
- `target`;
- una o più `DecisionOption`.

Il `target` è tipizzato:

- `MESSAGE_CLASSIFICATION` → `MessageId`;
- `REQUEST_ANALYSIS` → `RequestId`.

`DecisionOption` contiene:

- `action`;
- `confidence`;
- `reasons`.

La presenza di una `PendingDecision` indica che esiste una decisione
che deve essere lavorata dall'operatore. Non viene introdotto uno stato.

La `PendingDecision` viene eliminata quando la decisione viene applicata
o annullata.

## Persistenza

La persistenza è responsabilità di `PendingDecisionRepository`, che
espone inizialmente le seguenti operazioni:

- `save`;
- `findById`;
- `findByType`;
- `delete`.

`PendingDecision` non viene referenziata da `Message` o `Request`.
È `PendingDecision` a mantenere il riferimento al proprio `target`.

## Conseguenze

La Dashboard può recuperare le decisioni pendenti in un momento
successivo rispetto all'analisi in background.

Il dominio (`Message`, `Request`, `RequestItem`) non deve conoscere
`PendingDecision`.

La logica di applicazione o annullamento della decisione rimane nei
rispettivi use case dell'Application Layer.
