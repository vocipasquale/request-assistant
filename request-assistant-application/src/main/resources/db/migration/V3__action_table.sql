--PRAGMA foreign_keys = ON;

CREATE TABLE action (
    id INTEGER PRIMARY KEY,
    description TEXT
);

CREATE TABLE action_step (
    action_id INTEGER,
    step_description TEXT
);

DROP TABLE decision_option;

CREATE TABLE decision_option (
                                 id INTEGER PRIMARY KEY,
                                 pending_decision_id INTEGER,
                                 action_id INTEGER,
                                 confidence REAL,
                                 reasons TEXT,
                                 FOREIGN KEY (pending_decision_id) REFERENCES pending_decision(id)
                                     ON UPDATE NO ACTION
                                     ON DELETE CASCADE
);

CREATE INDEX idx_decision_option_pending_decision_id
    ON decision_option(pending_decision_id);