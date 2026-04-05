package me.personal.springjpa.repository;

import me.personal.springjpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository — you get CRUD for free, just define the interface.
 *
 * JpaRepository<User, Long> provides:
 *   save(), findById(), findAll(), deleteById(), count(), existsById(), etc.
 *
 * Query derivation — Spring generates SQL from method names:
 *   findByName       → SELECT * FROM users WHERE name = ?
 *   findByAgeGreater → SELECT * FROM users WHERE age > ?
 *
 * @Query — write JPQL or native SQL when method names get too complex.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Query derivation — Spring generates the query from the method name
    Optional<User> findByEmail(String email);

    List<User> findByName(String name);

    List<User> findByAgeGreaterThan(int age);

    List<User> findByNameContainingIgnoreCase(String keyword);

    // Custom JPQL query
    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    List<User> findByAgeBetween(@Param("minAge") int minAge, @Param("maxAge") int maxAge);

    // Custom JPQL with JOIN FETCH (avoids N+1 problem)
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.posts WHERE u.id = :id")
    Optional<User> findByIdWithPosts(@Param("id") Long id);
}
