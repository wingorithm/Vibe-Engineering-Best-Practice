# Booking Flow Logic Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the core business logic for the booking flow, including price calculation, ticket reservation with pessimistic locking, idempotency checks, controller endpoints, and a scheduler for cleaning up expired reservations.

**Architecture:** This plan builds upon the previously scaffolded `booking` module. It introduces a `PriceCalculatorService` to handle discount logic, keeping it separate from the main `BookingService`. The `BookingService` will orchestrate the entire reservation process, including locking, validation, and persistence. The `BookingController` will expose this functionality via a REST API, and a `@Scheduled` task will handle expired bookings.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Data JPA, Flyway, PostgreSQL, Mockito.

---

### Task 1: Price Calculator Service

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/PriceCalculatorService.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/PriceCalculatorServiceImpl.java`
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/booking/service/impl/PriceCalculatorServiceImplTest.java`

- [ ] **Step 1: Write the failing test for `PriceCalculatorService`**
```java
// src/test/java/wingorithm/ticketing/vibeengineering/booking/service/impl/PriceCalculatorServiceImplTest.java
package wingorithm.ticketing.vibeengineering.booking.service.impl;

import org.junit.jupiter.api.Test;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerTierEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorServiceImplTest {

    private final PriceCalculatorServiceImpl priceCalculatorService = new PriceCalculatorServiceImpl();

    @Test
    void calculateFinalPrice_LoversTier_ShouldApply30PercentDiscount() {
        EventEntity event = EventEntity.builder().basePrice(new BigDecimal("100.00")).build();
        CustomerTierEntity tier = CustomerTierEntity.builder().name("Lovers").discountPercentage(30).build();
        CustomerEntity customer = CustomerEntity.builder().tier(tier).build();

        BigDecimal finalPrice = priceCalculatorService.calculateFinalPrice(event, customer, 1);
        
        assertEquals(new BigDecimal("70.00"), finalPrice);
    }
}
```

- [ ] **Step 2: Create the `PriceCalculatorService` interface**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/PriceCalculatorService.java
package wingorithm.ticketing.vibeengineering.booking.service;

import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import java.math.BigDecimal;

public interface PriceCalculatorService {
    BigDecimal calculateFinalPrice(EventEntity event, CustomerEntity customer, int quantity);
}
```

- [ ] **Step 3: Implement `PriceCalculatorServiceImpl` to make the test pass**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/PriceCalculatorServiceImpl.java
package wingorithm.ticketing.vibeengineering.booking.service.impl;

import org.springframework.stereotype.Service;
import wingorithm.ticketing.vibeengineering.booking.service.PriceCalculatorService;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PriceCalculatorServiceImpl implements PriceCalculatorService {

    @Override
    public BigDecimal calculateFinalPrice(EventEntity event, CustomerEntity customer, int quantity) {
        BigDecimal basePrice = event.getBasePrice();
        BigDecimal discount = BigDecimal.valueOf(customer.getTier().getDiscountPercentage()).divide(new BigDecimal("100"));
        BigDecimal discountedPrice = basePrice.multiply(BigDecimal.ONE.subtract(discount));
        return discountedPrice.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}
```

- [ ] **Step 4: Run tests and commit**
Run: `.\gradlew test --tests wingorithm.ticketing.vibeengineering.booking.service.impl.PriceCalculatorServiceImplTest`
Expected: PASS
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/service/ src/test/java/wingorithm/ticketing/vibeengineering/booking/service/
git commit -m "feat: implement PriceCalculatorService with tier-based discounts"
```

---

### Task 2: Booking Service Implementation

**Files:**
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java`
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImplTest.java`

- [ ] **Step 1: Write the failing test for `BookingService`**
```java
// src/test/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImplTest.java
// (This will be a complex test, so we will build it up. Start with the success case.)
package wingorithm.ticketing.vibeengineering.booking.service.impl;
// ... imports
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    // ... mocks

    @Test
    void reserveTicket_Success() {
        // ... test setup
    }
}
```

- [ ] **Step 2: Implement `BookingServiceImpl`**
*(This is a large file, so it will be provided in a separate turn during execution)*

- [ ] **Step 3: Write comprehensive tests for all scenarios**
*(Success, ticket unavailable, idempotency, etc. This will be an iterative process with the implementation.)*

- [ ] **Step 4: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java src/test/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImplTest.java
git commit -m "feat: implement core booking service logic with locking and idempotency"
```

---

### Task 3: Booking Controller Implementation

**Files:**
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java`
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingControllerTest.java`

- [ ] **Step 1: Implement the `BookingController`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java
// ... imports
import org.springframework.web.bind.annotation.*;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
// ...
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BaseResponse<BookingResponse> reserveTicket(
            @RequestBody ReserveTicketRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        BookingResponse response = bookingService.reserveTicket(request, idempotencyKey);
        return BaseResponse.success(response);
    }
}
```

- [ ] **Step 2: Write tests for the `BookingController`**
```java
// src/test/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingControllerTest.java
// (Using Mockito to test the controller in isolation)
```

- [ ] **Step 3: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java src/test/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingControllerTest.java
git commit -m "feat: implement booking controller endpoint"
```

---

### Task 4: Reservation Cleanup Scheduler

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/scheduler/ReservationCleanupScheduler.java`
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/booking/scheduler/ReservationCleanupSchedulerTest.java`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java`

- [ ] **Step 1: Enable Scheduling**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java
import org.springframework.scheduling.annotation.EnableScheduling;
// ...
@SpringBootApplication
@EnableScheduling
public class TicketingVeApplication { //...
```

- [ ] **Step 2: Implement the Scheduler**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/scheduler/ReservationCleanupScheduler.java
// (Implementation to follow during execution)
```

- [ ] **Step 3: Write tests for the Scheduler**
```java
// src/test/java/wingorithm/ticketing/vibeengineering/booking/scheduler/ReservationCleanupSchedulerTest.java
// (Test to verify it correctly identifies and processes expired bookings)
```

- [ ] **Step 4: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/scheduler/ src/test/java/wingorithm/ticketing/vibeengineering/booking/scheduler/ src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java
git commit -m "feat: implement reservation cleanup scheduler"
```
---

### Task 5: Final Integration Test

**Files:**
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/booking/BookingFlowIntegrationTest.java`

- [ ] **Step 1: Write end-to-end integration test**
This test will use `@SpringBootTest` to run the full application context and verify the entire booking flow, from API call to database state changes. It will be the final validation of the feature.

- [ ] **Step 2: Run all tests and verify build**
Run: `.\gradlew clean build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**
```bash
git add src/test/java/wingorithm/ticketing/vibeengineering/booking/BookingFlowIntegrationTest.java
git commit -m "test: add end-to-end integration test for booking flow"
```