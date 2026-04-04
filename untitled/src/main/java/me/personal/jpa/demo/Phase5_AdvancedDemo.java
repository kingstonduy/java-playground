package me.personal.jpa.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Persistence;
import me.personal.jpa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * =====================================================================
 *  PHASE 5 — Advanced: Inheritance, Embeddable, Optimistic Locking
 * =====================================================================
 */
public class Phase5_AdvancedDemo {

    private static final Logger log = LoggerFactory.getLogger(Phase5_AdvancedDemo.class);

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa-demo");

        try {
            inheritanceDemo(emf);
            embeddableDemo(emf);
            optimisticLockingDemo(emf);
        } finally {
            emf.close();
        }
    }

    /**
     * Inheritance — SINGLE_TABLE strategy
     *
     * One "animals" table stores Dogs and Cats.
     * Column "animal_type" discriminates between them.
     *
     * Table looks like:
     *   | id | animal_type | name   | age | bark_volume | indoor |
     *   |----|-------------|--------|-----|-------------|--------|
     *   | 1  | DOG         | Rex    | 5   | 8           | NULL   |
     *   | 2  | CAT         | Whiskers| 3  | NULL        | true   |
     */
    static void inheritanceDemo(EntityManagerFactory emf) {
        log.info("\n========== Inheritance (SINGLE_TABLE) ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Dog dog = Dog.builder().name("Rex").age(5).barkVolume(8).build();
        Cat cat = Cat.builder().name("Whiskers").age(3).indoor(true).build();

        em.persist(dog);
        em.persist(cat);
        em.getTransaction().commit();
        em.close();

        // Query ALL animals (polymorphic query)
        EntityManager em2 = emf.createEntityManager();
        List<Animal> allAnimals = em2.createQuery("SELECT a FROM Animal a", Animal.class)
                .getResultList();

        log.info("All animals ({}):", allAnimals.size());
        for (Animal a : allAnimals) {
            if (a instanceof Dog d) {
                log.info("  Dog: {} (bark volume: {})", d.getName(), d.getBarkVolume());
            } else if (a instanceof Cat c) {
                log.info("  Cat: {} (indoor: {})", c.getName(), c.getIndoor());
            }
        }

        // Query only Dogs
        List<Dog> dogs = em2.createQuery("SELECT d FROM Dog d", Dog.class)
                .getResultList();
        log.info("Only dogs: {}", dogs.size());

        em2.close();
    }

    /**
     * @Embeddable — Address is embedded into User's table (no separate table)
     */
    static void embeddableDemo(EntityManagerFactory emf) {
        log.info("\n========== @Embeddable ==========");
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        // Address fields become columns in the "users" table
        Address address = Address.builder()
                .street("123 Main St")
                .city("Springfield")
                .zipCode("62701")
                .build();

        User user = User.builder()
                .name("Eve")
                .email("eve@example.com")
                .age(27)
                .address(address)    // embedded, not a relationship
                .build();

        em.persist(user);
        em.getTransaction().commit();
        em.close();

        // Read back — address is loaded as part of User (no join needed)
        EntityManager em2 = emf.createEntityManager();
        User loaded = em2.find(User.class, user.getId());
        log.info("User: {} | Address: {}, {}, {}",
                loaded.getName(),
                loaded.getAddress().getStreet(),
                loaded.getAddress().getCity(),
                loaded.getAddress().getZipCode());

        // Query by embedded field
        List<User> springfieldUsers = em2.createQuery(
                        "SELECT u FROM User u WHERE u.address.city = :city", User.class)
                .setParameter("city", "Springfield")
                .getResultList();
        log.info("Users in Springfield: {}", springfieldUsers.size());

        em2.close();
    }

    /**
     * @Version — Optimistic Locking
     *
     * Prevents "lost updates" when two transactions modify the same row.
     * Instead of locking the row (pessimistic), it checks version on UPDATE.
     */
    static void optimisticLockingDemo(EntityManagerFactory emf) {
        log.info("\n========== @Version (Optimistic Locking) ==========");

        // Create a product
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();
        Product product = Product.builder().name("Laptop").price(999.99).build();
        em1.persist(product);
        em1.getTransaction().commit();
        em1.close();

        log.info("Created product: {} (version: {})", product.getName(), product.getVersion());

        // Simulate two concurrent transactions reading the same product

        // Transaction A: reads product (version = 0)
        EntityManager emA = emf.createEntityManager();
        emA.getTransaction().begin();
        Product productA = emA.find(Product.class, product.getId());
        log.info("Transaction A reads product (version: {})", productA.getVersion());

        // Transaction B: reads the same product (version = 0)
        EntityManager emB = emf.createEntityManager();
        emB.getTransaction().begin();
        Product productB = emB.find(Product.class, product.getId());
        log.info("Transaction B reads product (version: {})", productB.getVersion());

        // Transaction A: modifies and commits first → version becomes 1
        productA.setPrice(899.99);
        emA.getTransaction().commit();
        emA.close();
        log.info("Transaction A committed (price: {}, new version: {})", productA.getPrice(), productA.getVersion());

        // Transaction B: tries to modify and commit → version mismatch!
        productB.setPrice(799.99);
        try {
            emB.getTransaction().commit();  // FAILS: version in DB is 1, but B thinks it's 0
            log.info("Transaction B committed (should not happen)");
        } catch (Exception e) {
            log.error("Transaction B FAILED: {} — {}", e.getClass().getSimpleName(), e.getMessage());
            log.info("This is expected! Optimistic locking prevented a lost update.");
            if (emB.getTransaction().isActive()) {
                emB.getTransaction().rollback();
            }
        }
        emB.close();

        // Final state: Transaction A's price wins
        EntityManager em3 = emf.createEntityManager();
        Product finalProduct = em3.find(Product.class, product.getId());
        log.info("Final price: {} (version: {})", finalProduct.getPrice(), finalProduct.getVersion());
        em3.close();
    }
}
