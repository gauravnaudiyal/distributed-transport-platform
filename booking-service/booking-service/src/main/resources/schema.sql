-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    booking_id       VARCHAR(36)  PRIMARY KEY,
    user_id          VARCHAR(255) NOT NULL,
    origin           VARCHAR(255) NOT NULL,
    destination      VARCHAR(255) NOT NULL,
    state            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    map_version      VARCHAR(50),
    coordinator_region VARCHAR(100),
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    time_window_start TIMESTAMP   NOT NULL,
    time_window_end   TIMESTAMP   NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_state   ON bookings(state);

-- Booking segments table
CREATE TABLE IF NOT EXISTS booking_segments (
    id                   BIGSERIAL    PRIMARY KEY,
    booking_id           VARCHAR(36)  NOT NULL REFERENCES bookings(booking_id),
    segment_id           VARCHAR(100) NOT NULL,
    authoritative_region VARCHAR(100) NOT NULL,
    reservation_status   VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    time_window_start    TIMESTAMP    NOT NULL,
    time_window_end      TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_segments_booking_id ON booking_segments(booking_id);

-- Outbox events table (transactional outbox pattern)
CREATE TABLE IF NOT EXISTS outbox_events (
    event_id   VARCHAR(36)  PRIMARY KEY,
    event_type VARCHAR(50)  NOT NULL,
    booking_id VARCHAR(36)  NOT NULL,
    user_id    VARCHAR(255) NOT NULL,
    map_version VARCHAR(50),
    payload    TEXT,
    published  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished ON outbox_events(published, created_at)
    WHERE published = FALSE;
