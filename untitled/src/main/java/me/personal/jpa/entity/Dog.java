package me.personal.jpa.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Dog subclass — stored in "animals" table with animal_type = 'DOG'.
 * The barkVolume column will be NULL for Cat rows.
 */
@Entity
@DiscriminatorValue("DOG")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Dog extends Animal {

    Integer barkVolume;

    @Builder
    public Dog(String name, Integer age, Integer barkVolume) {
        super(null, name, age);
        this.barkVolume = barkVolume;
    }
}
