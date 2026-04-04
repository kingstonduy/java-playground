# JPA + Hibernate (Without Spring) — Complete Guide

## What is JPA vs Hibernate?

- **JPA (Jakarta Persistence API)** = a specification (interface). It defines annotations like `@Entity`, `@Table`, `@Id` and APIs like `EntityManager`.
- **Hibernate** = an implementation of JPA. It does the actual work (SQL generation, caching, dirty checking).
- Think of it like: JPA = `List` interface, Hibernate = `ArrayList` implementation.

---

## Project Structure

```
jpa/
├── entity/                    # All JPA entities
│   ├── User.java              # Main entity — demonstrates all annotations
│   ├── UserProfile.java       # @OneToOne (owning side)
│   ├── Post.java              # @ManyToOne (owning side)
│   ├── Tag.java               # @ManyToMany (inverse side)
│   ├── Address.java           # @Embeddable (value object, no own table)
│   ├── Animal.java            # Inheritance base class (abstract)
│   ├── Dog.java               # Inheritance subclass
│   ├── Cat.java               # Inheritance subclass
│   └── Product.java           # @Version (optimistic locking)
│
├── demo/                      # Runnable demos, one per phase
│   ├── Phase1_CrudDemo.java   # persist, find, merge, remove
│   ├── Phase2_RelationshipDemo.java
│   ├── Phase3_QueryDemo.java  # JPQL, Criteria API, Native SQL
│   ├── Phase4_LifecycleDemo.java
│   └── Phase5_AdvancedDemo.java
│
└── JPA_HIBERNATE.md           # This file
```

Run any demo by changing `mainClass` in `build.gradle` and running `./gradlew run`.

---

## Phase 1 — Setup & CRUD

### Configuration: `persistence.xml`

Location: `src/main/resources/META-INF/persistence.xml` (JPA looks here by default).

Key settings:
| Property | Purpose |
|---|---|
| `jdbc.url` | Database connection URL |
| `hibernate.hbm2ddl.auto` | Schema generation (`create-drop`, `update`, `validate`, `none`) |
| `hibernate.show_sql` | Print generated SQL (for learning) |

### Core API

```
EntityManagerFactory (heavy, one per app)
  └── EntityManager (lightweight, one per transaction)
        ├── persist(entity)    — INSERT
        ├── find(Class, id)    — SELECT by PK
        ├── merge(entity)      — UPDATE (re-attach detached entity)
        └── remove(entity)     — DELETE
```

### CRUD Operations → `Phase1_CrudDemo.java`

```java
// CREATE
em.persist(user);              // INSERT INTO users ...

// READ
User u = em.find(User.class, 1L);  // SELECT * FROM users WHERE id = 1

// UPDATE (dirty checking — just modify the object!)
user.setName("New Name");     // Hibernate detects change at commit time

// DELETE
em.remove(user);              // DELETE FROM users WHERE id = 1
```

**Key insight:** There is no `em.update()` method. Hibernate uses **dirty checking** — it compares the current state of a managed entity against a snapshot taken when it was loaded. Any differences generate UPDATE SQL automatically.

---

## Phase 2 — Relationships

### Relationship Types → `Phase2_RelationshipDemo.java`

| Annotation | Example | FK Location |
|---|---|---|
| `@OneToOne` | User ↔ UserProfile | `user_profiles.user_id` |
| `@OneToMany` / `@ManyToOne` | User → Posts | `posts.author_id` |
| `@ManyToMany` | User ↔ Tags | `user_tags` join table |

### Owning Side vs Inverse Side

This is the most confusing JPA concept. Here's the rule:

- **Owning side** = the side that has the `@JoinColumn` (the FK column in the DB)
- **Inverse side** = the side with `mappedBy` (just a mirror, no FK)

```java
// OWNING side (Post has the FK column "author_id")
@ManyToOne
@JoinColumn(name = "author_id")
User author;

// INVERSE side (User mirrors the relationship)
@OneToMany(mappedBy = "author")   // "author" = the field name in Post
List<Post> posts;
```

**Rule: always set BOTH sides of a bidirectional relationship.**

