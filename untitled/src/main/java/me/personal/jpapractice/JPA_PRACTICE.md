# JPA/Hibernate Practice Problems

> **Domain: Online Bookstore**
>
> You are building the persistence layer for an online bookstore. Each problem introduces a new feature requirement that forces you to use a specific JPA/Hibernate concept. Build incrementally — each problem builds on the previous ones.

---

## Problem 1 — Entity Mapping & CRUD

### Requirement

Design and map the following entities:

- **Author**: id (Long, auto-generated), name (String, not null, max 100), email (String, unique), birthDate (LocalDate)
- **Book**: id (Long, auto-generated), title (String, not null), isbn (String, unique, 13 chars), price (BigDecimal), publishedDate (LocalDate), pageCount (Integer)

Write a demo that:
1. Persists 3 authors and 5 books
2. Finds a book by ID
3. Updates a book's price (using dirty checking — no explicit update call)
4. Deletes an author by ID
5. Verifies the L1 cache: call `em.find()` twice with the same ID and assert the same object reference is returned

### Features Tested

- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- `@Column(nullable, unique, length)`
- `persist()`, `find()`, `merge()`, `remove()`
- Dirty checking (modify a managed entity and commit — no update call)
- First-level cache (identity guarantee)

### Solution Sketch

```java
@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true)
    private String email;

    private LocalDate birthDate;
}

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(unique = true, length = 13)
    private String isbn;

    private BigDecimal price;
    private LocalDate publishedDate;
    private Integer pageCount;
}

// CRUD demo
em.getTransaction().begin();
Book book = em.find(Book.class, 1L);
book.setPrice(new BigDecimal("29.99"));  // dirty checking — no em.merge() needed
em.getTransaction().commit();            // UPDATE generated automatically

// L1 cache
Book a = em.find(Book.class, 1L);  // SQL fired
Book b = em.find(Book.class, 1L);  // NO SQL — from cache
assert a == b;                      // same object reference
```

---

## Problem 2 — Relationships (OneToMany, ManyToOne)

### Requirement

An **Author** can write many **Books**. A **Book** has exactly one **Author**.

1. Add a bidirectional `@OneToMany` / `@ManyToOne` relationship between Author and Book
2. Make Author the inverse side (`mappedBy`) and Book the owning side (`@JoinColumn`)
3. Write a helper method `author.addBook(book)` that sets both sides
4. Persist an Author with 3 Books using cascade
5. Remove a Book from the Author's list and verify it's deleted from the DB (orphan removal)

### Features Tested

- `@OneToMany(mappedBy, cascade, orphanRemoval)`
- `@ManyToOne` + `@JoinColumn`
- Bidirectional relationship management (set both sides!)
- `CascadeType.ALL`
- `orphanRemoval = true`

### Solution Sketch

```java
// Author.java
@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Book> books = new ArrayList<>();

public void addBook(Book book) {
    books.add(book);
    book.setAuthor(this);
}

public void removeBook(Book book) {
    books.remove(book);
    book.setAuthor(null);
}

// Book.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "author_id")
private Author author;

// Demo
em.getTransaction().begin();
Author author = new Author("J.K. Rowling", "jk@example.com");
author.addBook(new Book("HP 1", "1234567890123", new BigDecimal("19.99")));
author.addBook(new Book("HP 2", "1234567890124", new BigDecimal("21.99")));
author.addBook(new Book("HP 3", "1234567890125", new BigDecimal("24.99")));
em.persist(author);  // cascades to all 3 books
em.getTransaction().commit();

// orphan removal
em.getTransaction().begin();
author.removeBook(author.getBooks().get(0));  // removed from list → deleted from DB
em.getTransaction().commit();
```

---

## Problem 3 — ManyToMany with Extra Columns

### Requirement

Books belong to multiple **Categories**, and a Category contains many Books. Additionally, you need to track **when** a book was added to a category (`addedDate`) and who added it (`addedBy`).

Since `@ManyToMany` cannot hold extra columns, you must **decompose it** into two `@OneToMany` relationships with an explicit join entity.

1. Create `Category` entity (id, name, description)
2. Create `BookCategory` entity (composite key of bookId + categoryId, addedDate, addedBy)
3. Map the relationships: Book → BookCategory ← Category
4. Persist a Book that belongs to 2 Categories
5. Query: find all books in "Science Fiction" category

