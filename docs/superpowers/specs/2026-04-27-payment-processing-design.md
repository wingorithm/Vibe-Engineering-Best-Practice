# Payment Processing Design

**Goal:** Implement a resilient payment processing system that integrates with an external payment gateway using Feign Client. The system will use a two-phase transaction with compensating actions to handle payment failures gracefully and will be structured using the Template Method design pattern.

**Architecture:**
*   **Feature Module:** `payment` module, containing the Feign client, DTOs, and the core payment service.
*   **Template Method Pattern:** An abstract `BookingFlowTemplate` will define the high-level steps of the booking process (`reserve`, `processPayment`, `confirm`), allowing for flexible implementation while enforcing a consistent workflow.
*   **Two-Phase Transaction:** The initial ticket reservation will be one transaction. The payment processing will be a separate, subsequent action. If the payment fails, a compensating transaction will be triggered to roll back the reservation.
*   **Feign Client:** A declarative `PaymentGatewayClient` will handle all communication with the external payment service.

**Components:**

1.  **Database Migration (Flyway):**
    *   **`V5__add_payment_details_to_booking.sql`:**
        *   **Update `booking` table:** Add `receipt_url` (VARCHAR) and `payment_failure_reason` (TEXT) to store results from the payment gateway.

2.  **`payment` Module:**
    *   **`integration/`**
        *   **`PaymentGatewayClient.java`:** A Feign client interface for `http://localhost:9001`.
        *   **`dto/`**:
            *   `PaymentRequest.java`: DTO for the payment gateway request.
            *   `PaymentResponse.java`: DTO for the payment gateway response (handling both success and failure cases).
    *   **`service/`**
        *   **`PaymentService.java`:** Interface for processing payments.
        *   **`impl/PaymentServiceImpl.java`:** Implementation that uses the `PaymentGatewayClient`.

3.  **`booking` Module (Refactoring):**
    *   **`service/`**
        *   **`template/BookingFlowTemplate.java` (Abstract Class):**
            *   Defines the final `processBooking` method which calls the abstract steps.
            *   Abstract methods: `reserve`, `processPayment`, `confirmBooking`, `compensate`.
        *   **`impl/BookingServiceImpl.java` (Refactored):**
            *   Will extend `BookingFlowTemplate`.
            *   The main `reserveTicket` method will now call `processBooking` from the template.
            *   Implements the `reserve`, `processPayment`, `confirmBooking`, and `compensate` methods.
            *   The `processPayment` method will call the `PaymentService`.
            *   The `compensate` method will be called on payment failure to revert the booking status and ticket count.

4.  **Error Handling & DTOs:**
    *   **`PaymentFailedException`:** Custom exception to be thrown by `PaymentService` on a failed payment.
    *   **`PaymentResponse` DTOs:** Will be structured to capture both success (`receipt_url`, etc.) and failure (`error`, `message`) fields from the gateway.

**Data Flow (Successful Payment):**
1.  `BookingController` receives a reservation request.
2.  `BookingServiceImpl`'s `processBooking` (from template) is called.
3.  **`reserve()` (Transaction 1):**
    *   Pessimistically locks the `EventEntity`.
    *   Decrements `available_tickets`.
    *   Creates a `BookingEntity` with `status = RESERVED`.
    *   Commits this transaction.
4.  **`processPayment()`:**
    *   Calls `PaymentService`, which uses `PaymentGatewayClient` to hit the external gateway.
    *   The gateway returns a successful `PaymentResponse`.
5.  **`confirmBooking()` (Transaction 2):**
    *   Updates the `BookingEntity` status to `SOLD`.
    *   Saves the `payment_transaction_id` and `receipt_url` to the `BookingEntity`.
    *   Commits this transaction.
6.  A successful `BookingResponse` is returned to the user.

**Data Flow (Failed Payment):**
1.  Steps 1-3 are the same as the success flow.
2.  **`processPayment()`:**
    *   The `PaymentGatewayClient` receives a failure response from the gateway.
    *   `PaymentService` throws a `PaymentFailedException`.
3.  The `processBooking` template catches the exception and calls `compensate()`.
4.  **`compensate()` (Transaction 3 - Compensating):**
    *   Finds the `BookingEntity`.
    *   Updates its status to `CANCELLED`.
    *   Increments `available_tickets` on the `EventEntity`.
    *   Saves the `payment_failure_reason`.
    *   Commits this transaction.
5.  A `BaseResponse` with an appropriate `ErrorSchema` is returned to the user.

**Testing:**
*   **Unit Tests:** For `PaymentServiceImpl` (using a mock Feign client) and `BookingServiceImpl` (mocking the `PaymentService`).
*   **Integration Tests:** Use a tool like WireMock to mock the external payment gateway (`http://localhost:9001`) and test the full flow within a `@SpringBootTest` context.

Does this design look good? Is there anything you would like to clarify or change?