```java
// BAD — only sets one side, JPA may not persist correctly
user.getPosts().add(post);

// GOOD — set both sides
user.getPosts().add(post);
post.setAuthor(user);

// BEST — use a helper method (see User.addPost())
user.addPost(post);
```

### Cascade Types

| Type | Effect |
|---|---|
| `PERSIST` | `em.persist(user)` also persists user's posts |
| `MERGE` | `em.merge(user)` also merges user's posts |
| `REMOVE` | `em.remove(user)` also deletes user's posts |
| `ALL` | All of the above + REFRESH + DETACH |

`orphanRemoval = true`: if you remove a Post from `user.getPosts()`, it gets deleted from DB.

### Fetch Types

| Type | When data is loaded | Default for |
|---|---|---|
| `LAZY` | On first access (`user.getPosts()`) | `@OneToMany`, `@ManyToMany` |
| `EAGER` | Immediately with parent query | `@OneToOne`, `@ManyToOne` |

**Best practice:** Always use `LAZY`. Override `EAGER` defaults on `@ManyToOne`:
```java
@ManyToOne(fetch = FetchType.LAZY)
```

### The N+1 Problem

```java
List<User> users = em.createQuery("SELECT u FROM User u").getResultList();  // 1 query
for (User u : users) {
    u.getPosts().size();  // N queries! (one per user, if LAZY)
}
```

Fix with **JOIN FETCH**:
```java
em.createQuery("SELECT u FROM User u JOIN FETCH u.posts").getResultList();  // 1 query
```

---

## Phase 3 — Querying

### 3 Query Approaches → `Phase3_QueryDemo.java`

#### 1. JPQL (most common)

Uses **entity/field names**, not table/column names:

```java
// JPQL uses "User" (entity) and "email" (field), NOT "users" and "email" (column)
em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
  .setParameter("email", "alice@example.com")
  .getSingleResult();

// JOIN across relationships
em.createQuery("SELECT p FROM Post p JOIN p.author a WHERE a.name = :name", Post.class)
  .setParameter("name", "Alice")
  .getResultList();

// Aggregates
em.createQuery("SELECT COUNT(u), AVG(u.age) FROM User u");
```

#### 2. Criteria API (type-safe, good for dynamic queries)

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);

cq.select(root)
  .where(cb.greaterThan(root.get("age"), 28))
  .orderBy(cb.asc(root.get("age")));

List<User> users = em.createQuery(cq).getResultList();
```

#### 3. Native SQL (raw SQL, database-specific)

```java
em.createNativeQuery("SELECT * FROM users WHERE user_age > ?1", User.class)
  .setParameter(1, 28)
  .getResultList();
```

#### Pagination

```java
em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
  .setFirstResult(0)      // offset (skip N rows)
  .setMaxResults(10)       // limit (take N rows)
  .getResultList();
```

---

## Phase 4 — Entity Lifecycle

### States → `Phase4_LifecycleDemo.java`

```
  new User()          persist()         close()/detach()
       │                  │                    │
       ▼                  ▼                    ▼
     [NEW] ──persist()──▶ [MANAGED] ──close()──▶ [DETACHED]
                           │    ▲                     │
                        remove()  merge()          merge()
                           │    │                     │
                           ▼    └─────────────────────┘
                        [REMOVED]
```

| State | Tracked? | In DB? | How to get here |
|---|---|---|---|
| NEW | No | No | `new User()` |
| MANAGED | Yes (dirty checking) | Yes | `persist()`, `find()`, `merge()` |
| DETACHED | No | Yes | `close()`, `detach()` |
| REMOVED | Scheduled for delete | Soon no | `remove()` |

### Dirty Checking

```java
User user = em.find(User.class, 1L);   // MANAGED — Hibernate snapshots the state
user.setName("New Name");               // modify the object
em.getTransaction().commit();           // Hibernate compares, detects change, generates UPDATE
```

No `em.update()` needed. This is one of the most powerful JPA features.

### merge() Gotcha

```java
User detached = ...;                       // loaded from a previous closed EntityManager
User managed = em.merge(detached);         // returns a NEW managed copy

