CREATE TABLE person (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(32)
);

CREATE INDEX idx_person_email ON person (email);