### Features Tested

- `@ManyToMany` decomposition pattern
- `@EmbeddedId` or `@IdClass` for composite keys
- `@MapsId` to share the FK as part of the composite PK
- Modeling join tables with extra columns

### Solution Sketch

```java
// Composite key
@Embeddable
public class BookCategoryId implements Serializable {
    private Long bookId;
    private Long categoryId;
    // equals() and hashCode() required!
}

@Entity
@Table(name = "book_categories")
public class BookCategory {
    @EmbeddedId
    private BookCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDate addedDate;
    private String addedBy;
}

// Book.java
@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
private List<BookCategory> bookCategories = new ArrayList<>();

// Category.java
@OneToMany(mappedBy = "category")
private List<BookCategory> bookCategories = new ArrayList<>();
```

---

## Problem 4 — Embeddable Value Objects

### Requirement

An Author has a home **Address** and a business **Address**. An address is not an entity (no ID, no own table) — it is a value object.

1. Create an `@Embeddable` Address class (street, city, state, zipCode, country)
2. Embed it twice in Author using `@AttributeOverrides` to avoid column name conflicts
3. Persist an Author with both addresses
4. Query authors by city using JPQL

### Features Tested

- `@Embeddable` / `@Embedded`
- `@AttributeOverrides` / `@AttributeOverride`
- Value object pattern (no identity, stored in parent's table)

### Solution Sketch

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}

// Author.java
@Embedded
@AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "home_street")),
    @AttributeOverride(name = "city",   column = @Column(name = "home_city")),
    @AttributeOverride(name = "state",  column = @Column(name = "home_state")),
    @AttributeOverride(name = "zipCode",column = @Column(name = "home_zip")),
    @AttributeOverride(name = "country",column = @Column(name = "home_country"))
})
private Address homeAddress;

@Embedded
@AttributeOverrides({
    @AttributeOverride(name = "street", column = @Column(name = "biz_street")),
    @AttributeOverride(name = "city",   column = @Column(name = "biz_city")),
    @AttributeOverride(name = "state",  column = @Column(name = "biz_state")),
    @AttributeOverride(name = "zipCode",column = @Column(name = "biz_zip")),
    @AttributeOverride(name = "country",column = @Column(name = "biz_country"))
})
private Address businessAddress;

// JPQL query by embedded field
em.createQuery(
    "SELECT a FROM Author a WHERE a.homeAddress.city = :city", Author.class)
    .setParameter("city", "London")
    .getResultList();
```

---

## Problem 5 — Inheritance Mapping

### Requirement

Your bookstore sells different types of products:
- **Product** (base): id, name, price
- **PhysicalBook** extends Product: weight (Double), shippingCost (BigDecimal)
- **EBook** extends Product: fileSize (Double), format (String: "PDF", "EPUB", "MOBI")
- **AudioBook** extends Product: duration (Duration), narrator (String)

Implement this using **JOINED** inheritance strategy. Then:
1. Persist one of each type
2. Query all Products (polymorphic query — should return all 3)
3. Query only EBooks
4. Compare: change the strategy to `SINGLE_TABLE` and observe the SQL difference

### Features Tested

- `@Inheritance(strategy = InheritanceType.JOINED)` vs `SINGLE_TABLE`
- `@DiscriminatorColumn`, `@DiscriminatorValue`
- Polymorphic queries
- Trade-offs: JOINED (normalized, slower) vs SINGLE_TABLE (denormalized, faster)

### Solution Sketch

```java
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "product_type")
public abstract class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private BigDecimal price;
}

@Entity
@Table(name = "physical_books")
@DiscriminatorValue("PHYSICAL")
public class PhysicalBook extends Product {
    private Double weight;
    private BigDecimal shippingCost;
}

@Entity
@Table(name = "ebooks")
@DiscriminatorValue("EBOOK")
public class EBook extends Product {
    private Double fileSize;
    private String format;
}

@Entity
@Table(name = "audio_books")
@DiscriminatorValue("AUDIO")
public class AudioBook extends Product {
    private Long durationSeconds;
    private String narrator;
}

