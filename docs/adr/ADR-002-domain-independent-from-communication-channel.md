# Contesto

Le pratiche oggi vengono gestite tramite email Outlook.
In futuro potrebbero arrivare da altri canali (chat, ticket, file, API).

# Decisione

Il dominio non conosce Outlook né alcun altro canale specifico.
Le comunicazioni sono rappresentate dall'astrazione Message.
I canali sono responsabilità degli adapter.

# Conseguenze

MailMessage appartiene agli adapter come tecnologia di acquisizione, oppure al dominio come specializzazione di Message (questo lo definiremo meglio quando introdurremo altri canali).
Request gestisce solo Message.
Gli use case non dipendono da Outlook.