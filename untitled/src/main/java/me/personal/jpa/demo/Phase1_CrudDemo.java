package me.personal.jpa.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import me.personal.jpa.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * =====================================================================
 *  PHASE 1 — CRUD Operations
 * =====================================================================
 *
 * Core JPA API:
 *   EntityManagerFactory — heavy, one per app (like a DB connection pool)
 *   EntityManager        — lightweight, one per transaction/unit of work
 *
 * CRUD:
 *   persist(entity) — INSERT (entity must be new, no ID yet)
 *   find(Class, id) — SELECT by primary key
 *   merge(entity)   — UPDATE (or INSERT if detached entity doesn't exist)
 *   remove(entity)  — DELETE (entity must be managed)
 */
public class Phase1_CrudDemo {

    private static final Logger log = LoggerFactory.getLogger(Phase1_CrudDemo.class);

    public static void main(String[] args) {
        // Create EntityManagerFactory — reads persistence.xml "jpa-demo" unit
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-demo");

        try {
            create(emf);
            read(emf);
            update(emf);
            delete(emf);
        } finally {
            emf.close();  // always close — releases DB connections
        }
    }

    /**
     * CREATE — persist() inserts a new entity.
     */
    static void create(EntityManagerFactory emf) {
        log.info("\n========== CREATE ==========");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();                // start transaction

        User user = User.builder()
                .name("Alice")
                .email("alice@example.com")
                .age(25)
                .build();

        em.persist(user);                           // INSERT — user gets an ID

        log.info("Persisted user with ID: {}", user.getId());

        em.getTransaction().commit();               // commit transaction
        em.close();
    }

    /**
     * READ — find() loads an entity by primary key.
     */
    static void read(EntityManagerFactory emf) {
        log.info("\n========== READ ==========");
        EntityManager em = emf.createEntityManager();

        User user = em.find(User.class, 1L);       // SELECT WHERE id = 1

        if (user != null) {
            log.info("Found: {} (email: {}, age: {})", user.getName(), user.getEmail(), user.getAge());
        } else {
            log.info("User not found!");
        }

        em.close();
    }

    /**
     * UPDATE — just modify a managed entity, Hibernate auto-detects changes (dirty checking).
     * No explicit "update()" method needed!
     */
    static void update(EntityManagerFactory emf) {
        log.info("\n========== UPDATE ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = em.find(User.class, 1L);       // load (now "managed")
        user.setAge(26);                             // modify — Hibernate detects this
        user.setName("Alice Updated");

        // No em.merge() or em.update() needed!
        // Hibernate will auto-flush the change at commit time.

        em.getTransaction().commit();               // UPDATE is sent here
        em.close();

        log.info("Updated user (dirty checking did the work)");
    }

    /**
     * DELETE — remove() deletes a managed entity.
     */
    static void delete(EntityManagerFactory emf) {
        log.info("\n========== DELETE ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = em.find(User.class, 1L);       // must be managed first
        em.remove(user);                             // DELETE

        em.getTransaction().commit();
        em.close();

        // Verify deletion
        EntityManager em2 = emf.createEntityManager();
        User deleted = em2.find(User.class, 1L);
        log.info("After delete, user is: {}", deleted);  // null
        em2.close();
    }
}
