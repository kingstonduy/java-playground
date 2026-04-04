package me.personal.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * PHASE 5 — @Version (Optimistic Locking)
 *
 * Simple entity to demonstrate optimistic locking separately.
 *
 * How @Version works:
 *   1. You read Product (version = 0)
 *   2. You modify price
 *   3. Hibernate sends: UPDATE products SET price=?, version=1 WHERE id=? AND version=0
 *   4. If another transaction already changed it (version != 0), UPDATE affects 0 rows
 *   5. Hibernate throws OptimisticLockException
 *
 * This prevents "lost updates" without pessimistic locking (no SELECT FOR UPDATE).
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    Double price;

    @Version
    Long version;
}
