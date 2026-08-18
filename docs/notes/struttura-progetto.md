# Struttura del progetto

├───data: contiene il file **SQLite** del database

├───docs

│   ├───adr: file .md che documentano le decisioni architetturali del progetto in ordine di tempo e versione

│   └───notes: altri file .md con informazioni

├───lib: al momento contiene le librerie jacob che non è stato possibile definire come dipendenze maven

├───src

│   ├───main

│   │   ├───java

│   │   │   └───it

│   │   │       └───requestassistant: package applicativo

│   │   │           ├───adapters: contiene le implementazioni delle "firme" di application.port.*"

│   │   │           │   └───outlook: implementazione specifica di MailReader e MailWriter

│   │   │           ├───application

│   │   │           │   └───port

│   │   │           │       └───out: firme (interfacce) dei servizi esterni utilizzati dall'application layer. Flusso: Applicazione --> Esterno

│   │   │           │       └───in:  firme (interfacce) che definiscono le operazioni che l'esterno può chiedere all'applicazione. Flusso: Esterno --> Applicazione

│   │   │           ├───domain

│   │   │           │   └───model: oggetti del dominio. Devono essere indipendenti da servizi esterni (Outlook, Jacob, etc...)

│   │   │           ├───playground: contiene il/i processo/i che gira/girano in background

│   │   │           └───poc: poc di origine

│   │   └───resources

│   │       └───db

│   │           └───migration: file .sql usati da SQLite

│   └───test

│       └───java: test junit

