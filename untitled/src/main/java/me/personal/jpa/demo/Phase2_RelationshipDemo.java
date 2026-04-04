package me.personal.jpa.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import me.personal.jpa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * =====================================================================
 *  PHASE 2 — Relationships
 * =====================================================================
 *
 * Relationship types:
 *   @OneToOne   — User ↔ UserProfile (1:1)
 *   @OneToMany  — User → Posts (1:N)
 *   @ManyToOne  — Post → User (N:1, owning side)
 *   @ManyToMany — User ↔ Tag (M:N, via join table)
 *
 * Key concepts:
 *   - Owning side: the side with @JoinColumn (has the FK in its table)
 *   - Inverse side: the side with mappedBy (no FK, just a reference)
 *   - Always set BOTH sides of a bidirectional relationship!
 *   - CascadeType controls what happens to children when parent changes
 *   - FetchType.LAZY = load on demand, EAGER = load immediately
 */
public class Phase2_RelationshipDemo {

    private static final Logger log = LoggerFactory.getLogger(Phase2_RelationshipDemo.class);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-demo");

        try {
            oneToOneDemo(emf);
            oneToManyDemo(emf);
            manyToManyDemo(emf);
        } finally {
            emf.close();
        }
    }

    /**
     * @OneToOne — User has one UserProfile
     */
    static void oneToOneDemo(EntityManagerFactory emf) {
        log.info("\n========== @OneToOne ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = User.builder()
                .name("Bob")
                .email("bob@example.com")
                .age(30)
                .build();

        UserProfile profile = UserProfile.builder()
                .bio("Java developer")
                .avatarUrl("https://example.com/bob.jpg")
                .user(user)           // set owning side
                .build();

        user.setProfile(profile);     // set inverse side too!

        em.persist(user);             // cascade persists profile too (CascadeType.ALL)

        em.getTransaction().commit();
        em.close();

        // Read back
        EntityManager em2 = emf.createEntityManager();
        User loaded = em2.find(User.class, user.getId());
        log.info("User: {} | Profile bio: {}", loaded.getName(), loaded.getProfile().getBio());
        em2.close();
    }

    /**
     * @OneToMany / @ManyToOne — User has many Posts
     */
    static void oneToManyDemo(EntityManagerFactory emf) {
        log.info("\n========== @OneToMany / @ManyToOne ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = em.find(User.class, 1L);  // Bob from previous demo

        // Use helper method to set both sides
        Post post1 = Post.builder().title("Hello World").content("My first post").build();
        Post post2 = Post.builder().title("JPA Guide").content("Learning JPA today").build();

        user.addPost(post1);  // sets post.author = user AND adds to user.posts
        user.addPost(post2);

        // cascade = ALL: persisting user cascades to posts
        em.getTransaction().commit();
        em.close();

        // Read back
        EntityManager em2 = emf.createEntityManager();
        User loaded = em2.find(User.class, user.getId());
        log.info("User {} has {} posts:", loaded.getName(), loaded.getPosts().size());
        loaded.getPosts().forEach(p -> log.info("  - {}", p.getTitle()));
        em2.close();
    }

    /**
     * @ManyToMany — User has many Tags, Tag has many Users
     */
    static void manyToManyDemo(EntityManagerFactory emf) {
        log.info("\n========== @ManyToMany ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = em.find(User.class, 1L);

        Tag java = Tag.builder().name("Java").build();
        Tag hibernate = Tag.builder().name("Hibernate").build();

        user.addTag(java);       // sets both sides
        user.addTag(hibernate);

        // cascade PERSIST: tags are auto-persisted through user
        em.getTransaction().commit();
        em.close();

        // Read back
        EntityManager em2 = emf.createEntityManager();
        User loaded = em2.find(User.class, user.getId());
        log.info("User {} has tags:", loaded.getName());
        loaded.getTags().forEach(t -> log.info("  - {}", t.getName()));
        em2.close();
    }
}
