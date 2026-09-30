--PRAGMA foreign_keys = ON;

CREATE TABLE user_account (
                              id INTEGER PRIMARY KEY,
                              cognome TEXT,
                              nome TEXT,
                              codice_fiscale TEXT,
                              email TEXT,
                              utenza TEXT
);

CREATE UNIQUE INDEX uk_user_account_codice_fiscale
    ON user_account(codice_fiscale);

CREATE UNIQUE INDEX uk_user_account_email
    ON user_account(email);

CREATE TABLE request (
                         id INTEGER PRIMARY KEY,
                         create_at TEXT,
                         update_at TEXT,
                         title TEXT,
                         status TEXT,
                         note TEXT,
                         user_id INTEGER NOT NULL UNIQUE,   -- 1-1 rigido
                         FOREIGN KEY (user_id) REFERENCES user_account(id)
                             ON UPDATE NO ACTION
                             ON DELETE NO ACTION
);

CREATE INDEX idx_request_status
    ON request(status);

CREATE TABLE request_item (
                              id INTEGER PRIMARY KEY,
                              request_id INTEGER,
                              type TEXT,
                              create_at TEXT,
                              update_at TEXT,
                              dettaglio TEXT,
                              nota TEXT,
                              ambiente TEXT,                     -- es: "SVILUPPO;COLLAUDO"
                              status TEXT,
                              ticket TEXT,
                              FOREIGN KEY (request_id) REFERENCES request(id)
                                  ON UPDATE NO ACTION
                                  ON DELETE NO ACTION
);

CREATE INDEX idx_request_item_request_id
    ON request_item(request_id);

CREATE INDEX idx_request_item_ticket
    ON request_item(ticket);

CREATE TABLE message (
                         id INTEGER PRIMARY KEY,
                         request_id INTEGER,
                         subject TEXT,
                         sender_address TEXT,
                         received_at TEXT,
                         to_address TEXT,
                         cc_address TEXT,
                         body_text TEXT,
                         entry_id TEXT,
                         conversation_id TEXT,
                         conversation_topic TEXT,
                         importance INTEGER,
                         has_attachment INTEGER,
                         category TEXT,
                         FOREIGN KEY (request_id) REFERENCES request(id)
                             ON UPDATE NO ACTION
                             ON DELETE NO ACTION
);

CREATE UNIQUE INDEX uk_message_entry_id
    ON message(entry_id);

CREATE INDEX idx_message_request_id
    ON message(request_id);

CREATE INDEX idx_message_conversation_id_received_at
    ON message(conversation_id, received_at);

CREATE TABLE pending_decision (
                                  id INTEGER PRIMARY KEY,
                                  created_at TEXT,
                                  type TEXT,
                                  target TEXT                         -- stringa come da tua correzione
);

CREATE TABLE decision_option (
                                 id INTEGER PRIMARY KEY,
                                 pending_decision_id INTEGER,
                                 action TEXT,
                                 confidence REAL,
                                 reasons TEXT,
                                 FOREIGN KEY (pending_decision_id) REFERENCES pending_decision(id)
                                     ON UPDATE NO ACTION
                                     ON DELETE CASCADE
);

CREATE INDEX idx_decision_option_pending_decision_id
    ON decision_option(pending_decision_id);