package me.personal.jpa.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Cat subclass — stored in "animals" table with animal_type = 'CAT'.
 * The indoor column will be NULL for Dog rows.
 */
@Entity
@DiscriminatorValue("CAT")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cat extends Animal {

    Boolean indoor;

    @Builder
    public Cat(String name, Integer age, Boolean indoor) {
        super(null, name, age);
        this.indoor = indoor;
    }
}
