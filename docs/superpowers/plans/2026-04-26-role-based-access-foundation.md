# Role-Based Access Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Establish database connectivity to PostgreSQL and use Flyway to initialize the `vibeengineer` schema with core customer tiering tables and mock data, complying with the project constitution.

**Architecture:** Use Flyway for schema management and Spring Data JPA for object-relational mapping. Entities reside in feature-based packages (`customer/model/entity`), extend an auditable `BaseEntity`, and map to tables with mandatory `created_at`/`updated_at` timestamps.

**Tech Stack:** Spring Boot 4.0.6, Spring Data JPA, Flyway, PostgreSQL, Lombok, Java 21.

---

### Task 1: Update Dependencies & Enable Auditing

**Files:**
- Modify: `build.gradle`
- Modify: `src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java`

- [ ] **Step 1: Add JPA and Flyway dependencies**

Add the following to the `dependencies` block in `build.gradle`:
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

- [ ] **Step 2: Enable JPA Auditing**

Modify `src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java` to add `@EnableJpaAuditing`:
```java
package wingorithm.ticketing.vibeengineering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TicketingVeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketingVeApplication.class, args);
    }
}
```

- [ ] **Step 3: Refresh Gradle and verify**

Run: `.\gradlew dependencies --configuration compileClasspath | Select-String -Pattern "flyway|data-jpa"`
Expected: Success, showing the new dependencies.

- [ ] **Step 4: Commit**

```bash
git add build.gradle src/main/java/wingorithm/ticketing/vibeengineering/TicketingVeApplication.java
git commit -m "chore: add JPA, Flyway dependencies and enable JPA auditing"
```

---

### Task 2: Configure Application Properties

**Files:**
- Modify: `src/main/resources/application.properties`

- [ ] **Step 1: Update properties with DB and Flyway config**

```properties
spring.application.name=ticketing-VE

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.properties.hibernate.default_schema=vibeengineer
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.schemas=vibeengineer
spring.flyway.create-schemas=true
spring.flyway.locations=classpath:db/migration
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/application.properties
git commit -m "config: setup database and flyway properties"
```

---

### Task 3: Database Migrations (with Timestamps)

**Files:**
- Create: `src/main/resources/db/migration/V1__init_schema.sql`
- Create: `src/main/resources/db/migration/V2__seed_data.sql`

- [ ] **Step 1: Create V1 Migration (Schema and Tables)**

File: `src/main/resources/db/migration/V1__init_schema.sql`
```sql
CREATE TABLE IF NOT EXISTS customer_tier (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    discount_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    tier_id INTEGER REFERENCES customer_tier(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

- [ ] **Step 2: Create V2 Migration (Seed Data)**

File: `src/main/resources/db/migration/V2__seed_data.sql`
```sql
-- Seed Tiers
INSERT INTO customer_tier (name, discount_percentage) VALUES ('Beginner', 0.00);
INSERT INTO customer_tier (name, discount_percentage) VALUES ('Fans', 10.00);
INSERT INTO customer_tier (name, discount_percentage) VALUES ('Lovers', 30.00);

-- Seed Mock Customers
INSERT INTO customer (id, name, email, tier_id) 
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alice Lover', 'alice@example.com', 3);
INSERT INTO customer (id, name, email, tier_id) 
VALUES ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Bob Fan', 'bob@example.com', 2);
INSERT INTO customer (id, name, email, tier_id) 
VALUES ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Charlie Beginner', 'charlie@example.com', 1);
```

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/
git commit -m "feat: add flyway migrations with constitution timestamps and seed data"
```

---

### Task 4: Common Base Entity

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/common/model/entity/BaseEntity.java`

- [ ] **Step 1: Create BaseEntity for Auditing**

File: `src/main/java/wingorithm/ticketing/vibeengineering/common/model/entity/BaseEntity.java`
```java
package wingorithm.ticketing.vibeengineering.common.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/common/model/entity/BaseEntity.java
git commit -m "feat: add BaseEntity for JPA auditing of timestamps"
```

---

### Task 5: Customer Module Entities

**Files:**
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/customer/model/entity/CustomerTierEntity.java`
- Create: `src/main/java/wingorithm/ticketing/vibeengineering/customer/model/entity/CustomerEntity.java`

- [ ] **Step 1: Create CustomerTierEntity**

File: `src/main/java/wingorithm/ticketing/vibeengineering/customer/model/entity/CustomerTierEntity.java`
```java
package wingorithm.ticketing.vibeengineering.customer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;
import java.math.BigDecimal;

@Entity
@Table(name = "customer_tier", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerTierEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;
}
```

- [ ] **Step 2: Create CustomerEntity**

File: `src/main/java/wingorithm/ticketing/vibeengineering/customer/model/entity/CustomerEntity.java`
```java
package wingorithm.ticketing.vibeengineering.customer.model.entity;

import jakarta.persistence.*;
import lombok.*;
import wingorithm.ticketing.vibeengineering.common.model.entity.BaseEntity;
import java.util.UUID;

@Entity
@Table(name = "customer", schema = "vibeengineer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity extends BaseEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id")
    private CustomerTierEntity tier;
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/wingorithm/ticketing/vibeengineering/customer/model/entity/
git commit -m "feat: add Customer and CustomerTier entities adhering to structure"
```

---

### Task 6: Verification

**Files:**
- Create: `src/test/java/wingorithm/ticketing/vibeengineering/customer/model/entity/DatabaseIntegrationTest.java`

- [ ] **Step 1: Run the application**

Run: `.\gradlew bootRun`
Expected: Application starts successfully, Flyway migrations are applied.

- [ ] **Step 2: Add a basic smoke test**

Create `src/test/java/wingorithm/ticketing/vibeengineering/customer/model/entity/DatabaseIntegrationTest.java`:
```java
package wingorithm.ticketing.vibeengineering.customer.model.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.EntityManager;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.UUID;

@SpringBootTest
class DatabaseIntegrationTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    void testFindCustomer() {
        UUID id = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        CustomerEntity customer = entityManager.find(CustomerEntity.class, id);
        assertThat(customer).isNotNull();
        assertThat(customer.getName()).isEqualTo("Alice Lover");
        assertThat(customer.getTier().getName()).isEqualTo("Lovers");
        assertThat(customer.getCreatedAt()).isNotNull();
    }
}
```

- [ ] **Step 3: Run tests**

Run: `.\gradlew test`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add src/test/java/wingorithm/ticketing/vibeengineering/customer/model/entity/DatabaseIntegrationTest.java
git commit -m "test: add database integration smoke test verifying timestamps and relations"
```