detached == managed  // FALSE! Always use the returned object.
```

### First-Level Cache (L1)

```java
User a = em.find(User.class, 1L);   // SQL query
User b = em.find(User.class, 1L);   // NO SQL — returned from cache
a == b  // TRUE — same object in memory
```

Each `EntityManager` has its own L1 cache. It's cleared when the `EntityManager` is closed.

---

## Phase 5 — Advanced

### Inheritance Mapping → `Phase5_AdvancedDemo.java`

3 strategies:

| Strategy | Tables | Pros | Cons |
|---|---|---|---|
| `SINGLE_TABLE` | 1 table for all | Fast (no joins) | Nullable columns |
| `JOINED` | 1 per class + parent | Normalized | Requires JOINs |
| `TABLE_PER_CLASS` | 1 per concrete class | No joins, no nulls | Can't query parent efficiently |

Our example uses `SINGLE_TABLE`:

```
animals table:
| id | animal_type | name     | age | bark_volume | indoor |
|----|-------------|----------|-----|-------------|--------|
| 1  | DOG         | Rex      | 5   | 8           | NULL   |
| 2  | CAT         | Whiskers | 3   | NULL        | true   |
```

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "animal_type")
public abstract class Animal { ... }

@Entity
@DiscriminatorValue("DOG")
public class Dog extends Animal { Integer barkVolume; }
```

### @Embeddable → Value Objects

For objects with no identity (Address, Money, DateRange):

```java
@Embeddable
public class Address { String street, city, zipCode; }

@Entity
public class User {
    @Embedded
    Address address;  // columns added directly to "users" table
}
```

No separate table. Address fields become columns in the parent table.

### @Version → Optimistic Locking

Prevents lost updates without database locks:

```java
@Version
Long version;
```

How it works:
1. Transaction A reads Product (version = 0)
2. Transaction B reads Product (version = 0)
3. A modifies and commits → `UPDATE ... SET version=1 WHERE id=? AND version=0` → Success
4. B modifies and commits → `UPDATE ... SET version=1 WHERE id=? AND version=0` → 0 rows affected → **OptimisticLockException!**

Transaction A wins, Transaction B must retry.

---

## Deep Dive — How Hibernate Works Under the Hood

### The Full Architecture Stack

```
Your Code
   │
   ▼
EntityManager  (JPA interface — you talk to this)
   │
   ▼
Session  (Hibernate's implementation of EntityManager)
   │  - Holds the Persistence Context (L1 cache + dirty tracking)
   │  - Queues SQL statements (action queue)
   │  - Flushes on commit or when needed
   │
   ▼
SessionFactory  (Hibernate's impl of EntityManagerFactory)
   │  - Heavy object, one per app, thread-safe
   │  - Holds entity metadata, compiled queries, L2 cache config
   │
   ▼
ConnectionPool  (manages physical DB connections)
   │  - Default: Hibernate's built-in pool (not for production)
   │  - Production: HikariCP, C3P0, etc.
   │
   ▼
JDBC Connection  (actual TCP socket to database)
   │
   ▼
Database
```

### EntityManager vs Session

```java
// These are the SAME thing:
EntityManager em = emf.createEntityManager();        // JPA standard
Session session = em.unwrap(Session.class);           // Hibernate native

// EntityManager is a thin wrapper around Session.
// In our code we use EntityManager (portable JPA API).
// But under the hood, it's always a Hibernate Session.
```

### Connection Pool — Idle, Active, Borrowing

A connection pool pre-creates a set of database connections and reuses them.

```
Connection Pool (e.g., HikariCP, size = 10)
┌──────────────────────────────────────────────┐
│  Connection 1  [ACTIVE - used by Thread A]   │
│  Connection 2  [ACTIVE - used by Thread B]   │
│  Connection 3  [IDLE - waiting in pool]      │
│  Connection 4  [IDLE - waiting in pool]      │
│  ...                                         │
│  Connection 10 [IDLE - waiting in pool]      │
└──────────────────────────────────────────────┘
```

| State | Meaning |
|---|---|
| **IDLE** | Connection is open but not in use. Sitting in the pool, waiting. |
| **ACTIVE** | Borrowed by a thread. Currently executing SQL or holding a transaction. |
| **BORROWING** | Thread requests a connection → pool gives an idle one (or creates new if under max). |
| **RETURNING** | Thread finishes → connection goes back to idle state (not closed, just returned). |

