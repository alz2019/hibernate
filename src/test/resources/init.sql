CREATE TABLE users
(
    id         BIGINT PRIMARY KEY,
    first_name VARCHAR(32),
    last_name  VARCHAR(32)
);

CREATE TABLE cards
(
    id        BIGINT PRIMARY KEY,
    number      VARCHAR(128),
    user_id BIGINT REFERENCES users
);