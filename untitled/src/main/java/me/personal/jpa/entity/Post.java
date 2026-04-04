package me.personal.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @ManyToOne example — many Posts belong to one User (author).
 *
 * This is the OWNING side of the User ↔ Post relationship.
 * The FK column "author_id" is stored in this table.
 *
 * @ManyToOne default fetch = EAGER (loads author with every post).
 * We override to LAZY for performance.
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column(columnDefinition = "TEXT")  // large text column
    String content;

    /**
     * @ManyToOne — owning side.
     * @JoinColumn creates FK "author_id" in this table pointing to users.id
     * fetch = LAZY: don't load the author unless you access post.getAuthor()
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    User author;
}