// Polymorphic query — returns ALL product types
em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
// SQL (JOINED): SELECT ... FROM products p
//   LEFT JOIN physical_books pb ON p.id = pb.id
//   LEFT JOIN ebooks e ON p.id = e.id
//   LEFT JOIN audio_books ab ON p.id = ab.id

// Query only EBooks
em.createQuery("SELECT e FROM EBook e", EBook.class).getResultList();
// SQL (JOINED): SELECT ... FROM products p JOIN ebooks e ON p.id = e.id
```

---

## Problem 6 — JPQL & Criteria API

### Requirement

Write the following queries using **both JPQL and Criteria API**:

1. Find all books priced between $10 and $50, ordered by price descending
2. Find authors who have written more than 3 books (GROUP BY + HAVING)
3. Find the average book price per category
4. Find books whose title contains a keyword (case-insensitive LIKE)
5. Find authors with no books (LEFT JOIN + IS NULL)
6. Pagination: get page 2 of books (10 per page), sorted by title

### Features Tested

- JPQL: `WHERE`, `ORDER BY`, `GROUP BY`, `HAVING`, `JOIN`, `LEFT JOIN`, `LIKE`, `BETWEEN`
- Criteria API: `CriteriaBuilder`, `CriteriaQuery`, `Root`, `Predicate`, `Join`
- `setFirstResult()` / `setMaxResults()` for pagination
- Aggregate functions: `COUNT`, `AVG`, `SUM`

### Solution Sketch

```java
// === JPQL Solutions ===

// 1. Books priced between $10 and $50
em.createQuery(
    "SELECT b FROM Book b WHERE b.price BETWEEN :min AND :max ORDER BY b.price DESC",
    Book.class)
    .setParameter("min", new BigDecimal("10"))
    .setParameter("max", new BigDecimal("50"))
    .getResultList();

// 2. Authors with more than 3 books
em.createQuery(
    "SELECT a FROM Author a WHERE SIZE(a.books) > 3", Author.class)
    .getResultList();
// OR with explicit GROUP BY:
em.createQuery(
    "SELECT a.name, COUNT(b) FROM Author a JOIN a.books b GROUP BY a.name HAVING COUNT(b) > 3")
    .getResultList();

// 3. Average book price per category
em.createQuery(
    "SELECT c.name, AVG(b.price) FROM Category c " +
    "JOIN c.bookCategories bc JOIN bc.book b GROUP BY c.name")
    .getResultList();

// 4. Case-insensitive search
em.createQuery(
    "SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(:keyword)", Book.class)
    .setParameter("keyword", "%harry%")
    .getResultList();

// 5. Authors with no books
em.createQuery(
    "SELECT a FROM Author a LEFT JOIN a.books b WHERE b IS NULL", Author.class)
    .getResultList();

// 6. Pagination (page 2, 10 per page)
em.createQuery("SELECT b FROM Book b ORDER BY b.title", Book.class)
    .setFirstResult(10)    // skip first page (0-9)
    .setMaxResults(10)     // take 10
    .getResultList();

// === Criteria API Solutions ===

CriteriaBuilder cb = em.getCriteriaBuilder();

// 1. Books priced between $10 and $50
CriteriaQuery<Book> cq = cb.createQuery(Book.class);
Root<Book> book = cq.from(Book.class);
cq.select(book)
    .where(cb.between(book.get("price"), new BigDecimal("10"), new BigDecimal("50")))
    .orderBy(cb.desc(book.get("price")));
em.createQuery(cq).getResultList();

// 2. Authors with more than 3 books
CriteriaQuery<Author> aq = cb.createQuery(Author.class);
Root<Author> author = aq.from(Author.class);
aq.select(author)
    .where(cb.greaterThan(cb.size(author.get("books")), 3));
em.createQuery(aq).getResultList();

// 4. Case-insensitive LIKE
cq = cb.createQuery(Book.class);
book = cq.from(Book.class);
cq.select(book)
    .where(cb.like(cb.lower(book.get("title")), "%harry%"));
em.createQuery(cq).getResultList();

