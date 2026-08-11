# ADR-003 – AI as Decision Support

## Status

Accepted

## Context

RequestAssistant supporta l'operatore nella gestione delle richieste ricevute tramite messaggi (inizialmente e-mail Outlook).

Le richieste possono evolvere nel tempo attraverso molteplici comunicazioni e non sempre è possibile determinarne automaticamente il significato o l'appartenenza ad una Request esistente.

L'obiettivo del sistema non è automatizzare le decisioni dell'operatore, ma ridurne il carico cognitivo, proponendo possibili interpretazioni e azioni.

## Decision

RequestAssistant adotta un modello di **Decision Support**.

L'Analysis Engine analizza il contenuto dei messaggi e produce **proposte**, senza modificare direttamente il dominio.

Le principali attività supportate sono:

* classificazione del `Message`;
* proposta di associazione ad una `Request` esistente oppure creazione di una nuova `Request`;
* generazione di una `ActionProposal`.

Ogni proposta deve essere accompagnata da:

* un valore di `confidence`;
* una lista di `reasons` che ne spieghino la motivazione.

La decisione finale spetta sempre all'operatore.

## Consequences

L'Analysis Engine è un componente di supporto e non costituisce il centro del dominio.

Il dominio viene modificato esclusivamente a seguito di una decisione esplicita dell'operatore.

La Dashboard presenta le proposte generate dal sistema, consentendone la conferma o la modifica prima dell'applicazione.

L'Analysis Engine è considerato un dettaglio implementativo e può essere realizzato mediante differenti tecnologie (LLM, motori a regole o altre soluzioni) senza impattare il modello di dominio.
