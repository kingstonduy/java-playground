package me.personal.testing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Optional;

/**
 * Repository that talks to a real database via JPA.
 * Used in Integration Test demos — tests use a real H2 database.
 */
public class UserRepository {

    private final EntityManagerFactory emf;

    public UserRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void save(SimpleUser user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.close();
    }

    public Optional<SimpleUser> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        SimpleUser user = em.find(SimpleUser.class, id);
        em.close();
        return Optional.ofNullable(user);
    }

    public Optional<SimpleUser> findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            SimpleUser user = em.createQuery(
                            "SELECT u FROM SimpleUser u WHERE u.email = :email", SimpleUser.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<SimpleUser> findAll() {
        EntityManager em = emf.createEntityManager();
        List<SimpleUser> users = em.createQuery("SELECT u FROM SimpleUser u", SimpleUser.class)
                .getResultList();
        em.close();
        return users;
    }

    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        SimpleUser user = em.find(SimpleUser.class, id);
        if (user != null) {
            em.remove(user);
        }
        em.getTransaction().commit();
        em.close();
    }

    public SimpleUser update(SimpleUser user) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        SimpleUser merged = em.merge(user);
        em.getTransaction().commit();
        em.close();
        return merged;
    }
}