// 5. Authors with no books
aq = cb.createQuery(Author.class);
author = aq.from(Author.class);
Join<Author, Book> bookJoin = author.join("books", JoinType.LEFT);
aq.select(author).where(cb.isNull(bookJoin.get("id")));
em.createQuery(aq).getResultList();
```

---

## Problem 7 — N+1 Problem & Fetch Strategies

### Requirement

You have a page that shows all authors with their books and categories.

1. Write a naive query that triggers the N+1 problem. Enable `hibernate.show_sql=true` and count the queries.
2. Fix it with `JOIN FETCH`
3. Fix it with `@EntityGraph`
4. Fix it with `@BatchSize`
5. Compare the SQL generated by each approach

**Constraint:** You must demonstrate the problem first (show N+1 queries in the log), then show each solution reduces it.

### Features Tested

- N+1 problem understanding
- `JOIN FETCH` in JPQL
- `@EntityGraph` (named and ad-hoc)
- `@BatchSize` on collections
- `hibernate.show_sql` / `hibernate.generate_statistics`

### Solution Sketch

```java
// PROBLEM: N+1
List<Author> authors = em.createQuery("SELECT a FROM Author a", Author.class)
    .getResultList();  // 1 query
for (Author a : authors) {
    a.getBooks().size();  // N queries!
}

// FIX 1: JOIN FETCH
List<Author> authors = em.createQuery(
    "SELECT DISTINCT a FROM Author a JOIN FETCH a.books", Author.class)
    .getResultList();  // 1 query with JOIN

// FIX 2: @EntityGraph
@NamedEntityGraph(
    name = "Author.withBooks",
    attributeNodes = @NamedAttributeNode("books")
)
@Entity
public class Author { ... }

// usage
EntityGraph<?> graph = em.getEntityGraph("Author.withBooks");
List<Author> authors = em.createQuery("SELECT a FROM Author a", Author.class)
    .setHint("jakarta.persistence.fetchgraph", graph)
    .getResultList();

// FIX 3: @BatchSize (on the collection)
@OneToMany(mappedBy = "author")
@BatchSize(size = 10)  // loads books for 10 authors at once
private List<Book> books;
// Instead of N queries, fires ceil(N/10) queries
```

---

## Problem 8 — Optimistic Locking & Versioning

### Requirement

Your bookstore has an inventory system. Two admins might update the same book's stock at the same time. Implement optimistic locking to prevent lost updates.

1. Add a `@Version` field to the Book entity
2. Simulate a concurrent modification:
   - Transaction A reads book (version=0), modifies price
   - Transaction B reads same book (version=0), modifies stock
   - Transaction A commits (success, version→1)
   - Transaction B commits (fails with `OptimisticLockException`)
3. Catch the exception and implement a retry strategy (re-read and re-apply)

### Features Tested

- `@Version` for optimistic locking
- `OptimisticLockException` handling
- Understanding of version-based concurrency control
- Retry pattern for concurrent modifications

### Solution Sketch

```java
@Entity
public class Book {
    // ... other fields
    
    @Version
    private Long version;
    
    private Integer stockQuantity;
}

// Simulate concurrent modification
EntityManager em1 = emf.createEntityManager();
EntityManager em2 = emf.createEntityManager();

// Both read the same book
em1.getTransaction().begin();
Book book1 = em1.find(Book.class, 1L);  // version = 0

em2.getTransaction().begin();
Book book2 = em2.find(Book.class, 1L);  // version = 0

// Both modify
book1.setPrice(new BigDecimal("39.99"));
book2.setStockQuantity(100);

// First commit succeeds
em1.getTransaction().commit();  // version 0 → 1, success

// Second commit fails
try {
    em2.getTransaction().commit();  // WHERE version=0 → 0 rows → FAIL
} catch (OptimisticLockException e) {
    em2.getTransaction().rollback();
    
    // Retry: re-read fresh data, re-apply change
    em2 = emf.createEntityManager();
    em2.getTransaction().begin();
    Book fresh = em2.find(Book.class, 1L);  // version = 1 now
    fresh.setStockQuantity(100);             // re-apply our change
    em2.getTransaction().commit();           // version 1 → 2, success
}
```

---

## Problem 9 — Entity Lifecycle Callbacks

### Requirement

Implement an audit trail without changing business logic:

1. Every entity must automatically track `createdAt`, `updatedAt`, `createdBy`
2. Before persisting any Book, validate that the ISBN is exactly 13 characters
3. After loading any Author from DB, log a message
4. Use `@MappedSuperclass` so the audit fields are inherited by all entities

### Features Tested

- `@PrePersist`, `@PreUpdate`, `@PostLoad`
- `@MappedSuperclass` for shared fields
- `@EntityListeners` for separating cross-cutting concerns
- Validation in lifecycle callbacks

### Solution Sketch

```java
@MappedSuperclass
@EntityListeners(AuditListener.class)
public abstract class AuditableEntity {
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private String createdBy;
}

