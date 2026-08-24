# ADR-006 – Dashboard: framework e pattern

## Status

Accepted

## Contesto

La dashboard è l'interfaccia grafica che comunica con l'applicazione (it.requestassistant.application.port.in) per
- mostrare all'utente le "decisioni pendenti" e consentirgli di validarle o modificarle
- gestire il batch (start/stop/timing/logging) attraverso una porta che sarà definita in it.requestassistant.application.port.in
- eseguire ricerche sulle richieste evase e mostrare il risultato

La dashboard fa parte della stessa applicazione e all'interno del codice della dashboard deve essere possibile richiamare i bean del contesto Spring del modulo **request-assistant-application**.
Il codice sorgente deve essere ordinato e separato, nel senso che la parte relativa alla grafica deve essere separata dalla logica di business. 
Inoltre, deve essere possibile in un futuro aggiungere, modificare o eliminare grafica senza avere impatti strutturali sul resto dell'applicazione.

A prescindere dalla soluzione individuata il pattern esagonale con cui e' stata sviluppata l'applicazione non deve essere compromesso.

## Decisione

La dashboard viene sviluppata nel modulo **request-assistant-dashboard**.
Il framework è **JavaFX** con l'ausilio di **FxWeaver**.
Il pattern di sviluppo è **MVVM**.

Esempio:

- La View (FXML + Controller JavaFX): 
  - Il file FXML definisce il layout. Il Controller JavaFX (annotato con @Component e @FxmlView) funge da puro "passacarte": riceve i riferimenti ai componenti grafici ed effettua il binding (collegamento) delle loro proprietà con quelle del ViewModel.
- Il ViewModel (Bean di Spring):
  - Una classe contrassegnata con @Component che rappresenta lo stato della UI (es. usando StringProperty, ListProperty o BooleanProperty). Questa classe non sa nulla degli elementi grafici di JavaFX (bottoni, tabelle). 
- Il Model (Servizi Spring Boot): 
  - Il ViewModel può iniettare solo la porta in ingresso dell’applicazione (DashboardPort) e non i servizi Spring standard (es. @Service per la logica di business). Il ViewModel interroga la porta in ingresso quando l'utente compie un'azione.

## Conseguenze

Grazie ai framework **JavaFX** e **FxWeaver** e al pattern **MVVM** la dashboard sarà estremamente ordinata: se domani vorrò cambiare l'aspetto visivo di una scheda, modificherei solo il file FXML e il Controller, senza toccare una singola riga di logica di business.
