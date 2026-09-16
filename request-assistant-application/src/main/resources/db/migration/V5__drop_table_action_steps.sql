--PRAGMA foreign_keys = ON;
DROP TABLE action_step;

DROP TABLE action;

CREATE TABLE action (
    id INTEGER PRIMARY KEY,
    title TEXT,
    ai_response TEXT
);
