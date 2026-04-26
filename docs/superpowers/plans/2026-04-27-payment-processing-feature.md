# Payment Processing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a resilient payment processing system by integrating with an external payment gateway using Feign Client, structured with a Template Method design pattern and a two-phase transaction with compensation for handling failures.

**Architecture:** A new `payment` module will be created for all payment-related components. The `BookingService` will be refactored to use a `BookingFlowTemplate` to orchestrate the reservation and payment process. `PaymentService` will encapsulate the Feign client communication.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Data JPA, Feign Client, WireMock, Mockito.

---

### Task 1: Dependencies and Configuration

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/config/FeignConfig.java`

- [ ] **Step 1: Add Feign Client and WireMock Dependencies**
```gradle
// build.gradle
dependencies {
    // ... existing dependencies
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign:4.1.2'
    testImplementation 'com.github.tomakehurst:wiremock-jre8:3.0.1'
}
```

- [ ] **Step 2: Enable Feign Clients**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java
import org.springframework.cloud.openfeign.EnableFeignClients;
//...
@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class TicketingVeApplication { //...
```

- [ ] **Step 3: Create Feign Configuration**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/config/FeignConfig.java
package wingorithm.ticketing.vibeengineering.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

- [ ] **Step 4: Commit**
```bash
git add build.gradle src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java src/main/java/wingorithm/ticketing/vibeengineering/config/FeignConfig.java
git commit -m "feat: add feign client and wiremock dependencies and configuration"
```

---

### Task 2: Payment Module Scaffolding

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/payment/integration/dto/PaymentRequest.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/payment/integration/dto/PaymentResponse.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/payment/integration/PaymentGatewayClient.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/payment/service/PaymentService.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/payment/service/impl/PaymentServiceImpl.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/exception/PaymentFailedException.java`

- [ ] **Step 1: Create DTOs and Feign Client**
*(Implementations provided in the design document)*

- [ ] **Step 2: Create `PaymentService` and `PaymentServiceImpl`**
*(Initial empty implementations)*

- [ ] **Step 3: Create `PaymentFailedException`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/exception/PaymentFailedException.java
package wingorithm.ticketing.vibeengineering.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
```

- [ ] **Step 4: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/payment/ src/main/java/wingorithm/ticketing/vibeengineering/exception/PaymentFailedException.java
git commit -m "feat: scaffold payment module with feign client, dtos, and service"
```

---

### Task 3: Refactor Booking Service with Template Method

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/template/BookingFlowTemplate.java`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/BookingService.java`

- [ ] **Step 1: Create `BookingFlowTemplate`**
```java
// src/main/java/wingorithm/ticketing/vibeengineering/booking/service/template/BookingFlowTemplate.java
// (Implementation to be provided during execution)
```

- [ ] **Step 2: Refactor `BookingServiceImpl` to use the template**
*(This will involve moving the existing logic into the `reserve`, `confirmBooking`, and `compensate` methods of the template implementation.)*

- [ ] **Step 3: Update `BookingService` interface**
The `reserveTicket` method will now likely take a `BookingEntity` or `bookingId` to process payment, separating the reservation and payment steps internally.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/booking/service/
git commit -m "refactor: apply template method pattern to booking service"
```

---

### Task 4: Implement Payment Service and Logic

**Files:**
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/payment/service/impl/PaymentServiceImpl.java`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java`
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/payment/service/impl/PaymentServiceImplTest.java`

- [ ] **Step 1: Implement `PaymentServiceImpl`**
This service will use the `PaymentGatewayClient` to make the payment request.

- [ ] **Step 2: Integrate `PaymentService` into `BookingServiceImpl`**
The `processPayment` method of the `BookingServiceImpl` will now call the `PaymentService`.

- [ ] **Step 3: Write tests for `PaymentServiceImpl`**
Use Mockito to mock the Feign Client and test the service logic.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/payment/service/impl/PaymentServiceImpl.java src/main/java/wingorithm/ticketing/vibeengineering/booking/service/impl/BookingServiceImpl.java src/test/java/wingorithm/ticketing/vibeengineering/payment/service/impl/PaymentServiceImplTest.java
git commit -m "feat: implement payment service and integrate with booking flow"
```

---

### Task 5: End-to-End Integration Test with WireMock

**Files:**
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/payment/PaymentFlowIntegrationTest.java`

- [ ] **Step 1: Set up WireMock server**
Use a JUnit Jupiter extension or rule to start and stop a WireMock server for the test.

- [ ] **Step 2: Stub the payment gateway endpoints**
Configure WireMock to return success and failure responses for `POST /api/v1/payment`.

- [ ] **Step 3: Write the integration test**
This test will call the booking endpoint and assert that the `BookingEntity` is updated correctly based on the stubbed WireMock response (e.g., status becomes `SOLD` on success, `CANCELLED` on failure).

- [ ] **Step 4: Run all tests and verify build**
Run: `.\gradlew clean build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**
```bash
git add src/test/java/wingorithm/ticketing/vibeengineering/payment/PaymentFlowIntegrationTest.java
git commit -m "test: add end-to-end integration test for payment flow with wiremock"
```