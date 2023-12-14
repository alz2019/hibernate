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

INSERT INTO users(id, first_name, last_name) VALUES (1, 'Jack', 'Black'), (2, 'Miley', 'Lonergan');
INSERT INTO cards(id, number, user_id) VALUES (1, '1', 1);
INSERT INTO cards(id, number, user_id) VALUES (2, '2', 2);
