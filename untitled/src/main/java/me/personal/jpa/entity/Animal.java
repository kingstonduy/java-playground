package me.personal.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * PHASE 5 — Inheritance Mapping
 *
 * 3 strategies for mapping class inheritance to database tables:
 *
 * 1. SINGLE_TABLE (default, used here):
 *    - ONE table for all subclasses: "animals"
 *    - Discriminator column "animal_type" tells Hibernate which subclass
 *    - Pros: fastest queries (no joins), simple
 *    - Cons: nullable columns (Dog has bark_volume, Cat doesn't)
 *
 * 2. JOINED:
 *    - Separate table per class: animals, dogs, cats
 *    - dogs table has FK to animals table
 *    - Pros: normalized, no nulls
 *    - Cons: requires JOINs = slower queries
 *
 * 3. TABLE_PER_CLASS:
 *    - Completely separate tables: dogs, cats (no animals table)
 *    - Each table has ALL columns (including inherited ones)
 *    - Pros: no joins, no nulls
 *    - Cons: can't query all Animals efficiently, duplicated columns
 */
@Entity
@Table(name = "animals")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "animal_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    Integer age;
}
