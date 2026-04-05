package me.personal.springjpa;

import me.personal.springjpa.entity.User;
import me.personal.springjpa.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner — Spring Boot calls run() after the application context is ready.
 * This is where we demo CRUD operations using Spring Data JPA.
 */
@Component
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final UserService userService;

    public DemoRunner(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        log.info("\n============================");
        log.info("  Spring Boot + JPA Demo");
        log.info("============================\n");

        // ===== CREATE =====
        log.info("--- CREATE ---");
        User alice = userService.create("Alice", "alice@example.com", 25);
        User bob = userService.create("Bob", "bob@example.com", 30);
        User charlie = userService.create("Charlie", "charlie@example.com", 22);
        log.info("Created: {}", alice);
        log.info("Created: {}", bob);
        log.info("Created: {}", charlie);

        // ===== READ =====
        log.info("\n--- READ ---");
        userService.findById(alice.getId())
                .ifPresent(u -> log.info("Found by ID: {}", u));
        userService.findByEmail("bob@example.com")
                .ifPresent(u -> log.info("Found by email: {}", u));
        log.info("All users: {}", userService.findAll());

        // ===== ADD POSTS (cascade persist) =====
        log.info("\n--- ADD POSTS ---");
        userService.addPost(alice.getId(), "Hello World", "My first post!");
        userService.addPost(alice.getId(), "Spring JPA", "JPA with Spring Boot is great.");
        userService.addPost(bob.getId(), "Bob's Post", "Hi from Bob.");

        // ===== READ WITH POSTS (JOIN FETCH) =====
        log.info("\n--- READ WITH POSTS (JOIN FETCH) ---");
        userService.findByIdWithPosts(alice.getId())
                .ifPresent(u -> {
                    log.info("User: {} has {} posts", u.getName(), u.getPosts().size());
                    u.getPosts().forEach(p -> log.info("  - {} : {}", p.getTitle(), p.getContent()));
                });

        // ===== DELETE =====
        log.info("\n--- DELETE ---");
        userService.deleteById(charlie.getId());
        log.info("After delete, all users: {}", userService.findAll());

        log.info("\n============================");
        log.info("  Demo complete!");
        log.info("============================");
    }
}