// Separate listener class
public class AuditListener {
    @PrePersist
    public void prePersist(AuditableEntity entity) {
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setCreatedBy("system");  // or get from security context
    }

    @PreUpdate
    public void preUpdate(AuditableEntity entity) {
        entity.setUpdatedAt(LocalDateTime.now());
    }
}

// Book-specific validation
@Entity
public class Book extends AuditableEntity {
    // ... fields

    @PrePersist
    @PreUpdate
    private void validateIsbn() {
        if (isbn == null || isbn.length() != 13) {
            throw new IllegalStateException("ISBN must be exactly 13 characters");
        }
    }
}

// Author with @PostLoad
@Entity
public class Author extends AuditableEntity {
    @PostLoad
    private void onLoad() {
        System.out.println("Author loaded from DB: " + name);
    }
}
```

---

## Problem 10 — Named Queries & DTO Projections

### Requirement

1. Define 3 `@NamedQuery` on the Book entity for commonly used queries
2. Create a DTO projection: `BookSummaryDTO(title, authorName, price)` — query only these 3 columns, not the full entity
3. Use constructor expression in JPQL (`SELECT NEW ...`)
4. Compare the SQL generated vs loading full entities

### Features Tested

- `@NamedQuery` / `@NamedQueries`
- DTO projections with `SELECT NEW`
- Understanding that projections avoid loading unnecessary columns
- Query optimization awareness

### Solution Sketch

```java
@Entity
@NamedQueries({
    @NamedQuery(
        name = "Book.findByIsbn",
        query = "SELECT b FROM Book b WHERE b.isbn = :isbn"
    ),
    @NamedQuery(
        name = "Book.findExpensive",
        query = "SELECT b FROM Book b WHERE b.price > :minPrice ORDER BY b.price DESC"
    ),
    @NamedQuery(
        name = "Book.countByAuthor",
        query = "SELECT COUNT(b) FROM Book b WHERE b.author.id = :authorId"
    )
})
public class Book { ... }

// Usage
Book book = em.createNamedQuery("Book.findByIsbn", Book.class)
    .setParameter("isbn", "1234567890123")
    .getSingleResult();

// DTO projection — only fetches 3 columns, not full entity
public class BookSummaryDTO {
    private String title;
    private String authorName;
    private BigDecimal price;

    public BookSummaryDTO(String title, String authorName, BigDecimal price) {
        this.title = title;
        this.authorName = authorName;
        this.price = price;
    }
}

List<BookSummaryDTO> summaries = em.createQuery(
    "SELECT NEW me.personal.jpapractice.dto.BookSummaryDTO(b.title, a.name, b.price) " +
    "FROM Book b JOIN b.author a", BookSummaryDTO.class)
    .getResultList();
// SQL: SELECT b.title, a.name, b.price FROM books b JOIN authors a ON ...
// vs full entity: SELECT b.* FROM books b JOIN authors a ON ...
```

---

## Problem 11 — Native Queries & Stored Procedures

### Requirement

1. Write a native SQL query to get the top 5 most expensive books (use database-specific SQL)
2. Write a native query that returns a result set mapped to a `@SqlResultSetMapping`
3. Use `@Formula` to add a computed field on Book: `discountedPrice = price * 0.9`

### Features Tested

- `createNativeQuery()` with entity mapping
- `@SqlResultSetMapping` + `@ColumnResult`
- `@Formula` (Hibernate-specific, not standard JPA)
- When to use native SQL vs JPQL

### Solution Sketch

```java
// Native query with entity mapping
List<Book> top5 = em.createNativeQuery(
    "SELECT * FROM books ORDER BY price DESC LIMIT 5", Book.class)
    .getResultList();

// @SqlResultSetMapping for custom projections
@Entity
@SqlResultSetMapping(
    name = "BookPriceMapping",
    columns = {
        @ColumnResult(name = "title", type = String.class),
        @ColumnResult(name = "price", type = BigDecimal.class)
    }
)
public class Book { ... }

