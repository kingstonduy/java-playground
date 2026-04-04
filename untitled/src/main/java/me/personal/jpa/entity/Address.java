package me.personal.jpa.entity;

import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * PHASE 5 — @Embeddable (Value Object)
 *
 * An @Embeddable is NOT an entity — it has no @Id, no own table.
 * Its fields are embedded into the parent entity's table.
 *
 * When User has @Embedded Address, the users table gets columns:
 *   street, city, zip_code
 *
 * Use @Embeddable for value objects that don't have their own identity
 * (e.g., Address, Money, DateRange).
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {

    String street;
    String city;
    String zipCode;
}
