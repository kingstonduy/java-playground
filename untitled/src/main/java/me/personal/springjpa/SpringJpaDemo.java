package me.personal.springjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot + JPA/Hibernate Demo
 * <p>
 * Unlike the pure-JPA demos (me.personal.jpa), this uses Spring Boot's auto-configuration:
 * - DataSource is configured via application.properties (no persistence.xml needed)
 * - EntityManager is created and managed by Spring
 * - Spring Data JPA repositories provide CRUD + query methods for free
 * - @Transactional handles transaction management declaratively
 * <p>
 * To run: change build.gradle mainClass to 'me.personal.springjpa.SpringJpaDemo'
 * or run: ./gradlew run -PmainClass=me.personal.springjpa.SpringJpaDemo
 */
@SpringBootApplication
public class SpringJpaDemo {

    public static void main(String[] args) {
        SpringApplication.run(SpringJpaDemo.class, args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received shutdown signal, exiting...");
        }));
    }
}
