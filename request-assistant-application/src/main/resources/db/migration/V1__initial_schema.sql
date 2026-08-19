/*

| Java                  | SQLite consigliato | Note                                                |
| --------------------- | ------------------ | --------------------------------------------------- |
| `byte` / `Byte`       | `INTEGER`          | normalmente non serve una colonna specifica         |
| `short` / `Short`     | `INTEGER`          | idem                                                |
| `int` / `Integer`     | `INTEGER`          |                                                     |
| `long` / `Long`       | `INTEGER`          | scelta naturale                                     |
| `BigInteger`          | `TEXT`             | oppure `BLOB` se hai esigenze particolari           |
| `float` / `Float`     | `REAL`             | precisione limitata                                 |
| `double` / `Double`   | `REAL`             |                                                     |
| `BigDecimal`          | `TEXT`             | **consigliato** se serve precisione decimale esatta |
| `boolean` / `Boolean` | `INTEGER`          | convenzione `0 = false`, `1 = true`                 |
| `char` / `Character`  | `TEXT`             | normalmente `TEXT` con un carattere                 |
| `String`              | `TEXT`             | scelta naturale                                     |
| `byte[]`              | `BLOB`             |                                                     |
| `UUID`                | `TEXT`             | tipicamente stringa UUID                            |
| `Enum`                | `TEXT`             | normalmente nome dell'enum                          |
| `Date`                | `TEXT` / `INTEGER` | dipende dalla strategia temporale                   |
| `java.sql.Date`       | `TEXT`             | `YYYY-MM-DD`                                        |
| `java.sql.Timestamp`  | `TEXT`             | ISO-8601                                            |
| `LocalDate`           | `TEXT`             | `YYYY-MM-DD`                                        |
| `LocalTime`           | `TEXT`             | `HH:mm:ss...`                                       |
| `LocalDateTime`       | `TEXT`             | ISO-8601                                            |
| `Instant`             | `TEXT`             | ISO-8601 UTC                                        |
| `OffsetDateTime`      | `TEXT`             | ISO-8601 con offset                                 |
| `ZonedDateTime`       | `TEXT`             | ISO-8601, ma attenzione alla gestione dello ZoneId  |
| `Duration`            | `INTEGER`          | ad esempio millisecondi/nanosecondi                 |
| `byte[]`              | `BLOB`             | dati binari                                         |
| `BigDecimal`          | `TEXT`             | per evitare perdita di precisione                   |
| `Object`              | `TEXT` / `BLOB`    | da evitare come tipo persistente generico           |
| JSON                  | `TEXT`             | SQLite non necessita di un tipo JSON dedicato       |

 */



CREATE TABLE message (
    subject TEXT,
    sender_address TEXT,
    received_at TEXT,
    to_address TEXT,
    cc_address TEXT,
    body_text TEXT,
    entry_id TEXT NOT NULL,
    conversation_id TEXT,
    conversation_topic TEXT,
    importance INTEGER,
    hasAttachment INTEGER,
    category TEXT
);


CREATE UNIQUE INDEX uk_message_entry_id
    ON message(entry_id);
