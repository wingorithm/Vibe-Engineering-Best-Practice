I want to build the core REST API for a Concert Event Ticketing System.
where the core features comprise of:

- Role-Based Access: Customer that has tiering (Beginner, Fans, Lovers).
    - When booking each customer has different price cut based on tiering
        - Beginner = no price cut
        - Fans = 10% price cut
        - Lovers = 30% price cut

- Event Search: endpoints to find events location or artist name.
    - UAC 1: Given a user searches for events by date and location, the response must be paginated
    - UAC 2: Given a user searches for events api should response within 100ms.

- Booking Flow: A transactional workflow to reserve a ticket, process a mock payment, and issue a digital ticket.  Description: Users attempt to reserve tickets. This initiates a 15-minute hold
    - UAC 1 (Strict Concurrency): Given 1,000 concurrent requests for an event with only 10 tickets remaining, exactly 10 requests must succeed with a 202 Accepted (reservation pending), and 990 must fail gracefully. Zero oversell is permitted.
    - UAC 2 (State Machine): Given a successful reservation, the ticket state changes to RESERVED. If payment is not confirmed within 15 minutes, a background job must automatically revert the state to AVAILABLE and replenish the cache.
    - UAC 3 (Idempotency): Given a user retries the exact same reservation request (due to network timeout) using the same Idempotency-Key header, the system must return the cached response of the initial request without creating a duplicate reservation.

- Payment Processing: hit the external payment gateway to support 'Booking Flow' on `http://localhost:9001/api/v1/payment` with below json request example
```json
{
    "event_name": "Neon Nights",
    "customer_name": "Alice Lover",
    "trx_id": "uuid-8a7b6c5d4e",
    "amount": 150000.00,
    "currency": "IDR"
}
```
it can return payment confirmation completed or failure with below response example
```json
// COMPLETED 200 ok
{
    "transaction_id": "uuid-8a7b6c5d4e",
    "status": "COMPLETED",
    "amount": 150000,
    "currency": "IDR",
    "created_at": "2026-04-25T09:57:02Z",
    "payment_method": {
        "type": "credit_card",
        "card_brand": "visa",
        "last_four": "4242"
    },
    "receipt_url": "https://api.paymentgateway.example.com/receipts/uuid-8a7b6c5d4e"
}

// FAILED 400 bad request
{
  "transaction_id": "uuid-8a7b6c5d4e",
  "status": "FAILED",
  "amount": 150000,
  "currency": "IDR",
  "created_at": "2026-04-25T09:58:13Z",
  "remark": "Neon Nights - Alice Lover",
  "error": {
    "code": "insufficient_funds",
    "message": "The transaction was declined by the issuer due to insufficient funds.",
    "decline_code": "card_declined"
  }
}
```
- UAC 1: Given a valid payment success payload for a RESERVED ticket, the system updates the state to SOLD, update receipt to db, and return receipt_url.
- UAC 2: Given an invalid or failed response, the system must reject it, the system updates the ticket state to AVAILABLE, return failed payment response to user.