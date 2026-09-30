--PRAGMA foreign_keys = ON;
DROP TABLE request;

CREATE TABLE request (
                         id INTEGER PRIMARY KEY,
                         create_at TEXT,
                         update_at TEXT,
                         title TEXT,
                         status TEXT,
                         note TEXT,
                         user_id INTEGER NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES user_account(id)
                             ON UPDATE NO ACTION
                             ON DELETE NO ACTION
);