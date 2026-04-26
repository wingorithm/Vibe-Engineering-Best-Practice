# Booking Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a high-performance booking system with pessimistic locking for concurrency control, a dedicated table for idempotency, and a scheduled job for cleaning up expired reservations.

**Architecture:** A new `booking` feature module will be created. A `PriceCalculatorService` will be introduced to decouple pricing logic from booking orchestration, making the system more modular and easier to extend for future payment gateway integration. Concurrency is managed at the database level using `SELECT ... FOR UPDATE` to prevent race conditions.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Data JPA, Flyway, PostgreSQL.

---

### Task 1: Database Migration

**Files:**
- Create: `src/main/resources/db/migration/V4__create_booking_tables.sql`

- [ ] **Step 1: Create V4 Migration Script**

File: `src/main/resources/db/migration/V4__create_booking_tables.sql`
```sql
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
```

- [ ] **Step 2: Run migration and verify**

Run: `.\gradlew bootRun`
Expected: Flyway V4 migration applies successfully. Check logs for confirmation.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V4__create_booking_tables.sql
git commit -m "feat: add booking and idempotency tables, update event table"
```

---

### Task 2: Core Entities & Enums

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/BookingStatus.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/BookingEntity.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/IdempotencyKeyStatus.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/IdempotencyKeyEntity.java`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/entity/EventEntity.java`

- [ ] **Step 1: Add `availableTickets` to EventEntity**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/event/model/entity/EventEntity.java
// ... existing class ...
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "available_tickets")
    private Integer availableTickets;
}
```

- [ ] **Step 2: Create `BookingStatus` Enum**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/BookingStatus.java
package wingorithm.ticketing.vibeengineering.booking.model.entity;

public enum BookingStatus {
    RESERVED,
    SOLD,
    CANCELLED
}
```

- [ ] **Step 3: Create `BookingEntity`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/BookingEntity.java
package wingorithm.ticketing.vibeengineering.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;
import wingorithm.ticketing.vibeengineering.customer.model.entity.CustomerEntity;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEntity extends BaseEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "reserved_at", nullable = false)
    private LocalDateTime reservedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "tier_discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal tierDiscountAmount;

    @Column(name = "final_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "payment_transaction_id")
    private UUID paymentTransactionId;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;
}
```

- [ ] **Step 4: Create `IdempotencyKeyStatus` Enum**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/IdempotencyKeyStatus.java
package wingorithm.ticketing.vibeengineering.booking.model.entity;

public enum IdempotencyKeyStatus {
    PENDING,
    COMPLETED,
    FAILED
}
```

- [ ] **Step 5: Create `IdempotencyKeyEntity`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/model/entity/IdempotencyKeyEntity.java
package wingorithm.ticketing.vibeengineering.booking.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_key", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKeyEntity {
    @Id
    private String key;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyKeyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

- [ ] **Step 6: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/ src/main/java/wingorithm/ticketing/vibeengineering/event/model/entity/EventEntity.java
git commit -m "feat: create booking and idempotency entities and enums"
```

---

### Task 3: Repositories

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/repository/BookingRepository.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/repository/IdempotencyKeyRepository.java`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/event/repository/EventRepository.java`

- [ ] **Step 1: Create `BookingRepository`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/repository/BookingRepository.java
package wingorithm.ticketing.vibeengineering.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.booking.model.entity.BookingEntity;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {}
```

- [ ] **Step 2: Create `IdempotencyKeyRepository`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/repository/IdempotencyKeyRepository.java
package wingorithm.ticketing.vibeengineering.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.booking.model.entity.IdempotencyKeyEntity;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {}
```

- [ ] **Step 3: Add Pessimistic Lock query to `EventRepository`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/event/repository/EventRepository.java
// ... imports
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
// ... existing interface
public interface EventRepository extends JpaRepository<EventEntity, UUID> {
    // ... existing searchEvents method

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM EventEntity e WHERE e.id = :id")
    Optional<EventEntity> findByIdWithPessimisticLock(@Param("id") UUID id);
}
```

- [ ] **Step 4: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/repository/ src/main/java/wingorithm/ticketing/vibeengineering/event/repository/EventRepository.java
git commit -m "feat: add booking, idempotency repositories and pessimistic lock query"
```

---

### Task 4: DTOs and Exceptions

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/model/dto/ReserveTicketRequest.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/model/dto/BookingResponse.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/exception/TicketUnavailableException.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/exception/IdempotencyException.java`

- [ ] **Step 1: Create DTOs**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/model/dto/ReserveTicketRequest.java
package wingorithm.ticketing.vibeengineering.booking.model.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ReserveTicketRequest {
    private UUID eventId;
    private UUID customerId;
    private Integer quantity;
}

// src/main/java/wingorithm/ticketing/vibeengineering/booking/model/dto/BookingResponse.java
package wingorithm.ticketing.vibeengineering.booking.model.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BookingResponse {
    private UUID bookingId;
    private String status;
    private LocalDateTime expiresAt;
    private BigDecimal finalPrice;
}
```

- [ ] **Step 2: Create Custom Exceptions**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/exception/TicketUnavailableException.java
package wingorithm.ticketing.vibeengineering.exception;

public class TicketUnavailableException extends RuntimeException {
    public TicketUnavailableException(String message) {
        super(message);
    }
}

// src/main/java/wingorithm/ticketing/vibeengineering/exception/IdempotencyException.java
package wingorithm.ticketing.vibeengineering.exception;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/model/dto/ src/main/java/wingorithm/ticketing/vibeengineering/exception/
git commit -m "feat: add DTOs and custom exceptions for booking flow"
```

---

### Task 5: Service Layer (Booking and Controller)

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/BookingService.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java`

- [ ] **Step 1: Create `BookingService` interface**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/BookingService.java
package wingorithm.ticketing.vibeengineering.booking.service;

import wingorithm.ticketing.vibeengineering.booking.model.dto.BookingResponse;
import wingorithm.ticketing.vibeengineering.booking.model.dto.ReserveTicketRequest;

public interface BookingService {
    BookingResponse reserveTicket(ReserveTicketRequest request, String idempotencyKey);
}
```

- [ ] **Step 2: Create `BookingServiceImpl`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java
// (Implementation will be provided in the next turn to keep this manageable)
```

- [ ] **Step 3: Create `BookingController`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java
// (Implementation will be provided in the next turn)
```

- [ ] **Step 4: Commit placeholder files**
```bash
# Create empty files first
touch src/main/java/wingorithm/ticketing/vibeengineering/booking/service/BookingService.java
touch src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java
touch src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/BookingController.java
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/service/ src/main/java/wingorithm/ticketing/vibeengineering/booking/controller/
git commit -m "feat: scaffold booking service and controller"
```
*(Self-correction: The plan is getting very large. I will break the implementation of the Service and Controller into the next tasks to ensure each step is small and focused, even though they are conceptually part of the same layer.)*

---
This concludes the setup. The subsequent tasks will focus on implementing the service logic, controller endpoints, the cleanup scheduler, and finally, thorough testing.