**Lifecycle of a connection:**

```
1. App starts → pool creates MIN connections (e.g., 5)     → 5 IDLE
2. Thread A needs DB → borrows Connection 1                 → 1 ACTIVE, 4 IDLE
3. Thread A commits → returns Connection 1                  → 0 ACTIVE, 5 IDLE
4. 10 threads need DB simultaneously → all borrowed         → 10 ACTIVE, 0 IDLE
5. Thread 11 needs DB → WAITS (pool exhausted)             → blocks until one returns
6. If wait > timeout → ConnectionTimeoutException
```

**Key pool settings (HikariCP example):**
```properties
maximumPoolSize=10          # max connections (active + idle)
minimumIdle=5               # keep at least 5 idle connections
connectionTimeout=30000     # wait max 30s for a connection before throwing
idleTimeout=600000          # close idle connections after 10min (if above minimumIdle)
maxLifetime=1800000         # recycle connections every 30min (prevents stale connections)
```

**Hibernate's default pool (what we use in the demos):**
```xml
<property name="hibernate.connection.pool_size" value="20"/>
```
This is a simple built-in pool — fine for learning, **never use in production**. Use HikariCP instead.

### When Does Hibernate Actually Get a Connection?

Hibernate is **lazy about connections**. Creating an `EntityManager` does NOT immediately get a connection:

```java
EntityManager em = emf.createEntityManager();    // NO connection yet!
em.getTransaction().begin();                      // still NO connection!

User user = em.find(User.class, 1L);             // NOW it borrows a connection (needs to run SQL)

em.getTransaction().commit();                     // returns connection to pool
em.close();                                       // cleanup
```

### Why One "Operation" Can Generate Multiple SQL Queries

This is one of the most surprising things about Hibernate. Here are the common cases:

#### Case 1: LAZY Loading (the N+1 problem)

```java
User user = em.find(User.class, 1L);       // Query 1: SELECT * FROM users WHERE id=1
List<Post> posts = user.getPosts();          // Query 2: SELECT * FROM posts WHERE author_id=1
UserProfile profile = user.getProfile();     // Query 3: SELECT * FROM user_profiles WHERE user_id=1
```

You wrote ONE line (`em.find`), but accessing LAZY relationships triggers additional queries.
Each LAZY access = 1 more query = 1 more DB roundtrip.

**In a loop, this explodes:**
```java
List<User> users = em.createQuery("SELECT u FROM User u").getResultList();  // 1 query
for (User u : users) {       // if 100 users:
    u.getPosts().size();      // +100 queries (one per user!)
    u.getProfile().getBio();  // +100 queries
}
// Total: 201 queries for what should be 1 operation!
```

**Fix:** JOIN FETCH loads everything in 1 query:
```java
em.createQuery("SELECT u FROM User u JOIN FETCH u.posts JOIN FETCH u.profile")
// 1 query with JOINs — much faster
```

#### Case 2: @Version (Optimistic Locking) — SELECT then UPDATE

```java
// You write:
product.setPrice(899.99);
em.getTransaction().commit();

// Hibernate generates:
// Query 1: SELECT p.version FROM products p WHERE p.id = ?    (check current version)
// Query 2: UPDATE products SET price=?, version=? WHERE id=? AND version=?  (conditional update)
```

The SELECT is needed to compare versions. If another transaction changed the version,
the UPDATE's WHERE clause won't match any rows → OptimisticLockException.

#### Case 3: Cascade Operations

```java
// You write:
em.persist(user);  // user has 3 posts and 1 profile

// Hibernate generates:
// Query 1: INSERT INTO users ...
// Query 2: INSERT INTO user_profiles ...       (cascade from user → profile)
// Query 3: INSERT INTO posts ... (post 1)      (cascade from user → posts)
// Query 4: INSERT INTO posts ... (post 2)
// Query 5: INSERT INTO posts ... (post 3)
```

One `persist()` = 5 INSERT statements. Each cascaded entity is a separate SQL.

#### Case 4: Dirty Checking at Flush Time

