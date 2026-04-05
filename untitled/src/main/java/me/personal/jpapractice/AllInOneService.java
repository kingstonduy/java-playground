package me.personal.jpapractice;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.personal.jpapractice.author.AuthorEntity;
import me.personal.jpapractice.author.AuthorRepository;
import me.personal.jpapractice.book.BookEntity;
import me.personal.jpapractice.book.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AllInOneService {

    AuthorRepository authorRepository;
    BookRepository bookRepository;
    EntityManager em;

    // ─────────────────────────────────────────────
    // Task 1: Persist 3 authors and 5 books
    // ─────────────────────────────────────────────
    @Transactional
    public void task1() {
        List<AuthorEntity> authors = List.of(
                new AuthorEntity().setName("Author 1").setEmail("author1@example.com").setBirthDate(LocalDate.of(1970, 1, 1)),
                new AuthorEntity().setName("Author 2").setEmail("author2@example.com").setBirthDate(LocalDate.of(1980, 5, 15)),
                new AuthorEntity().setName("Author 3").setEmail("author3@example.com").setBirthDate(LocalDate.of(1990, 9, 20))
        );

        List<BookEntity> books = List.of(
                new BookEntity().setTitle("Book 1").setIsbn("ISBN001").setPrice(BigDecimal.valueOf(19.99)).setPublishedDate(LocalDate.of(2020, 1, 1)).setPageCount(100),
                new BookEntity().setTitle("Book 2").setIsbn("ISBN002").setPrice(BigDecimal.valueOf(24.99)).setPublishedDate(LocalDate.of(2020, 6, 1)).setPageCount(200),
                new BookEntity().setTitle("Book 3").setIsbn("ISBN003").setPrice(BigDecimal.valueOf(29.99)).setPublishedDate(LocalDate.of(2021, 1, 1)).setPageCount(300),
                new BookEntity().setTitle("Book 4").setIsbn("ISBN004").setPrice(BigDecimal.valueOf(34.99)).setPublishedDate(LocalDate.of(2021, 6, 1)).setPageCount(400),
                new BookEntity().setTitle("Book 5").setIsbn("ISBN005").setPrice(BigDecimal.valueOf(39.99)).setPublishedDate(LocalDate.of(2022, 1, 1)).setPageCount(500)
        );

        authorRepository.saveAll(authors);
        bookRepository.saveAll(books);

        System.out.println("✅ Task 1: Saved " + authors.size() + " authors and " + books.size() + " books");
    }

    // ─────────────────────────────────────────────
    // Task 2: Find a book by ID
    // ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public void task2(Long bookId) {
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        System.out.println("✅ Task 2: Found book → " + book.getTitle() + " | price: " + book.getPrice());
    }

    // ─────────────────────────────────────────────
    // Task 3: Update book price via dirty checking
    // ─────────────────────────────────────────────
    @Transactional
    public void task3(Long bookId) {
        // Load entity — now it's MANAGED by Hibernate Session
        BookEntity book = em.find(BookEntity.class, bookId);

        System.out.println("Task 3: Price before → " + book.getPrice());

        // Just set the value — NO save() or update() call needed
        // Hibernate detects the change at flush time (end of transaction)
        book.setPrice(BigDecimal.valueOf(99.99));

        System.out.println("✅ Task 3: Price updated via dirty checking → " + book.getPrice());
        // Hibernate will auto-emit: UPDATE books SET price=99.99 WHERE id=?
    }

    // ─────────────────────────────────────────────
    // Task 4: Delete an author by ID
    // ─────────────────────────────────────────────
    @Transactional
    public void task4(Long authorId) {
        AuthorEntity author = authorRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found: " + authorId));

        authorRepository.delete(author);

        System.out.println("✅ Task 4: Deleted author → " + author.getName());
    }

    // ─────────────────────────────────────────────
    // Task 5: Verify L1 cache — same object reference
    // ─────────────────────────────────────────────
    @Transactional
    public void task5(Long bookId) {
        // First call — hits the DB
        BookEntity first = em.find(BookEntity.class, bookId);
        System.out.println("Task 5: First  call  → fetched from DB,  hashCode: " + System.identityHashCode(first));

        // Second call — Hibernate returns the SAME instance from L1 cache
        // No SQL is fired
        BookEntity second = em.find(BookEntity.class, bookId);
        System.out.println("Task 5: Second call → fetched from L1 cache, hashCode: " + System.identityHashCode(second));

        // Both references point to the exact same object in memory
        boolean sameReference = (first == second);
        System.out.println("✅ Task 5: Same object reference? → " + sameReference); // true
    }
}