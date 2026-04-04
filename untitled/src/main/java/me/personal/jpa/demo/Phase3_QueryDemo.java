package me.personal.jpa.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import me.personal.jpa.entity.Post;
import me.personal.jpa.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * =====================================================================
 *  PHASE 3 — Querying
 * =====================================================================
 *
 * 3 ways to query in JPA:
 *
 * 1. JPQL (Java Persistence Query Language)
 *    - SQL-like but uses entity/field names, not table/column names
 *    - "SELECT u FROM User u" not "SELECT * FROM users"
 *    - Portable across databases
 *
 * 2. Criteria API
 *    - Type-safe, programmatic query building
 *    - No string-based queries = no typos, compile-time checking
 *    - Verbose but powerful for dynamic queries
 *
 * 3. Native SQL
 *    - Raw SQL when you need database-specific features
 *    - Bypasses JPQL limitations
 *    - Less portable
 */
public class Phase3_QueryDemo {

    private static final Logger log = LoggerFactory.getLogger(Phase3_QueryDemo.class);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-demo");

        try {
            seedData(emf);
            jpqlDemo(emf);
            criteriaApiDemo(emf);
            nativeSqlDemo(emf);
            paginationDemo(emf);
        } finally {
            emf.close();
        }
    }

    static void seedData(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User alice = User.builder().name("Alice").email("alice@example.com").age(25).build();
        User bob = User.builder().name("Bob").email("bob@example.com").age(30).build();
        User charlie = User.builder().name("Charlie").email("charlie@example.com").age(35).build();

        alice.addPost(Post.builder().title("Alice Post 1").content("Hello").build());
        alice.addPost(Post.builder().title("Alice Post 2").content("World").build());
        bob.addPost(Post.builder().title("Bob Post 1").content("JPA rocks").build());

        em.persist(alice);
        em.persist(bob);
        em.persist(charlie);

        em.getTransaction().commit();
        em.close();
    }

    /**
     * JPQL — SQL-like but uses entity names
     */
    static void jpqlDemo(EntityManagerFactory emf) {
        log.info("\n========== JPQL ==========");
        EntityManager em = emf.createEntityManager();

        // Basic SELECT
        List<User> allUsers = em.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
        log.info("All users: {}", allUsers.size());

        // WHERE with named parameter (:name)
        User alice = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", "alice@example.com")
                .getSingleResult();
        log.info("Found by email: {}", alice.getName());

        // WHERE with comparison
        List<User> olderUsers = em.createQuery(
                        "SELECT u FROM User u WHERE u.age > :minAge ORDER BY u.age", User.class)
                .setParameter("minAge", 28)
                .getResultList();
        log.info("Users older than 28:");
        olderUsers.forEach(u -> log.info("  - {} (age {})", u.getName(), u.getAge()));

        // JOIN — query across relationships
        List<Post> alicePosts = em.createQuery(
                        "SELECT p FROM Post p JOIN p.author a WHERE a.name = :name", Post.class)
                .setParameter("name", "Alice")
                .getResultList();
        log.info("Alice's posts:");
        alicePosts.forEach(p -> log.info("  - {}", p.getTitle()));

        // Aggregate functions
        Long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
        Double avgAge = em.createQuery("SELECT AVG(u.age) FROM User u", Double.class)
                .getSingleResult();
        log.info("Total users: {}, Average age: {}", count, avgAge);

        // LIKE query
        List<User> matched = em.createQuery(
                        "SELECT u FROM User u WHERE u.name LIKE :pattern", User.class)
                .setParameter("pattern", "%li%")    // matches "Alice", "Charlie"
                .getResultList();
        log.info("Names containing 'li': {}", matched.stream().map(User::getName).toList());

        em.close();
    }

    /**
     * Criteria API — type-safe programmatic queries
     */
    static void criteriaApiDemo(EntityManagerFactory emf) {
        log.info("\n========== Criteria API ==========");
        EntityManager em = emf.createEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Basic SELECT
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);          // FROM User
        cq.select(root);                                  // SELECT *

        List<User> all = em.createQuery(cq).getResultList();
        log.info("All users (Criteria): {}", all.size());

        // WHERE with predicate
        CriteriaQuery<User> cq2 = cb.createQuery(User.class);
        Root<User> root2 = cq2.from(User.class);
        cq2.select(root2)
                .where(cb.greaterThan(root2.get("age"), 28))    // WHERE age > 28
                .orderBy(cb.asc(root2.get("age")));              // ORDER BY age ASC

        List<User> older = em.createQuery(cq2).getResultList();
        log.info("Users older than 28 (Criteria):");
        older.forEach(u -> log.info("  - {} (age {})", u.getName(), u.getAge()));

        // Multiple conditions with AND/OR
        CriteriaQuery<User> cq3 = cb.createQuery(User.class);
        Root<User> root3 = cq3.from(User.class);
        Predicate agePredicate = cb.between(root3.get("age"), 25, 32);
        Predicate namePredicate = cb.like(root3.get("name"), "%o%");
        cq3.select(root3).where(cb.and(agePredicate, namePredicate));  // age BETWEEN 25 AND 32 AND name LIKE '%o%'

        List<User> filtered = em.createQuery(cq3).getResultList();
        log.info("Age 25-32 AND name contains 'o': {}", filtered.stream().map(User::getName).toList());

        em.close();
    }

    /**
     * Native SQL — raw SQL queries
     */
    static void nativeSqlDemo(EntityManagerFactory emf) {
        log.info("\n========== Native SQL ==========");
        EntityManager em = emf.createEntityManager();

        // Map result to entity
        @SuppressWarnings("unchecked")
        List<User> users = em.createNativeQuery("SELECT * FROM users WHERE user_age > ?1", User.class)
                .setParameter(1, 28)
                .getResultList();
        log.info("Native SQL - users older than 28:");
        users.forEach(u -> log.info("  - {} (age {})", u.getName(), u.getAge()));

        // Scalar result (not mapped to entity)
        Object[] result = (Object[]) em.createNativeQuery("SELECT COUNT(*), AVG(user_age) FROM users")
                .getSingleResult();
        log.info("Native SQL - count: {}, avg age: {}", result[0], result[1]);

        em.close();
    }

    /**
     * Pagination — setFirstResult + setMaxResults
     */
    static void paginationDemo(EntityManagerFactory emf) {
        log.info("\n========== Pagination ==========");
        EntityManager em = emf.createEntityManager();

        int pageSize = 2;

        // Page 1 (offset 0)
        List<User> page1 = em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                .setFirstResult(0)          // offset (skip N rows)
                .setMaxResults(pageSize)     // limit (take N rows)
                .getResultList();
        log.info("Page 1: {}", page1.stream().map(User::getName).toList());

        // Page 2 (offset 2)
        List<User> page2 = em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                .setFirstResult(pageSize)
                .setMaxResults(pageSize)
                .getResultList();
        log.info("Page 2: {}", page2.stream().map(User::getName).toList());

        em.close();
    }
}
