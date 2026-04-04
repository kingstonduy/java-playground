package me.personal.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.HashSet;
import java.util.Set;

/**
 * @ManyToMany example — a Tag can belong to many Users, a User can have many Tags.
 *
 * This is the INVERSE side (mappedBy = "tags" refers to User.tags).
 * The join table "user_tags" is defined on the User side.
 */
@Entity
@Table(name = "tags")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String name;

    /**
     * @ManyToMany inverse side.
     * mappedBy = "tags" means User.tags is the owning side.
     * We don't define @JoinTable here — it's already defined on User.
     */
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    Set<User> users = new HashSet<>();
}