```java
em.getTransaction().begin();

User user1 = em.find(User.class, 1L);    // Query 1: SELECT
User user2 = em.find(User.class, 2L);    // Query 2: SELECT

user1.setName("Changed");
user2.setAge(30);

em.getTransaction().commit();
// Hibernate compares ALL managed entities against their snapshots:
// Query 3: UPDATE users SET name=? WHERE id=1    (user1 changed)
// Query 4: UPDATE users SET age=? WHERE id=2     (user2 changed)
```

Hibernate checks EVERY managed entity at flush time, not just the ones you think you changed.

#### Case 5: Collection Initialization (Size Check)

```java
user.getPosts().size();
// Hibernate must load the ENTIRE collection to count it!
// Query: SELECT * FROM posts WHERE author_id = ?

// Better approach — use a COUNT query:
em.createQuery("SELECT COUNT(p) FROM Post p WHERE p.author.id = :id")
  .setParameter("id", userId)
  .getSingleResult();
// Only returns the number, doesn't load all Post objects
```

#### Case 6: ManyToMany — Join Table Operations

```java
user.addTag(javaTag);
em.getTransaction().commit();

// Hibernate generates:
// Query 1: SELECT from user_tags WHERE user_id=?     (load existing associations)
// Query 2: INSERT INTO user_tags (user_id, tag_id)    (add new association)
```

Hibernate reads the join table first to know what's already there, then inserts the new row.

### Flush Modes — When SQL Actually Hits the Database

Hibernate doesn't send SQL immediately. It queues operations and **flushes** them:

```java
em.persist(user);                // SQL is NOT sent yet! Just queued.
em.persist(post);                // Still queued.

// SQL is sent (flushed) when:
// 1. em.getTransaction().commit()     — always flushes before commit
// 2. em.flush()                       — manual flush
// 3. Before a query that overlaps     — auto-flush to ensure consistency

em.getTransaction().commit();    // NOW all queued INSERTs are sent as a batch
```

**Auto-flush before query:**
```java
em.persist(user);                // queued, not sent
// If you now query users, Hibernate flushes first so the query sees the new user:
em.createQuery("SELECT u FROM User u").getResultList();  // flush happens first!
```

### Summary: Why It Matters

| Situation | Extra queries you might not expect |
|---|---|
| Accessing LAZY relation | +1 SELECT per access |
| Loop + LAZY (N+1) | +N SELECTs |
| `@Version` update | +1 SELECT for version check |
| Cascade persist/merge | +1 INSERT/UPDATE per child |
| `.size()` on collection | Loads full collection |
| ManyToMany add/remove | +1 SELECT on join table |
| Dirty checking | +1 UPDATE per changed entity |

**Tools to catch this:**
- `hibernate.show_sql=true` — see every SQL statement
- `hibernate.generate_statistics=true` — see query counts per session
- Log `org.hibernate.SQL` at DEBUG level for parameterized queries

---

## Quick Reference — Annotation Cheat Sheet

### Entity Mapping
| Annotation | Purpose |
|---|---|
| `@Entity` | Mark class as JPA entity |
| `@Table(name="...")` | Custom table name |
| `@Id` | Primary key |
| `@GeneratedValue` | Auto-generate ID |
| `@Column` | Custom column settings |

### Relationships
| Annotation | FK Location | Default Fetch |
|---|---|---|
| `@OneToOne` | Either side | EAGER |
| `@ManyToOne` | This table | EAGER |
| `@OneToMany` | Other table | LAZY |
| `@ManyToMany` | Join table | LAZY |

### Advanced
| Annotation | Purpose |
|---|---|
| `@Embeddable` / `@Embedded` | Value object (no own table) |
| `@Inheritance` | Map class hierarchy |
| `@DiscriminatorColumn` | Column to distinguish subclasses |
| `@Version` | Optimistic locking |
| `@NamedQuery` | Pre-defined JPQL query |

---

## Running the Demos

Change `mainClass` in `build.gradle`:

```groovy
application {
    mainClass = 'me.personal.jpa.demo.Phase1_CrudDemo'
    // or Phase2_RelationshipDemo, Phase3_QueryDemo, Phase4_LifecycleDemo, Phase5_AdvancedDemo
}
```

Then run:
```bash
./gradlew run
```

All demos use H2 in-memory database — no setup needed, data is reset on each run.
