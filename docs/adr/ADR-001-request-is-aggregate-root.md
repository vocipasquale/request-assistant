Status: Accepted
Date: 2026-08-06

# Contesto

Il sistema deve gestire richieste di abilitazione che evolvono nel tempo.

Una richiesta può contenere:

una o più comunicazioni (email oggi, altri canali in futuro);
una o più richieste elementari (es. VPN, Oracle DEV, Oracle TEST, ...).

Era necessario individuare il punto in cui garantire la consistenza del dominio.

# Decisione

Request è l'Aggregate Root del dominio.

Tutte le modifiche al dominio devono passare attraverso Request.

Nessun altro oggetto può modificare direttamente lo stato della pratica.

# Motivazione

La pratica (Request) è il concetto centrale del dominio.

Le comunicazioni e le richieste elementari esistono solo nel contesto di una pratica.

Centralizzare le modifiche nell'Aggregate Root permette di preservare gli invarianti del dominio.

# Conseguenze
Message è immutabile.
RequestItem è immutabile.
Le collezioni esposte da Request sono in sola lettura.
Nessun client può modificare direttamente gli oggetti interni.

# Alternative valutate

Aggregate Root = Message

Scartata.

Una comunicazione è solo un evento della pratica.

Aggregate Root = RequestItem

Scartata.

Una pratica può contenere molte richieste elementari.