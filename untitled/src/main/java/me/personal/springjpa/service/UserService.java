package me.personal.springjpa.service;

import lombok.RequiredArgsConstructor;
import me.personal.springjpa.entity.Post;
import me.personal.springjpa.entity.User;
import me.personal.springjpa.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer — business logic + transaction management.
 *
 * @Transactional — Spring wraps the method in a DB transaction:
 * - begin() before method
 * - commit() on success
 * - rollback() on RuntimeException
 * @Transactional(readOnly = true) — hint to Hibernate to skip dirty checking
 * (better performance for read-only operations).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByIdWithPosts(Long id) {
        return userRepository.findByIdWithPosts(id);
    }

    @Transactional
    public User create(String name, String email, int age) {
        User user = User.builder()
                .name(name)
                .email(email)
                .age(age)
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public User addPost(Long userId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Post post = Post.builder()
                .title(title)
                .content(content)
                .build();

        user.addPost(post);  // bidirectional helper
        return userRepository.save(user);  // cascades to Post
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
}
