# ADR-005 – Refactoring

## Status

Accepted

## Contesto

In questa fase del progetto mi sono reso conto che l'organizzazione originale non è più sufficiente.
Mi ritrovo spesso a riciclare sulle decisioni prese in passato è ho molti dubbi sul pattern esagonale.

## Decisione

Devo riorganizzare il codice in moduli Maven in modo da definire il perimetro delle "componenti" del progetto **Request Assistant**.

## Conseguenze

D'ora in avanti il progetto sarà strutturato nel seguente modo:

- **request-assistant**: modulo Maven principale (parent)
- **request-assistant-application**: modulo Maven che contiene la business logic dell'applicazione (paradigma esagonale)
- **request-assistant-batch**: modulo Maven relativo al processo batch (playground) orchestratore
- **request-assistant-dashboard**: modulo Maven relativo alla dashboard grafica (web? Swing? ... ancora non so...)
- **request-assistant-common**: modulo Maven che contiene i componenti (classi java) comuni tra i vari moduli
- **request-assistant-runner**: modulo Maven dedicato al packaging e all'avvio dell'applicazione 