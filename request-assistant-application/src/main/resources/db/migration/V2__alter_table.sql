--PRAGMA foreign_keys = ON;

ALTER TABLE pending_decision ADD COLUMN message_id INTEGER;
ALTER TABLE pending_decision ADD COLUMN request_id INTEGER;