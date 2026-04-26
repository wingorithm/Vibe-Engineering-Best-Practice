# Booking Flow Design

**Goal:** Implement a robust and high-performance booking system for concert events, ensuring strict concurrency control, idempotency, and automated reservation cleanup.

**Architecture:**
*   **Feature Module:** `booking` module, mirroring the existing `event` and `customer` structure.
*   **Database-Centric Concurrency:** Leverage PostgreSQL's row-level pessimistic locking for `EventEntity` to manage ticket availability.
*   **Idempotency:** A dedicated `IdempotencyKeyEntity` table to prevent duplicate requests.
*   **Scheduled Task:** A Spring `@Scheduled` job for automatic cleanup of expired reservations.

**Components:**

1.  **Database Migration (Flyway):**
    *   **`V4__create_booking_tables.sql`:**
        *   **`booking` table:** Stores reservation details (`id`, `event_id`, `customer_id`, `status` (RESERVED, SOLD, CANCELLED), `reserved_at`, `expires_at`, `total_price`, `tier_discount_amount`, `final_price`, `payment_transaction_id`).
        *   **`idempotency_key` table:** Stores `key` (UUID/String), `request_hash`, `response_body` (JSON), `status` (PENDING, COMPLETED, FAILED), `created_at`.
        *   **Update `event` table:** Add `available_tickets` (integer, default `total_tickets`).

2.  **`BookingEntity`:**
    *   Entity mapping for the `booking` table.
    *   `status` enum (RESERVED, SOLD, CANCELLED).
    *   `event` (ManyToOne to `EventEntity`), `customer` (ManyToOne to `CustomerEntity`).

3.  **`IdempotencyKeyEntity`:**
    *   Entity mapping for the `idempotency_key` table.
    *   Unique constraint on `key`.

4.  **`BookingRepository`:**
    *   Spring Data JPA repository for `BookingEntity`.
    *   Custom query for pessimistic locking on `EventEntity` when reserving tickets.

5.  **`IdempotencyKeyRepository`:**
    *   Spring Data JPA repository for `IdempotencyKeyEntity`.

6.  **`BookingService`:**
    *   **`reserveTickets(ReserveTicketRequest request, String idempotencyKey)` method:**
        *   Check `IdempotencyKeyEntity` for existing key. If `COMPLETED` or `PENDING`, return cached response. If `FAILED`, reprocess.
        *   Acquire pessimistic lock on `EventEntity` row using `SELECT ... FOR UPDATE`.
        *   Check `available_tickets`. If `0`, throw `TicketsUnavailableException`.
        *   Decrement `available_tickets` in `EventEntity`.
        *   Calculate `total_price`, `tier_discount_amount`, `final_price` based on `CustomerTierEntity`.
        *   Create `BookingEntity` with `status = RESERVED`, `expires_at = now + 15 minutes`.
        *   Save `BookingEntity`.
        *   Save `IdempotencyKeyEntity` with `status = PENDING` and a placeholder response.
        *   Return `BookingResponse`.

7.  **`BookingController`:**
    *   REST endpoint for `/api/v1/bookings`.
    *   `POST /api/v1/bookings`: Accepts `ReserveTicketRequest` and `Idempotency-Key` header. Returns `BaseResponse<BookingResponse>`.

8.  **`ReservationCleanupScheduler`:**
    *   Spring `@Component` with `@Scheduled` method.
    *   Runs every minute.
    *   Queries `Booking` table for `status = RESERVED` and `expires_at < now`.
    *   For each expired booking:
        *   Increment `available_tickets` in corresponding `EventEntity`.
        *   Update `BookingEntity` `status = CANCELLED`.
        *   Consider logging the cleanup action.

9.  **DTOs:**
    *   `ReserveTicketRequest`: `event_id`, `customer_id`, `quantity`.
    *   `BookingResponse`: Booking details, including `status`, `expires_at`, `final_price`.

**Error Handling:**
*   `TicketsUnavailableException`: Custom exception for out-of-stock scenarios.
*   `DuplicateIdempotencyKeyException`: Custom exception for idempotent requests in `PENDING` state to avoid immediate re-processing (handled by returning existing `PENDING` or `COMPLETED` response).
*   Global exception handler will catch these and return appropriate `ErrorSchema` responses.

**Testing:**
*   **Unit Tests:** For `BookingService` (mocking repositories, verifying calculations, locking logic).
*   **Integration Tests:** For `BookingController` using `MockMvc` or `RestTestClient` (if compatible with Spring Boot 4.0 after dependency updates) to verify API behavior, concurrency, and idempotency.
*   **Scheduled Task Test:** Verify the cleanup scheduler correctly identifies and processes expired reservations.
