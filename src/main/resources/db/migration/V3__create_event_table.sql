CREATE TABLE IF NOT EXISTS event (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    total_tickets INTEGER NOT NULL,
    base_price DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_event_search ON event (location, date_time);
CREATE INDEX idx_event_artist ON event (artist);

-- Seed some events
INSERT INTO event (id, name, artist, location, date_time, total_tickets, base_price)
VALUES ('e1eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'Neon Nights', 'Alice', 'Jakarta', '2026-05-01 20:00:00', 500, 150000.00);
INSERT INTO event (id, name, artist, location, date_time, total_tickets, base_price)
VALUES ('e2eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Skyline Beats', 'Bob', 'Jakarta', '2026-05-15 19:00:00', 300, 200000.00);
INSERT INTO event (id, name, artist, location, date_time, total_tickets, base_price)
VALUES ('e3eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'Velvet Vocals', 'Charlie', 'Bandung', '2026-06-01 18:30:00', 100, 500000.00);
