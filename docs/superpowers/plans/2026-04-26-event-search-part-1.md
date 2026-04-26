# Event Search API (Part 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a paginated event search API with standardized responses, Swagger documentation, and high-performance database indexing.

**Architecture:** Feature-based module (`event`), standardized response envelope, MapStruct for mapping, and Flyway for schema.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Spring Data JPA, Flyway, MapStruct, SpringDoc OpenAPI.

---

### Task 1: Infrastructure & Standardized Response

**Files:**
- Modify: `build.gradle`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/common/model/dto/ErrorSchema.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/common/model/dto/BaseResponse.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: Add SpringDoc and MapStruct dependencies**

Add the following to `build.gradle`:
```gradle
dependencies {
    // ...
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5'
    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
    // ...
}
```

- [ ] **Step 2: Create ErrorSchema DTO**

File: `src/main/java/wingorithm/ticketing/vibeengineering/common/model/dto/ErrorSchema.java`
```java
package wingorithm.ticketing.vibeengineering.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorSchema {
    private String errorCode;
    private String message;
}
```

- [ ] **Step 3: Create BaseResponse DTO**

File: `src/main/java/wingorithm/ticketing/vibeengineering/common/model/dto/BaseResponse.java`
```java
package wingorithm.ticketing.vibeengineering.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private ErrorSchema errorSchema;
    private T outputSchema;

    public static <T> BaseResponse<T> success(T output) {
        return BaseResponse.<T>builder()
                .errorSchema(ErrorSchema.builder()
                        .errorCode("0000")
                        .message("Success")
                        .build())
                .outputSchema(output)
                .build();
    }
}
```

- [ ] **Step 4: Create GlobalExceptionHandler**

File: `src/main/java/wingorithm/ticketing/vibeengineering/exception/GlobalExceptionHandler.java`
```java
package wingorithm.ticketing.vibeengineering.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
import wingorithm.ticketing.vibeengineering.common.model.dto.ErrorSchema;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleAllExceptions(Exception ex) {
        BaseResponse<Object> response = BaseResponse.builder()
                .errorSchema(ErrorSchema.builder()
                        .errorCode("9999")
                        .message(ex.getMessage())
                        .build())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

- [ ] **Step 5: Verify build**

Run: `.\gradlew clean build -x test`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add build.gradle src/main/java/wingorithm/ticketing/vibeengineering/common/model/dto/ src/main/java/wingorithm/ticketing/vibeengineering/exception/
git commit -m "chore: add springdoc, mapstruct and standardized response infrastructure"
```

---

### Task 2: Database Migration (Event Table)

**Files:**
- Create: `src/main/resources/db/migration/V3__create_event_table.sql`

- [ ] **Step 1: Create V3 Migration**

File: `src/main/resources/db/migration/V3__create_event_table.sql`
```sql
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
```

- [ ] **Step 2: Run migration and verify**

Run: `.\gradlew bootRun` (Check logs for Flyway V3 success)
Expected: `Successfully applied 1 migration to schema "vibeengineer"`

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V3__create_event_table.sql
git commit -m "feat: add event table migration with optimized indexes and seed data"
```

---

### Task 3: Event Entity & Repository

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/entity/EventEntity.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/repository/EventRepository.java`

- [ ] **Step 1: Create EventEntity**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/entity/EventEntity.java`
```java
package wingorithm.ticketing.vibeengineering.event.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity extends BaseEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String artist;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;
}
```

- [ ] **Step 2: Create EventRepository**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/repository/EventRepository.java`
```java
package wingorithm.ticketing.vibeengineering.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, UUID> {

    @Query("SELECT e FROM EventEntity e " +
           "WHERE (:location IS NULL OR e.location = :location) " +
           "AND (:artist IS NULL OR e.artist = :artist) " +
           "AND (CAST(:date AS date) IS NULL OR CAST(e.dateTime AS date) = :date)")
    Page<EventEntity> searchEvents(
            @Param("location") String location,
            @Param("artist") String artist,
            @Param("date") LocalDate date,
            Pageable pageable);
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/event/
git commit -m "feat: add EventEntity and EventRepository with search query"
```

---

