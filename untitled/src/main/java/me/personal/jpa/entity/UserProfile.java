package me.personal.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @OneToOne example — each User has exactly one UserProfile.
 *
 * This is the OWNING side (has @JoinColumn).
 * The FK column "user_id" is stored in this table.
 */
@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String bio;

    String avatarUrl;

    /**
     * @OneToOne — owning side.
     * @JoinColumn creates the FK column "user_id" in this table.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    User user;
}
