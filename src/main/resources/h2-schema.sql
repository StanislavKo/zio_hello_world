CREATE TABLE IF NOT EXISTS webhook(
    id bigint NOT NULL PRIMARY KEY,
    url VARCHAR(1024),
    topic VARCHAR(256),
    format VARCHAR(32),
    volume VARCHAR(32)
);