List<Object[]> results = em.createNativeQuery(
    "SELECT title, price FROM books WHERE price > ?1", "BookPriceMapping")
    .setParameter(1, 20)
    .getResultList();

// @Formula — computed column (Hibernate-specific)
@Entity
public class Book {
    private BigDecimal price;

    @Formula("price * 0.9")
    private BigDecimal discountedPrice;  // read-only, computed by SQL
}
// SQL: SELECT ..., (price * 0.9) as formula_0_ FROM books ...
```

---

## Problem 12 — Second-Level Cache

### Requirement

1. Enable Hibernate's second-level cache (use EhCache or Hibernate's built-in)
2. Mark the `Category` entity as cacheable (categories rarely change)
3. Demonstrate: load a Category in EntityManager A, close it, load same Category in EntityManager B — observe NO SQL on second load
4. Demonstrate cache invalidation: update a Category and verify the cache is refreshed

### Features Tested

- `@Cacheable` / `@Cache` (Hibernate)
- L2 cache vs L1 cache (L1 = per EntityManager, L2 = per SessionFactory)
- Cache regions and strategies (`READ_ONLY`, `READ_WRITE`, `NONSTRICT_READ_WRITE`)
- Cache invalidation on update

### Solution Sketch

```java
// persistence.xml additions
// <property name="hibernate.cache.use_second_level_cache" value="true"/>
// <property name="hibernate.cache.region.factory_class" 
//           value="org.hibernate.cache.internal.NoCachingRegionFactory"/>
// For real caching: use EhCache or Caffeine

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Category {
    @Id @GeneratedValue
    private Long id;
    private String name;
}

// Demo
// EM1: loads Category, puts it in L2 cache
EntityManager em1 = emf.createEntityManager();
Category cat1 = em1.find(Category.class, 1L);  // SQL fired, result cached in L2
em1.close();  // L1 cache gone, but L2 still has it

// EM2: loads same Category — served from L2 cache
EntityManager em2 = emf.createEntityManager();
Category cat2 = em2.find(Category.class, 1L);  // NO SQL! Loaded from L2 cache
em2.close();

// Invalidation: updating evicts from L2 cache
EntityManager em3 = emf.createEntityManager();
em3.getTransaction().begin();
Category cat3 = em3.find(Category.class, 1L);
cat3.setName("Updated Name");
em3.getTransaction().commit();  // L2 cache entry evicted/updated
em3.close();
```

---

## Problem 13 — Batch Processing

### Requirement

You need to import 10,000 books from a CSV. Naive `em.persist()` in a loop causes `OutOfMemoryError` because all 10,000 entities stay in the L1 cache.

1. Implement batch insert: flush and clear every 50 entities
2. Enable JDBC batching in Hibernate config
3. Compare memory usage and execution time with vs without batching

### Features Tested

- `em.flush()` + `em.clear()` pattern
- `hibernate.jdbc.batch_size`
- L1 cache memory management
- Bulk operations with JPQL UPDATE/DELETE

### Solution Sketch

```java
// persistence.xml
// <property name="hibernate.jdbc.batch_size" value="50"/>
// <property name="hibernate.order_inserts" value="true"/>

int BATCH_SIZE = 50;
em.getTransaction().begin();
for (int i = 0; i < 10_000; i++) {
    Book book = new Book("Book " + i, generateIsbn(i), randomPrice());
    em.persist(book);

    if (i > 0 && i % BATCH_SIZE == 0) {
        em.flush();   // send INSERT batch to DB
        em.clear();   // detach all entities, free memory
    }
}
em.getTransaction().commit();

// Bulk UPDATE (bypasses entity loading entirely)
em.getTransaction().begin();
int updated = em.createQuery(
    "UPDATE Book b SET b.price = b.price * 1.1 WHERE b.price < :threshold")
    .setParameter("threshold", new BigDecimal("20"))
    .executeUpdate();
em.getTransaction().commit();
System.out.println(updated + " books repriced");

