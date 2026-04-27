-- HotelNova Database Schema

CREATE TABLE IF NOT EXISTS users (
    id         SERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN','RECEPTIONIST')),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS guests (
    id           SERIAL PRIMARY KEY,
    document_id  VARCHAR(20)  NOT NULL UNIQUE,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(150),
    phone        VARCHAR(20),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
    id           SERIAL PRIMARY KEY,
    room_number  VARCHAR(10)  NOT NULL UNIQUE,
    room_type    VARCHAR(30)  NOT NULL CHECK (room_type IN ('SINGLE','DOUBLE','SUITE','PRESIDENTIAL')),
    price_per_night DECIMAL(10,2) NOT NULL,
    capacity     INTEGER      NOT NULL DEFAULT 1,
    available    BOOLEAN      NOT NULL DEFAULT TRUE,
    description  TEXT,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id           SERIAL PRIMARY KEY,
    guest_id     INTEGER      NOT NULL REFERENCES guests(id),
    room_id      INTEGER      NOT NULL REFERENCES rooms(id),
    user_id      INTEGER      NOT NULL REFERENCES users(id),
    check_in     DATE         NOT NULL,
    check_out    DATE         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'
                 CHECK (status IN ('ACTIVE','CHECKED_OUT','CANCELLED')),
    total_cost   DECIMAL(10,2),
    notes        TEXT,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dates CHECK (check_out > check_in)
);

CREATE INDEX IF NOT EXISTS idx_reservations_room ON reservations(room_id);
CREATE INDEX IF NOT EXISTS idx_reservations_guest ON reservations(guest_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);

-- Default admin user (password: admin123)
INSERT INTO users (username, password, full_name, role)
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

UPDATE users SET password = '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9' WHERE username = 'admin';