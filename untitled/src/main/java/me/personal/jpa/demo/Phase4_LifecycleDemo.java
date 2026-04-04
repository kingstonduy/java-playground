package me.personal.jpa.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import me.personal.jpa.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * =====================================================================
 *  PHASE 4 — Entity Lifecycle, Dirty Checking, Transactions
 * =====================================================================
 *
 * Entity States:
 *
 *   NEW (Transient)
 *     → just created with "new", not yet known to JPA
 *     → no ID, not in database
 *
 *   MANAGED (Persistent)
 *     → attached to an EntityManager
 *     → changes are AUTO-DETECTED (dirty checking)
 *     → gets synced to DB on flush/commit
 *
 *   DETACHED
 *     → was managed, but EntityManager was closed or entity was detached
 *     → changes are NOT tracked anymore
 *     → use merge() to re-attach
 *
 *   REMOVED
 *     → scheduled for deletion
 *     → will be deleted from DB on flush/commit
 *
 *
 *   Transitions:
 *     new User()          → NEW
 *     em.persist(user)    → NEW → MANAGED
 *     em.find(id)         → MANAGED (loaded from DB)
 *     em.close()          → MANAGED → DETACHED
 *     em.detach(user)     → MANAGED → DETACHED
 *     em.merge(user)      → DETACHED → MANAGED (returns new managed copy!)
 *     em.remove(user)     → MANAGED → REMOVED
 *
 *
 * Dirty Checking:
 *     When an entity is MANAGED, Hibernate keeps a snapshot of its original state.
 *     At flush time, it compares current state vs snapshot.
 *     If anything changed → generates UPDATE SQL automatically.
 *     You never call "update()" — just modify the object!
 *
 *
 * First-Level Cache (L1):
 *     Each EntityManager has its own cache.
 *     em.find(User.class, 1L) twice → only ONE SQL query (second is from cache).
 *     Cache is cleared when EntityManager is closed.
 */
public class Phase4_LifecycleDemo {

    private static final Logger log = LoggerFactory.getLogger(Phase4_LifecycleDemo.class);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-demo");

        try {
            lifecycleDemo(emf);
            dirtyCheckingDemo(emf);
            detachMergeDemo(emf);
            firstLevelCacheDemo(emf);
        } finally {
            emf.close();
        }
    }

    /**
     * Entity lifecycle transitions: NEW → MANAGED → DETACHED → re-MANAGED
     */
    static void lifecycleDemo(EntityManagerFactory emf) {
        log.info("\n========== Entity Lifecycle ==========");

        // NEW — just created, not known to JPA
        User user = User.builder().name("Dave").email("dave@example.com").age(28).build();
        log.info("1. NEW: user.id = {} (no ID yet)", user.getId());

        // MANAGED — persist attaches it to the EntityManager
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        log.info("2. MANAGED: user.id = {} (ID assigned after persist)", user.getId());

        // Still MANAGED — changes are tracked
        boolean isManaged = em.contains(user);
        log.info("3. em.contains(user) = {} (is managed)", isManaged);

        em.getTransaction().commit();

        // DETACHED — closing the EntityManager detaches all entities
        em.close();
        log.info("4. DETACHED: em is closed, user is no longer managed");
    }

    /**
     * Dirty checking — Hibernate auto-detects and persists changes
     */
    static void dirtyCheckingDemo(EntityManagerFactory emf) {
        log.info("\n========== Dirty Checking ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = em.find(User.class, 1L);         // MANAGED
        log.info("Before: name = {}, age = {}", user.getName(), user.getAge());

        user.setName("Dave Modified");                 // just modify the object
        user.setAge(29);

        // NO em.merge() or em.update() needed!
        // Hibernate compares current state vs original snapshot at commit time.
        // It detects the changes and generates:
        //   UPDATE users SET name='Dave Modified', user_age=29 WHERE id=1

        em.getTransaction().commit();                  // UPDATE is generated here
        em.close();

        // Verify
        EntityManager em2 = emf.createEntityManager();
        User reloaded = em2.find(User.class, 1L);
        log.info("After: name = {}, age = {}", reloaded.getName(), reloaded.getAge());
        em2.close();
    }

    /**
     * Detach and merge — working with detached entities
     */
    static void detachMergeDemo(EntityManagerFactory emf) {
        log.info("\n========== Detach & Merge ==========");

        // Load and detach
        EntityManager em1 = emf.createEntityManager();
        User user = em1.find(User.class, 1L);         // MANAGED
        em1.close();                                    // now DETACHED

        // Modify while detached — changes NOT tracked
        user.setName("Dave Detached Edit");
        log.info("Modified while detached: {}", user.getName());

        // Merge — re-attach to a new EntityManager
        EntityManager em2 = emf.createEntityManager();
        em2.getTransaction().begin();

        User managedCopy = em2.merge(user);            // returns a NEW managed copy!
        // IMPORTANT: user is still detached! managedCopy is managed.
        // Always use the returned object after merge.

        log.info("user == managedCopy? {}", user == managedCopy);  // false!
        log.info("managedCopy is managed? {}", em2.contains(managedCopy));  // true

        em2.getTransaction().commit();                 // UPDATE is generated
        em2.close();

        // Verify
        EntityManager em3 = emf.createEntityManager();
        User reloaded = em3.find(User.class, 1L);
        log.info("After merge: name = {}", reloaded.getName());
        em3.close();
    }

    /**
     * First-level cache — same EntityManager returns cached entity
     */
    static void firstLevelCacheDemo(EntityManagerFactory emf) {
        log.info("\n========== First-Level Cache ==========");
        EntityManager em = emf.createEntityManager();

        log.info("First find (hits DB):");
        User user1 = em.find(User.class, 1L);         // SQL query executed

        log.info("Second find (from cache, NO SQL):");
        User user2 = em.find(User.class, 1L);         // no SQL — from L1 cache

        log.info("Same object? {} (both point to the same managed instance)", user1 == user2);

        em.close();
    }
}