// Bulk DELETE
em.createQuery("DELETE FROM Book b WHERE b.stockQuantity = 0").executeUpdate();
```

---

## Problem 14 — Pessimistic vs Optimistic Locking

### Requirement

Compare both locking strategies on the same scenario: two users buying the last copy of a book.

1. Implement with **optimistic locking** (`@Version`) — second commit fails, must retry
2. Implement with **pessimistic locking** (`LockModeType.PESSIMISTIC_WRITE`) — second transaction blocks until first completes
3. Discuss trade-offs: when to use which?

### Features Tested

- `@Version` (optimistic)
- `em.find(Book.class, id, LockModeType.PESSIMISTIC_WRITE)` (pessimistic)
- `em.lock(entity, LockModeType.PESSIMISTIC_READ)`
- Understanding: optimistic = high concurrency + rare conflicts; pessimistic = frequent conflicts + must not fail

### Solution Sketch

```java
// PESSIMISTIC LOCKING — blocks other transactions
em.getTransaction().begin();
Book book = em.find(Book.class, 1L, LockModeType.PESSIMISTIC_WRITE);
// SQL: SELECT ... FROM books WHERE id=1 FOR UPDATE
// Other transactions trying to read this row will BLOCK

if (book.getStockQuantity() > 0) {
    book.setStockQuantity(book.getStockQuantity() - 1);
}
em.getTransaction().commit();  // lock released

// PESSIMISTIC_READ — allows other reads, blocks writes
Book book = em.find(Book.class, 1L, LockModeType.PESSIMISTIC_READ);
// SQL: SELECT ... FROM books WHERE id=1 FOR SHARE (or LOCK IN SHARE MODE)

// Trade-offs:
// | Aspect            | Optimistic (@Version)          | Pessimistic (FOR UPDATE)        |
// |-------------------|--------------------------------|---------------------------------|
// | Concurrency       | High (no blocking)             | Lower (transactions wait)       |
// | Conflict handling | Retry on exception             | No conflict (blocked)           |
// | Best for          | Read-heavy, rare conflicts     | Write-heavy, frequent conflicts |
// | Deadlock risk     | None                           | Possible                        |
// | Performance       | Better under low contention    | Better under high contention    |
```

---

## Summary: Feature Checklist

| # | Problem | Key JPA/Hibernate Features |
|---|---------|---------------------------|
| 1 | Entity Mapping & CRUD | `@Entity`, `@Column`, `persist/find/merge/remove`, dirty checking, L1 cache |
| 2 | OneToMany/ManyToOne | Bidirectional relationships, `mappedBy`, cascade, orphanRemoval |
| 3 | ManyToMany + Extra Columns | `@EmbeddedId`, `@MapsId`, join entity decomposition |
| 4 | Embeddable Value Objects | `@Embeddable`, `@Embedded`, `@AttributeOverrides` |
| 5 | Inheritance Mapping | `JOINED` vs `SINGLE_TABLE`, `@DiscriminatorColumn`, polymorphic queries |
| 6 | JPQL & Criteria API | Queries, joins, aggregates, pagination, dynamic queries |
| 7 | N+1 Problem & Fetch | `JOIN FETCH`, `@EntityGraph`, `@BatchSize` |
| 8 | Optimistic Locking | `@Version`, `OptimisticLockException`, retry pattern |
| 9 | Lifecycle Callbacks | `@PrePersist`, `@PreUpdate`, `@PostLoad`, `@MappedSuperclass`, `@EntityListeners` |
| 10 | Named Queries & DTOs | `@NamedQuery`, `SELECT NEW`, DTO projections |
| 11 | Native Queries | `createNativeQuery()`, `@SqlResultSetMapping`, `@Formula` |
| 12 | Second-Level Cache | `@Cacheable`, L2 cache config, cache invalidation |
| 13 | Batch Processing | flush/clear pattern, `hibernate.jdbc.batch_size`, bulk UPDATE/DELETE |
| 14 | Pessimistic vs Optimistic | `LockModeType.PESSIMISTIC_WRITE/READ`, comparison of locking strategies |

---

## Suggested Order

1. **Start with Problems 1-2** — get comfortable with entities and relationships
2. **Do 3-4** — learn advanced mapping patterns
3. **Do 5-6** — understand inheritance and querying
4. **Do 7-8** — performance and concurrency (most asked in interviews!)
5. **Do 9-10** — lifecycle and query optimization
6. **Do 11-14** — advanced topics for production readiness

Each problem is self-contained. You can skip around, but the domain model builds progressively.
