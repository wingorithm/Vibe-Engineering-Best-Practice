-- Add available_tickets to event table for concurrency control
ALTER TABLE vibeengineer.event
ADD COLUMN available_tickets INTEGER;

-- Initialize available_tickets with the total_tickets value
UPDATE vibeengineer.event
SET available_tickets = total_tickets;

-- Create booking table to store reservations
CREATE TABLE IF NOT EXISTS vibeengineer.booking (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL REFERENCES vibeengineer.event(id),
    customer_id UUID NOT NULL REFERENCES vibeengineer.customer(id),
    status VARCHAR(20) NOT NULL, -- RESERVED, SOLD, CANCELLED
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    total_price DECIMAL(15, 2) NOT NULL,
    tier_discount_amount DECIMAL(15, 2) NOT NULL,
    final_price DECIMAL(15, 2) NOT NULL,
    payment_transaction_id UUID,
    idempotency_key VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create idempotency_key table for handling duplicate requests
CREATE TABLE IF NOT EXISTS vibeengineer.idempotency_key (
    key VARCHAR(255) PRIMARY KEY,
    response_body TEXT,
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_booking_status_expires ON vibeengineer.booking (status, expires_at);