### Task 4: Event DTO & Mapper

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/dto/EventResponse.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/mapper/EventMapper.java`

- [ ] **Step 1: Create EventResponse DTO**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/dto/EventResponse.java`
```java
package wingorithm.ticketing.vibeengineering.event.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID id;
    private String name;
    private String artist;
    private String location;
    private LocalDateTime dateTime;
    private Integer totalTickets;
    private BigDecimal basePrice;
}
```

- [ ] **Step 2: Create EventMapper**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/model/mapper/EventMapper.java`
```java
package wingorithm.ticketing.vibeengineering.event.model.mapper;

import org.mapstruct.Mapper;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.model.entity.EventEntity;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventResponse toResponse(EventEntity entity);
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/event/model/dto/ src/main/java/wingorithm/ticketing/vibeengineering/event/model/mapper/
git commit -m "feat: add EventResponse DTO and MapStruct mapper"
```

---

### Task 5: Event Service

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/service/EventService.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/service/impl/EventServiceImpl.java`

- [ ] **Step 1: Create EventService interface**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/service/EventService.java`
```java
package wingorithm.ticketing.vibeengineering.event.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;

import java.time.LocalDate;

public interface EventService {
    Page<EventResponse> searchEvents(String location, String artist, LocalDate date, Pageable pageable);
}
```

- [ ] **Step 2: Create EventServiceImpl**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/service/impl/EventServiceImpl.java`
```java
package wingorithm.ticketing.vibeengineering.event.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.model.mapper.EventMapper;
import wingorithm.ticketing.vibeengineering.event.repository.EventRepository;
import wingorithm.ticketing.vibeengineering.event.service.EventService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(String location, String artist, LocalDate date, Pageable pageable) {
        return eventRepository.searchEvents(location, artist, date, pageable)
                .map(eventMapper::toResponse);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/event/service/
git commit -m "feat: add EventService implementation for paginated search"
```

---

### Task 6: Event Controller & Swagger

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/event/controller/EventController.java`

- [ ] **Step 1: Create EventController**

File: `src/main/java/wingorithm/ticketing/vibeengineering/event/controller/EventController.java`
```java
package wingorithm.ticketing.vibeengineering.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import wingorithm.ticketing.vibeengineering.common.model.dto.BaseResponse;
import wingorithm.ticketing.vibeengineering.event.model.dto.EventResponse;
import wingorithm.ticketing.vibeengineering.event.service.EventService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Search", description = "API for finding concert events")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Search events", description = "Find events by location, artist, or date with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved events"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public BaseResponse<Page<EventResponse>> searchEvents(
            @Parameter(description = "Filter by location") @RequestParam(required = false) String location,
            @Parameter(description = "Filter by artist name") @RequestParam(required = false) String artist,
            @Parameter(description = "Filter by date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        
        Page<EventResponse> result = eventService.searchEvents(location, artist, date, pageable);
        return BaseResponse.success(result);
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/event/controller/EventController.java
git commit -m "feat: add EventController with Swagger documentation and standardized response"
```

---

### Task 7: Integration Test

**Files:**
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/event/controller/EventControllerTest.java`

- [ ] **Step 1: Create EventControllerTest**

File: `src/test/java/wingorithm/ticketing/vibeengineering/event/controller/EventControllerTest.java`
```java
package wingorithm.ticketing.vibeengineering.event.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchEvents_ShouldReturnStandardizedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                .param("location", "Jakarta")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorSchema.errorCode").value("0000"))
                .andExpect(jsonPath("$.errorSchema.message").value("Success"))
                .andExpect(jsonPath("$.outputSchema.content").isArray())
                .andExpect(jsonPath("$.outputSchema.content[0].location").value("Jakarta"));
    }

    @Test
    void searchEvents_ByArtist_ShouldReturnCorrectResult() throws Exception {
        mockMvc.perform(get("/api/v1/events")
                .param("artist", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outputSchema.content[0].artist").value("Alice"));
    }
}
```

- [ ] **Step 2: Run tests**

Run: `.\gradlew test --tests wingorithm.ticketing.vibeengineering.event.controller.EventControllerTest`
Expected: PASS

- [ ] **Step 3: Verify Swagger UI**

Run: `.\gradlew bootRun`
Then check (manually or via curl): `http://localhost:8080/v3/api-docs`
Expected: 200 OK with JSON documentation.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/wingorithm/ticketing/vibeengineering/event/controller/EventControllerTest.java
git commit -m "test: add integration test for Event Search API"
```
