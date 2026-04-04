package me.personal.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * =====================================================================
 *  PHASE 1 — Basic Entity Mapping
 * =====================================================================
 *
 * @Entity  — marks this class as a JPA entity (maps to a database table)
 * @Table   — customize table name (optional, defaults to class name)
 * @Id      — marks the primary key field
 * @GeneratedValue — auto-generate the ID (IDENTITY = auto-increment)
 * @Column  — customize column name, nullable, unique, length, etc.
 *
 * =====================================================================
 *  PHASE 2 — Relationships
 * =====================================================================
 *
 * @OneToOne  — User has one UserProfile
 * @OneToMany — User has many Posts
 * @ManyToMany — User has many Tags, Tag has many Users
 *
 * mappedBy = "user" means the OTHER side (Post.user) owns the FK column.
 * The "owning side" is the one with the FK / @JoinColumn.
 *
 * CascadeType:
 *   PERSIST  — when you persist User, also persist its Posts
 *   MERGE    — when you merge User, also merge its Posts
 *   REMOVE   — when you delete User, also delete its Posts
 *   ALL      — all of the above + REFRESH + DETACH
 *
 * orphanRemoval = true — if you remove a Post from the list, delete it from DB
 *
 * FetchType:
 *   LAZY  — don't load until accessed (default for collections)
 *   EAGER — load immediately with the parent (default for @OneToOne, @ManyToOne)
 */
@Entity
@Table(name = "users")  // "user" is a reserved keyword in H2
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(unique = true, nullable = false)
    String email;

    @Column(name = "user_age")  // custom column name
    Integer age;

    // --- PHASE 2: Relationships ---

    /**
     * @OneToOne — User has one Profile.
     * cascade = ALL: any operation on User cascades to Profile.
     * The FK column "user_id" will be in the user_profiles table (owned by UserProfile).
     * mappedBy = "user" means UserProfile.user is the owning side.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude  // avoid infinite loop in toString
    UserProfile profile;

    /**
     * @OneToMany — User has many Posts.
     * mappedBy = "author" means Post.author owns the FK.
     * cascade ALL + orphanRemoval: removing a Post from this list deletes it from DB.
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    List<Post> posts = new ArrayList<>();

    /**
     * @ManyToMany — User has many Tags.
     * @JoinTable defines the join table between users and tags.
     * This is the OWNING side (has @JoinTable).
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_tags",                                    // join table name
            joinColumns = @JoinColumn(name = "user_id"),           // FK to this entity
            inverseJoinColumns = @JoinColumn(name = "tag_id")      // FK to the other entity
    )
    @ToString.Exclude
    @Builder.Default
    Set<Tag> tags = new HashSet<>();

    // --- PHASE 5: Embeddable ---

    /**
     * @Embedded — embeds Address fields directly into the users table.
     * No separate table is created. The Address columns become part of this table.
     */
    @Embedded
    Address address;

    // --- PHASE 5: Optimistic Locking ---

    /**
     * @Version — enables optimistic locking.
     * Hibernate checks this value on UPDATE. If another transaction changed it,
     * throws OptimisticLockException (prevents lost updates).
     */
    @Version
    Long version;

    // --- Helper methods for bidirectional relationships ---

    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getUsers().add(this);
    }
}
