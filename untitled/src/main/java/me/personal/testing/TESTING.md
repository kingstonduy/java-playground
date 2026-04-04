# Software Testing Guide

A comprehensive guide to software testing: 6 test types, test doubles (Mock, Stub, Spy, Fake, Dummy), and 50+ interview questions with detailed answers.

---

## Table of Contents

1. [Unit Test](#1-unit-test)
2. [Integration Test](#2-integration-test)
3. [White Box Test](#3-white-box-test)
4. [Black Box Test](#4-black-box-test)
5. [System Test (End-to-End)](#5-system-test-end-to-end)
6. [Regression Test](#6-regression-test)
7. [Test Doubles: Mock, Stub, Spy, Fake, and Dummy](#7-test-doubles-mock-stub-spy-fake-and-dummy)
8. [Key Definitions](#8-key-definitions)
9. [The Testing Pyramid](#9-the-testing-pyramid)
10. [Interview Questions and Answers](#10-interview-questions-and-answers)

---

## 1. Unit Test

**File:** `UnitTest_CalculatorTest.java`
**Classes Under Test:** `Calculator`, `BankAccount`

### What is a Unit Test?

A unit test verifies a **single class or method in complete isolation**. There are no databases, no network calls, no file system access, and no external dependencies of any kind. You are testing **pure logic** only.

### Why Write Unit Tests?

- **Fastest tests** — execute in milliseconds because there is no I/O.
- **Easy to debug** — when a unit test fails, you know exactly which method broke.
- **Deterministic** — the same input always produces the same output, every single time. There is no randomness or external state that could cause flaky results.
- **Scalable** — you can run thousands of them in seconds.

### The AAA Pattern

Every unit test in this project follows the **Arrange-Act-Assert** pattern:

| Step | Purpose | Example |
|---|---|---|
| **Arrange** | Set up the data and preconditions | `BankAccount account = new BankAccount("Alice", 100.0);` |
| **Act** | Call the method being tested | `account.deposit(50.0);` |
| **Assert** | Verify the result is correct | `assertThat(account.getBalance()).isEqualTo(150.0);` |

### What This Test Covers

- **`add()`** — Adding positive numbers, negative numbers, and zero. Each scenario is a separate test to isolate failures.
- **`divide()`** — Normal division, integer truncation (7/2 = 3, not 3.5), and division by zero which must throw an `ArithmeticException`.
- **`BankAccount`** — Depositing, withdrawing, rejecting overdrafts (`InsufficientFundsException`), rejecting negative deposits (`IllegalArgumentException`), and transferring between two accounts.

### Key Concept: Exception Testing

```java
assertThatThrownBy(() -> calculator.divide(10, 0))
    .isInstanceOf(ArithmeticException.class)
    .hasMessageContaining("Cannot divide by zero");
```

`assertThatThrownBy` is an AssertJ method that captures the exception thrown by the lambda. It lets you verify both the **type** of exception and the **error message**, ensuring the code fails in exactly the way you expect.

---

## 2. Integration Test

**File:** `IntegrationTest_UserRepositoryTest.java`
**Classes Under Test:** `UserRepository`, `SimpleUser` + JPA/Hibernate + H2 Database

### What is an Integration Test?

An integration test verifies that **multiple components work together correctly**. In this project, the integration being tested is: Java code + JPA/Hibernate (ORM framework) + a real H2 in-memory database. The test proves that your code can actually talk to a database.

### Why Not Just Use Unit Tests?

A unit test with mocks might tell you your repository logic is correct, but it **cannot catch**:

- Wrong SQL or JPQL syntax
- Wrong column names or data types
- Missing database constraints (e.g., unique email)
- Transaction issues
- Hibernate mapping errors

These are **boundary issues** — problems that only appear when real components connect.

### Key Annotations Explained

| Annotation | Meaning |
|---|---|
| `@BeforeAll` | Runs **once** before all tests in the class. Used here to create the `EntityManagerFactory` (expensive — like opening a connection pool). |
| `@AfterAll` | Runs **once** after all tests. Closes the `EntityManagerFactory` to release database connections. |
| `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` | Forces tests to run in a specific order (by `@Order` value). Normally tests run in arbitrary order, but here the tests depend on each other (save first, then find). |
| `@Order(n)` | Specifies the execution order of a test method. `@Order(1)` runs first, `@Order(2)` second, etc. |

### What This Test Covers

The full **CRUD** (Create, Read, Update, Delete) lifecycle:

1. **Save** — inserts a user into the database and verifies the auto-generated ID.
2. **Find by ID** — executes a `SELECT` and verifies the returned data matches.
3. **Find by email** — tests a custom JPQL query.
4. **Find not found** — verifies that a missing record returns `Optional.empty()`.
5. **Find all** — saves multiple users and verifies the full list.
6. **Update** — modifies a field, merges back to the database, and re-reads to confirm.
7. **Delete** — removes a user and verifies they are gone.
8. **Unique constraint** — attempts to save a duplicate email. This test **would pass with mocks** but fails against a real database because the DB enforces the unique constraint.

### Key Difference from Unit Test

| Unit Test | Integration Test |
|---|---|
| `Calculator.add(3, 7)` returns `10` | `UserRepository.save(user)` actually writes to a real database |
| Pure logic, no I/O | Real I/O with a real database |
| Milliseconds | Slower (DB setup/teardown) |

---

## 3. White Box Test

**File:** `WhiteBoxTest_CalculatorTest.java`
**Classes Under Test:** `Calculator` (specifically `getGrade()` and `calculateDiscount()`)

### What is a White Box Test?

A white box test (also called structural testing or glass box testing) is designed by **reading the source code**. You look at the implementation — every `if`, `else`, `switch`, and loop — and write tests to exercise every possible execution path.

### Why Write White Box Tests?

- Ensures **every line of code** is actually executed during testing.
- Catches **dead code** (unreachable branches).
- Finds bugs hidden in **specific code paths** that might never be triggered by typical inputs.
- Maximizes **code coverage**.

### Coverage Levels (Least to Most Thorough)

#### 1. Statement Coverage

Every **line of code** is executed at least once.

This is the weakest level. A single test might achieve high statement coverage but completely miss an important `else` branch.

#### 2. Branch Coverage (Decision Coverage)

Every **`if`/`else` branch** is taken at least once. For each conditional, you test both the `true` path and the `false` path.

**Example from `getGrade()`:**

The source code has this structure:
```
if (score < 0 || score > 100)  → Branch 1 (exception)
if (score >= 90)               → Branch 2 (grade A)
if (score >= 80)               → Branch 3 (grade B)
if (score >= 70)               → Branch 4 (grade C)
if (score >= 60)               → Branch 5 (grade D)
else                           → Branch 6 (grade F)
```

The test provides **at least one input per branch** to ensure each path is exercised: scores of -1, 101, 95, 85, 75, 65, and 50.

**Boundary testing** is also included: testing the exact values where behavior changes (e.g., 89 vs 90, 79 vs 80).

#### 3. Path Coverage

Every **possible combination** of branches is tested. This is the most thorough level but grows exponentially: for `n` independent `if` statements, there are `2^n` paths.

**Example from `calculateDiscount()`:**

The source code has 3 independent conditions:
- Is the customer a member? (yes/no)
- Quantity >= 10? >= 5? < 5? (3 options)
- Price > 1000 AND member? (yes/no)

This creates **9 distinct paths**, and the test covers all 9:

| Path | Member | Quantity | Price > 1000 | Discount |
|---|---|---|---|---|
| 1 | No | < 5 | N/A | 0% |
| 2 | Yes | < 5 | No | 10% |
| 3 | Yes | < 5 | Yes | 15% |
| 4 | No | 5-9 | N/A | 5% |
| 5 | Yes | 5-9 | No | 15% |
| 6 | Yes | 5-9 | Yes | 20% |
| 7 | No | >= 10 | N/A | 15% |
| 8 | Yes | >= 10 | No | 25% |
| 9 | Yes | >= 10 | Yes | 30% |

---

## 4. Black Box Test

**File:** `BlackBoxTest_PasswordValidatorTest.java`
**Class Under Test:** `PasswordValidator`

### What is a Black Box Test?

A black box test is designed based **only on the specification (requirements)**, without ever looking at the source code. You only know: "given this input, I expect this output." The internal implementation is invisible — a "black box."

### Why Write Black Box Tests?

- Tests from the **user's perspective** — how a real user would interact with the system.
- Catches issues that **developer-biased** tests miss (developers who wrote the code tend to test the cases they thought of, not the ones they missed).
- **Implementation-independent** — if the code is refactored but behavior stays the same, all tests still pass.

### Key Difference from White Box

| White Box | Black Box |
|---|---|
| "I see an `if/else` chain with 5 checks" → test each branch | "The spec says STRONG passwords have all 5 criteria" → test that |
| Derived from **code** | Derived from **specification** |

### The Specification Being Tested

A password is **STRONG** if it meets ALL of:
- At least 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (`!@#$%^&*`)

**FAIR** = fails exactly 1 rule. **WEAK** = fails 2 or more rules.

### Black Box Techniques Used

#### Technique 1: Equivalence Partitioning

Divide all possible inputs into **groups (partitions)** that should produce the same result. Then test **one representative** from each group.

| Partition | Representative Input | Expected |
|---|---|---|
| Meets all 5 rules | `Abcdef1!` | STRONG |
| Fails exactly 1 rule (no special char) | `Abcdefg1` | FAIR |
| Fails exactly 1 rule (no digit) | `Abcdefg!` | FAIR |
| Fails 2+ rules | `abc` | WEAK |
| Only lowercase | `abcdefgh` | WEAK |
| Only digits | `12345678` | WEAK |

**Why this works:** If `Abcdef1!` is STRONG, then `Xyz789@#` (same partition) should also be STRONG. Testing one representative from each partition is sufficient.

#### Technique 2: Boundary Value Analysis

Test at the **exact edges** where behavior changes. Bugs are most common at boundaries.

| Input | Length | Expected | Why |
|---|---|---|---|
| `Abc1!xx` | 7 (just below minimum) | FAIR | Fails only the length rule |
| `Abcde1!x` | 8 (at minimum) | STRONG | Meets all rules |
| `Abcde1!xx` | 9 (just above minimum) | STRONG | Safely above the boundary |

#### Technique 3: Edge Cases

Test unusual, extreme, or unexpected inputs:
- `null` and empty string `""` → WEAK
- Single character `"a"` → WEAK
- Very long password (100+ chars) meeting all rules → STRONG

#### Technique 4: Parameterized Tests (Decision Table)

JUnit 5's `@ParameterizedTest` with `@CsvSource` runs the **same test logic with many different inputs**, acting like a decision table:

```java
@CsvSource({
    "Abcdef1!,  STRONG",   // all criteria met
    "ABCDEF1!,  FAIR",     // no lowercase
    "abcdef1!,  FAIR",     // no uppercase
    "abcdefgh,  WEAK",     // no uppercase + no digit + no special
})
void decisionTable(String password, Strength expected) { ... }
```

| Annotation | Meaning |
|---|---|
| `@ParameterizedTest` | Runs the test method once for each set of arguments. |
| `@CsvSource` | Provides arguments as comma-separated values. Each line is one test invocation. |
| `@ValueSource(strings = {...})` | Provides a simple array of strings as arguments. |
| `@NullAndEmptySource` | Automatically adds `null` and `""` as test arguments. |

---

## 5. System Test (End-to-End)

**File:** `SystemTest_OrderProcessorTest.java`
**Classes Under Test:** `OrderProcessor`, `BankAccount`, `Order` (the entire order workflow)

### What is a System Test?

A system test (also called an end-to-end test or E2E test) tests the **entire system/workflow from start to finish**. All components are real — no fakes, no mocks, no shortcuts. It simulates what a real user would experience.

### Why Write System Tests?

Unit tests and integration tests can all pass, but the **complete workflow** can still fail. System tests verify:

- All components **wire together** correctly.
- Business **requirements** are met end-to-end.
- The system is in a **consistent state** after both success and failure.

### What This Test Covers

The full order workflow: **Validate → Calculate → Charge → Record**

#### Happy Path (Success)
1. Customer has $500.
2. Places an order for 2 keyboards at $100 each.
3. The test verifies **every step**:
   - Order recorded with correct item, quantity, price, and status.
   - Tax calculated correctly: $200 * 10% = $20.
   - Total correct: $200 + $20 = $220.
   - Customer balance decreased: $500 - $220 = $280.
   - Merchant balance increased: $0 + $220 = $220.
   - Order stored in the processor's order list.

#### Multiple Orders
Three orders placed sequentially. The test verifies that **state accumulates correctly** — merchant receives all payments, customer's balance decreases by the total of all orders.

#### Failure Scenarios (Consistency After Failure)

This is the most critical part of system testing. When an order **fails**, the test verifies that the system remains in a **consistent state**:

- **Insufficient funds:** Customer balance unchanged, merchant balance unchanged, no order recorded.
- **Invalid item (empty string):** Customer balance unchanged, no order recorded.
- **Invalid quantity (0 or negative):** Exception thrown, no side effects.

### Key Annotation: `@BeforeEach`

```java
@BeforeEach
void setup() {
    merchantAccount = new BankAccount("Shop", 0);
    processor = new OrderProcessor(merchantAccount);
}
```

`@BeforeEach` runs **before every single test method**, giving each test a fresh, clean state. This prevents tests from interfering with each other (unlike the Integration Test which intentionally shares state via `@BeforeAll`).

---

## 6. Regression Test

**File:** `RegressionTest_StringUtilsTest.java`
**Class Under Test:** `StringUtils`

### What is a Regression Test?

A regression test ensures that **previously fixed bugs do not come back**. Every time a bug is fixed, a test is written that:
1. **Fails** when the bug is present (confirming the bug).
2. **Passes** after the fix is applied.
3. **Stays forever** — never deleted, protecting the fix for all future changes.

### Why Write Regression Tests?

- Bugs **love to come back**, especially during refactoring.
- Documents the **bug history** of the codebase — each test tells a story.
- Gives developers **confidence to refactor** without breaking old fixes.

### The Regression Test Workflow

```
1. Bug reported: "reverse(null) throws NullPointerException"
2. Write test:   reverse(null) should return null     ← test FAILS (bug confirmed)
3. Fix the code: add null check
4. Run test:     reverse(null) should return null     ← test PASSES (bug fixed)
5. Keep test forever                                  ← prevents regression
```

### Bugs Documented in This Test

#### Bug #1: `reverse(null)` threw `NullPointerException`
- **Fixed:** 2024-01-15
- **Root cause:** No null check before calling `new StringBuilder(null).reverse()`.
- **Fix:** Added `if (input == null) return null;` at the top.
- **Test:** Verifies `reverse(null)` returns `null` instead of throwing.

#### Bug #2: `isPalindrome("Racecar")` returned `false`
- **Fixed:** 2024-02-20
- **Root cause:** Comparison was case-sensitive — `"Racecar"` does not equal `"racecaR"`.
- **Fix:** Convert to lowercase and strip non-alphanumeric characters before comparing.
- **Test:** Verifies `isPalindrome("Racecar")`, `isPalindrome("RaCeCaR")`, and even `isPalindrome("A man a plan a canal Panama")` all return `true`.

#### Bug #3: `truncate("hi", 10)` threw `StringIndexOutOfBoundsException`
- **Fixed:** 2024-03-10
- **Root cause:** `"hi".substring(0, 10 - 3)` tries to take 7 characters from a 2-character string.
- **Fix:** Return input as-is when its length is less than or equal to `maxLength`.
- **Test:** Verifies `truncate("hi", 10)` returns `"hi"` instead of throwing.

---

## 7. Test Doubles: Mock, Stub, Spy, Fake, and Dummy

### What is a Test Double?

A **test double** is any object that stands in for a real dependency during testing. The term comes from "stunt double" in movies — a replacement that looks like the real thing but is designed for a specific purpose.

When your code depends on external systems (databases, APIs, email services, payment gateways), you replace those dependencies with test doubles so your tests are **fast**, **isolated**, and **deterministic**.

There are 5 types of test doubles, each with a different purpose:

```
                        Test Doubles
                  ┌──────────┼──────────┐
                  │          │          │
               Dummy      Stub      Fake
                          │    │
                        Mock   Spy
```

---

### 7.1 Dummy

#### Definition

A **Dummy** is an object that is passed around but **never actually used**. It exists only to fill a required parameter. You don't care what it does — it's just there to satisfy the compiler.

#### When to Use

When a method requires a parameter that is irrelevant to the behavior you're testing.

#### Example

```java
// We're testing that the order validator checks the item name.
// The BankAccount parameter is required but irrelevant to this specific test.
// So we pass a "dummy" — an object we never actually use.

@Test
void shouldRejectEmptyItemName() {
    BankAccount dummyAccount = new BankAccount("dummy", 0);  // ← DUMMY: never used
    
    assertThatThrownBy(() -> processor.placeOrder(dummyAccount, "", 1, 100.0))
        .isInstanceOf(IllegalArgumentException.class);
}
```

#### Key Characteristics

| Property | Value |
|---|---|
| Has behavior? | No |
| Is called? | No (or its calls are irrelevant) |
| Purpose | Fill a required parameter slot |
| Complexity | Simplest test double |

---

### 7.2 Stub

#### Definition

A **Stub** is an object that returns **pre-configured, hardcoded responses** when its methods are called. It does not verify how it was called — it just provides canned data so the code under test can proceed.

A stub answers the question: **"When the code asks for data, what should it get?"**

#### When to Use

When your code depends on an external service for **data**, and you want to control exactly what data it receives. Common use cases:
- Returning a fixed user from a database query
- Returning a fixed response from an HTTP API
- Returning a fixed configuration value

#### Example

```java
// Real scenario: UserService depends on UserRepository (which hits a database).
// In a unit test, we STUB the repository to return a predetermined user.

// --- The interface ---
public interface UserRepository {
    Optional<User> findById(Long id);
}

// --- The class under test ---
public class UserService {
    private final UserRepository repository;
    
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    public String getUserDisplayName(Long id) {
        return repository.findById(id)
            .map(user -> user.getFirstName() + " " + user.getLastName())
            .orElse("Unknown User");
    }
}

// --- The test with a STUB ---
@Test
void shouldReturnFullName() {
    // Create a STUB — it returns a hardcoded user, no matter what
    UserRepository stubRepository = new UserRepository() {
        @Override
        public Optional<User> findById(Long id) {
            return Optional.of(new User("Alice", "Smith"));  // ← hardcoded response
        }
    };
    
    UserService service = new UserService(stubRepository);
    
    assertThat(service.getUserDisplayName(1L)).isEqualTo("Alice Smith");
}

@Test
void shouldReturnUnknownWhenUserNotFound() {
    // STUB that returns empty — simulates "user not found"
    UserRepository stubRepository = new UserRepository() {
        @Override
        public Optional<User> findById(Long id) {
            return Optional.empty();  // ← hardcoded: not found
        }
    };
    
    UserService service = new UserService(stubRepository);
    
    assertThat(service.getUserDisplayName(999L)).isEqualTo("Unknown User");
}
```

#### With Mockito (Framework)

```java
// Mockito makes creating stubs much easier:
@Test
void shouldReturnFullName() {
    UserRepository stubRepository = mock(UserRepository.class);
    
    // "when X is called, then return Y" — this is STUBBING
    when(stubRepository.findById(1L))
        .thenReturn(Optional.of(new User("Alice", "Smith")));
    
    UserService service = new UserService(stubRepository);
    
    assertThat(service.getUserDisplayName(1L)).isEqualTo("Alice Smith");
}
```

#### Key Characteristics

| Property | Value |
|---|---|
| Has behavior? | Yes — returns pre-configured data |
| Verifies calls? | **No** — does not check how it was called |
| Purpose | Provide canned data to the code under test |
| Focus | **Inputs** to the system under test (what data flows IN) |

---

### 7.3 Mock

#### Definition

A **Mock** is an object that **verifies interactions** — it checks that certain methods were called with the expected arguments, the expected number of times, and in the expected order. A mock is pre-programmed with expectations that are verified at the end of the test.

A mock answers the question: **"Did the code interact with its dependency correctly?"**

#### When to Use

When the **side effect** (the interaction) is what you care about, not the return value. Common use cases:
- Was an email sent?
- Was a notification dispatched?
- Was the audit log written to?
- Was the cache invalidated?

#### Example

```java
// Real scenario: When a user registers, an email should be sent.
// We don't want to send a REAL email in tests.
// We use a MOCK to verify that sendEmail() was called correctly.

// --- The interface ---
public interface EmailService {
    void sendWelcomeEmail(String to, String username);
}

// --- The class under test ---
public class RegistrationService {
    private final UserRepository repository;
    private final EmailService emailService;
    
    public void register(String username, String email) {
        User user = new User(username, email);
        repository.save(user);
        emailService.sendWelcomeEmail(email, username);  // ← side effect we want to verify
    }
}

// --- The test with a MOCK ---
@Test
void shouldSendWelcomeEmailOnRegistration() {
    UserRepository stubRepository = mock(UserRepository.class);
    EmailService mockEmailService = mock(EmailService.class);  // ← MOCK
    
    RegistrationService service = new RegistrationService(stubRepository, mockEmailService);
    service.register("alice", "alice@test.com");
    
    // VERIFY — did the code call sendWelcomeEmail with the right arguments?
    verify(mockEmailService).sendWelcomeEmail("alice@test.com", "alice");
}

@Test
void shouldNotSendEmailIfSaveFails() {
    UserRepository stubRepository = mock(UserRepository.class);
    when(stubRepository.save(any())).thenThrow(new RuntimeException("DB down"));
    
    EmailService mockEmailService = mock(EmailService.class);
    
    RegistrationService service = new RegistrationService(stubRepository, mockEmailService);
    
    assertThatThrownBy(() -> service.register("alice", "alice@test.com"));
    
    // VERIFY — email should NOT have been sent
    verify(mockEmailService, never()).sendWelcomeEmail(any(), any());
}
```

#### Verification Methods in Mockito

```java
verify(mock).method(args);                    // called exactly once
verify(mock, times(3)).method(args);          // called exactly 3 times
verify(mock, never()).method(args);           // never called
verify(mock, atLeastOnce()).method(args);     // called 1 or more times
verify(mock, atMost(5)).method(args);         // called at most 5 times
verifyNoMoreInteractions(mock);              // no other methods called
```

#### Key Characteristics

| Property | Value |
|---|---|
| Has behavior? | Optionally (can also return values like a stub) |
| Verifies calls? | **Yes** — this is its primary purpose |
| Purpose | Verify the code interacted with its dependency correctly |
| Focus | **Outputs** from the system under test (what flows OUT) |

---

### 7.4 Spy

#### Definition

A **Spy** is a **real object** that is wrapped with tracking. It calls the **real methods** by default, but you can:
1. **Override** specific methods to return canned data (partial stubbing).
2. **Verify** that specific methods were called (like a mock).

A spy answers the question: **"I want the real behavior, but I also want to watch what happens."**

#### When to Use

When you need **mostly real behavior** but want to:
- Verify that a specific method was called
- Override one expensive/dangerous method while keeping everything else real
- Track how many times a method was invoked

#### Example

```java
// Real scenario: You have a real list and want to verify interactions with it.

@Test
void spyOnRealList() {
    // Create a SPY on a real ArrayList
    List<String> spyList = spy(new ArrayList<>());
    
    // Real methods still work!
    spyList.add("Alice");
    spyList.add("Bob");
    
    assertThat(spyList).hasSize(2);              // real behavior
    assertThat(spyList.get(0)).isEqualTo("Alice"); // real behavior
    
    // But you can VERIFY interactions (like a mock)
    verify(spyList).add("Alice");
    verify(spyList).add("Bob");
    verify(spyList, times(2)).add(anyString());
}

// More practical example: spy on a real service
@Test
void spyOnService() {
    OrderProcessor realProcessor = new OrderProcessor(new BankAccount("Shop", 0));
    OrderProcessor spyProcessor = spy(realProcessor);
    
    // Override ONE method (the expensive/dangerous one)
    doReturn(new Order(...)).when(spyProcessor).callExternalPaymentGateway(any());
    
    // Everything else is REAL
    spyProcessor.placeOrder(...);
    
    // Verify the payment gateway was called
    verify(spyProcessor).callExternalPaymentGateway(any());
}
```

#### Spy vs Mock — The Critical Difference

```java
// MOCK — everything is fake. Real methods are NEVER called.
List<String> mockList = mock(ArrayList.class);
mockList.add("Alice");                    // does NOTHING (mock swallows the call)
assertThat(mockList).hasSize(0);          // still empty — add() was never really called

// SPY — everything is real. Real methods ARE called.
List<String> spyList = spy(new ArrayList<>());
spyList.add("Alice");                     // ACTUALLY adds "Alice" to the list
assertThat(spyList).hasSize(1);           // contains "Alice" — real behavior
```

#### Key Characteristics

| Property | Value |
|---|---|
| Uses real object? | **Yes** — calls real methods by default |
| Can override methods? | Yes — partial stubbing |
| Verifies calls? | Yes — like a mock |
| Purpose | Observe and partially override a real object |
| Danger | Overuse leads to fragile tests coupled to implementation |

---

### 7.5 Fake

#### Definition

A **Fake** is a **working implementation** that takes a shortcut compared to the real thing. It has real business logic, but it's simpler and not suitable for production.

A fake answers the question: **"I need something that actually works, but simpler/faster than the real dependency."**

#### When to Use

When stubs are too dumb (you need real behavior) but the real dependency is too expensive or slow. Common fakes:
- In-memory database instead of a real PostgreSQL
- In-memory message queue instead of Kafka
- HashMap-based repository instead of JPA
- Local file system instead of S3

#### Example

```java
// Real scenario: Your tests need a UserRepository that actually stores and retrieves data,
// but you don't want a real database.

// --- FAKE implementation ---
public class FakeUserRepository implements UserRepository {
    private final Map<Long, User> storage = new HashMap<>();
    private long nextId = 1;
    
    @Override
    public User save(User user) {
        user.setId(nextId++);
        storage.put(user.getId(), user);
        return user;
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
            .filter(u -> u.getEmail().equals(email))
            .findFirst();
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }
    
    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }
}

// --- Test using the FAKE ---
@Test
void shouldFindSavedUser() {
    UserRepository fakeRepo = new FakeUserRepository();  // ← FAKE
    UserService service = new UserService(fakeRepo);
    
    service.createUser("Alice", "alice@test.com");
    
    // The fake ACTUALLY stores and retrieves — like a real DB but in memory
    Optional<User> found = service.findByEmail("alice@test.com");
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Alice");
}
```

> **Note:** The H2 in-memory database used in `IntegrationTest_UserRepositoryTest.java` in this project is itself a **Fake** — it's a real working database, but simpler and faster than the production PostgreSQL/MySQL.

#### Key Characteristics

| Property | Value |
|---|---|
| Has real logic? | **Yes** — a working (simplified) implementation |
| Suitable for production? | **No** — takes shortcuts |
| Verifies calls? | No |
| Purpose | Provide a lightweight but functional replacement |
| Complexity | Most complex test double to build and maintain |

---

### Comparison Table: All 5 Test Doubles

| Test Double | Has Logic? | Returns Data? | Verifies Calls? | Example |
|---|---|---|---|---|
| **Dummy** | No | No | No | Unused parameter filler |
| **Stub** | No (hardcoded) | **Yes** (canned) | No | `when(repo.findById(1L)).thenReturn(user)` |
| **Mock** | Optional | Optional | **Yes** | `verify(emailService).sendEmail(...)` |
| **Spy** | **Yes** (real) | **Yes** (real) | **Yes** | `spy(realObject)` — watch + partial override |
| **Fake** | **Yes** (simplified) | **Yes** (computed) | No | `FakeUserRepository` backed by `HashMap` |

### When to Use Which?

```
Do you need the parameter at all?
  └─ No → DUMMY

Do you need to verify the interaction (was method X called)?
  └─ Yes → Do you need real behavior too?
             └─ Yes → SPY
             └─ No  → MOCK

Do you need the dependency to return data?
  └─ Yes → Do you need real working logic (store, retrieve, compute)?
             └─ Yes → FAKE
             └─ No (just hardcoded returns) → STUB
```

### Common Mistakes

#### Mistake 1: Using a Mock When You Need a Stub

```java
// BAD — using verify() when you only care about the return value
when(repo.findById(1L)).thenReturn(user);
String name = service.getUserName(1L);
assertThat(name).isEqualTo("Alice");
verify(repo).findById(1L);   // ← UNNECESSARY — you already proved it works via the assertion

// GOOD — just stub it, no need to verify
when(repo.findById(1L)).thenReturn(user);
String name = service.getUserName(1L);
assertThat(name).isEqualTo("Alice");   // ← this is sufficient
```

#### Mistake 2: Over-mocking (Testing Implementation, Not Behavior)

```java
// BAD — testing HOW the code works, not WHAT it does
verify(repo).findById(1L);
verify(repo, times(1)).findById(any());
verifyNoMoreInteractions(repo);
// This test breaks if you refactor the internals, even if behavior is unchanged

// GOOD — test the outcome
assertThat(service.getUserName(1L)).isEqualTo("Alice");
// This test survives refactoring as long as the behavior is preserved
```

#### Mistake 3: Spying When You Should Mock

```java
// BAD — spy on everything "just in case"
OrderProcessor spy = spy(new OrderProcessor(account));
// If you're overriding most methods, just use a mock

// GOOD — spy only when you need mostly-real behavior with 1-2 overrides
```

---

## 8. Key Definitions

### JUnit 5 Annotations

| Annotation | Meaning |
|---|---|
| `@Test` | Marks a method as a test case. JUnit discovers and runs it. |
| `@DisplayName("...")` | Gives the test a human-readable name shown in test reports. |
| `@Nested` | Groups related tests into an inner class. Improves organization and readability. |
| `@BeforeAll` | Runs once before all tests in the class. Must be `static`. Used for expensive setup (DB connections). |
| `@AfterAll` | Runs once after all tests. Used for cleanup (close connections). |
| `@BeforeEach` | Runs before every test method. Gives each test a fresh state. |
| `@Order(n)` | Controls the execution order when used with `@TestMethodOrder`. |
| `@ParameterizedTest` | Runs the same test logic with multiple sets of input data. |
| `@CsvSource` | Supplies comma-separated arguments to a parameterized test. |
| `@ValueSource` | Supplies a simple array of values to a parameterized test. |
| `@NullAndEmptySource` | Adds `null` and `""` as arguments to a parameterized test. |

### AssertJ Assertions

| Assertion | Meaning |
|---|---|
| `assertThat(x).isEqualTo(y)` | Verifies `x` equals `y`. |
| `assertThat(x).isTrue()` / `isFalse()` | Verifies a boolean value. |
| `assertThat(x).isNull()` | Verifies the value is `null`. |
| `assertThat(optional).isPresent()` | Verifies an `Optional` contains a value. |
| `assertThat(optional).isEmpty()` | Verifies an `Optional` is empty. |
| `assertThat(list).hasSize(n)` | Verifies a collection has exactly `n` elements. |
| `assertThat(list).extracting(...)` | Extracts a field from each element for further assertions. |
| `assertThatThrownBy(() -> ...)` | Captures the exception thrown by the lambda for assertion. |
| `.isInstanceOf(Class.class)` | Verifies the exception type. |
| `.hasMessageContaining("...")` | Verifies the exception message contains the given text. |

---

## 9. The Testing Pyramid

```
         /\
        /  \       System/E2E Tests    — Few, slow, high confidence
       /----\
      /      \     Integration Tests   — Moderate number
     /--------\
    /          \   Unit Tests          — Many, fast, focused
   /____________\
```

| Level | Speed | Count | What It Tests | Example in This Project |
|---|---|---|---|---|
| **Unit** | Milliseconds | Many | Single method, pure logic | `Calculator.add(3, 7)` |
| **Integration** | Seconds | Moderate | Components wired together | `UserRepository.save()` → real H2 DB |
| **System/E2E** | Slowest | Few | Full business workflow | Order: validate → calculate → charge → record |

**Best practice:** Write many unit tests, fewer integration tests, and the fewest system tests. This gives you fast feedback at the bottom and high confidence at the top.

---

## 10. Interview Questions and Answers

### Category 1: Fundamentals

---

**Q1: What is the difference between manual testing and automated testing?**

**A:** Manual testing is performed by a human who interacts with the application, clicks buttons, fills forms, and visually verifies results. Automated testing uses code (test scripts) to execute tests automatically and compare actual results against expected results.

| Aspect | Manual | Automated |
|---|---|---|
| Speed | Slow | Fast (milliseconds to seconds) |
| Repeatability | Error-prone (humans make mistakes) | 100% consistent |
| Cost over time | Increases (must re-test every release) | Decreases (write once, run forever) |
| Best for | Exploratory testing, UX evaluation | Regression, CI/CD, repetitive checks |

---

**Q2: What is the Testing Pyramid? Why is it shaped that way?**

**A:** The Testing Pyramid is a strategy that recommends writing tests in a specific ratio:
- **Many** unit tests (base) — fast, cheap, focused
- **Fewer** integration tests (middle) — moderate speed, test component wiring
- **Fewest** system/E2E tests (top) — slow, expensive, but highest confidence

It is shaped as a pyramid because the cost-to-value ratio increases as you go up. Unit tests are cheap to write and fast to run — you can have thousands. E2E tests are expensive and slow — you only write them for critical workflows. If you invert the pyramid (many E2E, few unit tests), your test suite becomes slow, fragile, and expensive to maintain.

---

**Q3: What is the difference between Verification and Validation?**

**A:**
- **Verification:** "Are we building the product **right**?" — Checks the code against the specification/design. (Did we implement the spec correctly?)
- **Validation:** "Are we building the **right** product?" — Checks the product against the user's actual needs. (Does the product solve the user's problem?)

Example: If the spec says "password must be 8+ characters" and your code enforces it — that's verification. If users actually needed 12+ characters for security compliance — the code is *verified* but not *validated*.

---

**Q4: What does SDLC stand for, and where does testing fit in?**

**A:** SDLC = Software Development Life Cycle. Testing fits into multiple phases:
1. **Requirements** → Review requirements for testability (validation)
2. **Design** → Review design for testability, write test plans
3. **Implementation** → Write unit tests (often alongside code via TDD)
4. **Testing** → Integration tests, system tests, UAT
5. **Deployment** → Smoke tests, sanity tests
6. **Maintenance** → Regression tests protect against breaking changes

---

**Q5: What is the difference between a test case, a test suite, and a test plan?**

**A:**
- **Test Case:** A single test with a specific input, action, and expected result. Example: "Given a user with $100, when they withdraw $30, then balance is $70."
- **Test Suite:** A collection of related test cases grouped together. Example: all BankAccount tests.
- **Test Plan:** A document describing the overall testing strategy, scope, resources, schedule, and criteria for a project or release.

---

### Category 2: Test Types

---

**Q6: What is the difference between Unit Testing and Integration Testing?**

**A:**

| Aspect | Unit Test | Integration Test |
|---|---|---|
| Scope | Single method/class | Multiple components together |
| Dependencies | None (mocked/stubbed) | Real (DB, API, file system) |
| Speed | Milliseconds | Seconds to minutes |
| What it catches | Logic errors in isolated code | Wiring errors, SQL bugs, config issues |
| Example | `Calculator.add(3,7) == 10` | `UserRepository.save()` → real DB → read back |

Key insight for interviews: **Unit tests can all pass while integration tests fail.** Example: your mock says `findByEmail()` returns a user, but the real JPQL query has a typo — only an integration test with a real DB catches that.

---

**Q7: What is the difference between Black Box and White Box testing?**

**A:**

| Aspect | Black Box | White Box |
|---|---|---|
| Knowledge | Only the specification | Full access to source code |
| Focus | What the system DOES | How the system DOES it |
| Derived from | Requirements/spec | Code structure (branches, paths) |
| Techniques | Equivalence partitioning, boundary analysis | Statement/branch/path coverage |
| Who writes them | QA, testers, users | Developers |
| Survives refactoring? | Yes (tests behavior) | Often breaks (tests implementation) |

---

**Q8: What is Regression Testing? When do you write regression tests?**

**A:** Regression testing ensures that previously fixed bugs do not reappear after code changes. You write a regression test **every time you fix a bug**:
1. Write a test that reproduces the bug (test fails — confirms the bug).
2. Fix the bug (test passes).
3. Keep the test forever (prevents the bug from coming back).

Regression tests are especially valuable during refactoring, because they give you confidence that your changes haven't broken old fixes.

---

**Q9: What is Smoke Testing vs Sanity Testing?**

**A:**
- **Smoke Testing:** A broad, shallow check that the most critical functions work after a new build. "Does the application start? Can users log in? Does the main page load?" If smoke tests fail, the build is rejected immediately — no point testing further.
- **Sanity Testing:** A narrow, focused check on a specific area after a minor change. "We changed the email template — does email sending still work?" It's a targeted spot-check, not a full test.

| Aspect | Smoke | Sanity |
|---|---|---|
| Scope | Broad (whole system) | Narrow (specific area) |
| Depth | Shallow | Moderate |
| When | After every new build | After a specific bug fix or change |
| Purpose | "Is the build stable enough to test?" | "Did this specific change break anything?" |

---

**Q10: What is Acceptance Testing (UAT)?**

**A:** User Acceptance Testing is performed by the **end users** or **business stakeholders** (not developers or QA) to verify the software meets their real-world needs. It is the final phase before the software goes to production. UAT answers: "Does this solve the business problem we hired it to solve?"

---

**Q11: What is Performance Testing? Name its subtypes.**

**A:** Performance testing evaluates the system's speed, scalability, and stability under load.

| Subtype | What it tests | Example |
|---|---|---|
| **Load Testing** | System behavior under expected load | 1,000 concurrent users |
| **Stress Testing** | System behavior beyond expected load (breaking point) | 100,000 concurrent users |
| **Spike Testing** | System response to sudden load spikes | 0 → 50,000 users in 10 seconds |
| **Endurance (Soak) Testing** | System stability over long periods | 1,000 users for 72 hours (memory leaks?) |
| **Scalability Testing** | How well the system scales with resources | Does doubling servers double throughput? |

---

**Q12: What is the difference between Functional and Non-Functional testing?**

**A:**
- **Functional Testing:** Does the system do what it's supposed to? Tests features against requirements. Examples: unit tests, integration tests, system tests, acceptance tests.
- **Non-Functional Testing:** Does the system do it well enough? Tests quality attributes. Examples: performance testing, security testing, usability testing, reliability testing.

---

### Category 3: Test Doubles (Mock, Stub, Spy)

---

**Q13: What is a Test Double?**

**A:** A test double is any object that replaces a real dependency during testing. The term comes from "stunt double." There are 5 types:
1. **Dummy** — fills a parameter slot, never used.
2. **Stub** — returns hardcoded data, doesn't verify calls.
3. **Mock** — verifies that methods were called with the right arguments.
4. **Spy** — wraps a real object, tracks calls, allows partial overriding.
5. **Fake** — a working but simplified implementation (e.g., in-memory DB).

---

**Q14: What is the difference between a Mock and a Stub?**

**A:** This is one of the most commonly asked interview questions.

| Aspect | Stub | Mock |
|---|---|---|
| Purpose | Provide **data** to the system under test | Verify **interactions** with the system under test |
| Direction | Controls what goes **IN** | Checks what goes **OUT** |
| Verification | No — you assert on the result | Yes — you `verify()` the mock was called |
| Example | `when(repo.findById(1)).thenReturn(user)` | `verify(emailService).sendEmail("alice@test.com")` |

**Rule of thumb:**
- Stub = "When asked, answer with this."
- Mock = "I expect to be called like this."

```java
// STUB — you assert the RESULT
when(repo.findById(1L)).thenReturn(user);      // stub provides data
String name = service.getName(1L);
assertThat(name).isEqualTo("Alice");            // assert on output

// MOCK — you verify the INTERACTION
service.register("alice", "alice@test.com");
verify(emailService).sendWelcomeEmail("alice@test.com", "alice");  // verify call happened
```

---

**Q15: What is a Spy? When would you use one instead of a Mock?**

**A:** A Spy wraps a **real object** and delegates to real methods by default. You use a spy when you need mostly real behavior but want to:
- Track/verify that a specific method was called
- Override 1-2 methods while keeping everything else real

Use a **mock** when you want everything fake. Use a **spy** when you want everything real except specific parts.

```java
List<String> spyList = spy(new ArrayList<>());
spyList.add("Alice");                          // REAL add() is called
assertThat(spyList).hasSize(1);                // really contains "Alice"
verify(spyList).add("Alice");                  // AND we can verify the call
```

---

**Q16: What is the danger of over-mocking?**

**A:** Over-mocking leads to:
1. **Fragile tests** — tests break when you refactor internals, even if behavior is unchanged.
2. **False confidence** — all tests pass, but the mock behavior doesn't match real behavior. The classic case: mocked DB returns data, but real JPQL query has a typo.
3. **Testing the implementation, not behavior** — `verify(repo, times(1)).findById(1L)` tests HOW the code works, not WHAT it produces. If you refactor to use a cache, this test breaks even though behavior is correct.

**Best practice:** Assert on outcomes (return values, state changes), not on interactions. Only use `verify()` when the interaction IS the behavior (sending emails, writing audit logs).

---

**Q17: What is Mockito? Name its key methods.**

**A:** Mockito is the most popular Java mocking framework. Key methods:

| Method | Purpose | Type |
|---|---|---|
| `mock(Class.class)` | Create a mock object | Setup |
| `spy(realObject)` | Create a spy wrapping a real object | Setup |
| `when(mock.method()).thenReturn(value)` | Stub a return value | Stubbing |
| `when(mock.method()).thenThrow(exception)` | Stub to throw an exception | Stubbing |
| `doReturn(value).when(spy).method()` | Stub a spy (avoids calling real method) | Stubbing |
| `verify(mock).method(args)` | Verify method was called once | Verification |
| `verify(mock, times(n)).method(args)` | Verify called exactly n times | Verification |
| `verify(mock, never()).method(args)` | Verify never called | Verification |
| `any()`, `anyString()`, `eq(value)` | Argument matchers | Matching |
| `@Mock` | Annotation to auto-create a mock | Annotation |
| `@InjectMocks` | Auto-inject mocks into the class under test | Annotation |
| `@Captor` | Capture arguments passed to mock methods | Annotation |

---

### Category 4: Code Coverage

---

**Q18: What is code coverage? What are its types?**

**A:** Code coverage measures what percentage of your code is executed during tests.

| Coverage Type | What it measures | Example |
|---|---|---|
| **Statement (Line) Coverage** | % of lines executed | All lines in `getGrade()` ran at least once |
| **Branch (Decision) Coverage** | % of `if/else` branches taken | Both `true` and `false` of each `if` |
| **Path Coverage** | % of all possible paths through the code | Every combination of branches |
| **Condition Coverage** | % of boolean sub-expressions evaluated to both T/F | In `if (a && b)`: both `a` and `b` tested as T and F |
| **Function Coverage** | % of functions/methods called | Every public method was invoked |

---

**Q19: Is 100% code coverage a guarantee of no bugs?**

**A:** **No.** 100% code coverage means every line was executed, but it does NOT mean:
- Every possible input was tested (infinite inputs, finite tests)
- Logic errors are caught (the code runs but produces wrong results)
- Edge cases are covered (coverage only measures "did it run?", not "did it produce the right output?")
- Integration issues exist (all lines of YOUR code ran, but the database query might still be wrong)

**Code coverage measures test quantity, not test quality.** A test that calls every line but never asserts anything achieves 100% coverage with zero bug-detection ability.

---

**Q20: What is a good code coverage target?**

**A:** There is no universal number, but common guidelines:
- **80%+ line coverage** is a reasonable target for most projects.
- **Critical business logic** (payments, security, auth) should aim for **90%+** with **branch coverage**.
- **100% coverage** is rarely worth the cost — the last 10-20% is usually defensive code that's hard to reach.
- **Coverage dips** (coverage decreasing over time) are a red flag — it means new code is being added without tests.

More important than the number: **are the right things tested?** 80% coverage of the critical paths is better than 100% coverage achieved by testing getters and setters.

---

### Category 5: TDD and BDD

---

**Q21: What is TDD (Test-Driven Development)?**

**A:** TDD is a development methodology where you write the test **before** writing the code. It follows the Red-Green-Refactor cycle:

```
1. RED    — Write a test that fails (no implementation yet)
2. GREEN  — Write the MINIMUM code to make the test pass
3. REFACTOR — Clean up the code while keeping the test green
4. REPEAT
```

**Benefits:**
- Forces you to think about the API/behavior before implementation
- Produces testable code by design (hard-to-test code = hard-to-design code)
- Results in high test coverage naturally
- Creates a living specification of what the code does

---

**Q22: What is BDD (Behavior-Driven Development)?**

**A:** BDD extends TDD by writing tests in a human-readable format that describes **behavior** from the user's perspective. Tests are written in Given-When-Then format:

```gherkin
Given a customer with $500 in their account
When they place an order for 2 keyboards at $100 each
Then the order total should be $220 (including 10% tax)
And the customer balance should be $280
And the merchant should receive $220
```

In Java, this maps to frameworks like **Cucumber** or **JBehave**. The key difference from TDD:
- **TDD** focuses on technical correctness: "does this method return the right value?"
- **BDD** focuses on business behavior: "does the system do what the user expects?"

---

**Q23: What is the difference between TDD and writing tests after code?**

**A:**

| Aspect | TDD (Test First) | Test After |
|---|---|---|
| Design influence | Tests drive the design → naturally testable | Design already done → may be hard to test |
| Coverage | High (every line was written to pass a test) | Variable (easy to forget edge cases) |
| Confidence | Code works from the start | Code "probably" works, tests confirm |
| Risk | Over-testing (testing trivial things) | Under-testing (missing critical cases) |
| Speed | Slower initially, faster long-term | Faster initially, slower long-term (more bugs) |

---

### Category 6: JUnit 5 Specific

---

**Q24: What is JUnit 5? How is it different from JUnit 4?**

**A:** JUnit 5 is the latest generation of the JUnit testing framework for Java. Key differences:

| Feature | JUnit 4 | JUnit 5 |
|---|---|---|
| Package | `org.junit` | `org.junit.jupiter.api` |
| Test annotation | `@Test` (from `org.junit`) | `@Test` (from `org.junit.jupiter.api`) |
| Lifecycle | `@Before`, `@After` | `@BeforeEach`, `@AfterEach` |
| Class lifecycle | `@BeforeClass`, `@AfterClass` | `@BeforeAll`, `@AfterAll` |
| Nested tests | Not supported | `@Nested` |
| Display names | Not supported | `@DisplayName` |
| Parameterized | Separate runner, clunky | `@ParameterizedTest` with `@CsvSource`, `@ValueSource` |
| Assertions | `assertEquals(expected, actual)` | `Assertions.assertEquals(expected, actual)` |
| Extensions | `@RunWith` + Runners | `@ExtendWith` + Extensions |

---

**Q25: What is `@Nested` in JUnit 5?**

**A:** `@Nested` allows you to create inner test classes to logically group related tests. It improves organization and produces a hierarchical test report.

```java
@DisplayName("BankAccount")
class BankAccountTest {

    @Nested
    @DisplayName("deposit()")
    class DepositTests {
        @Test void shouldDeposit() { ... }
        @Test void shouldRejectNegative() { ... }
    }

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {
        @Test void shouldWithdraw() { ... }
        @Test void shouldRejectOverdraft() { ... }
    }
}
```

Test report output:
```
BankAccount
  ├── deposit()
  │   ├── should deposit ✓
  │   └── should reject negative ✓
  └── withdraw()
      ├── should withdraw ✓
      └── should reject overdraft ✓
```

---

**Q26: What is `@ParameterizedTest`? How does it work?**

**A:** `@ParameterizedTest` runs the same test method multiple times with different arguments. It replaces copy-pasted tests that only differ in input/output.

Sources of arguments:
- `@ValueSource(strings = {"a", "b"})` — simple values
- `@CsvSource({"input,expected", ...})` — comma-separated pairs
- `@MethodSource("dataProvider")` — a method that returns a Stream of Arguments
- `@EnumSource(MyEnum.class)` — all values of an enum
- `@NullAndEmptySource` — `null` and `""`

---

**Q27: What is the difference between `@BeforeAll` and `@BeforeEach`?**

**A:**

| Annotation | Runs | Use case |
|---|---|---|
| `@BeforeAll` | **Once** before all tests in the class | Expensive setup: DB connection, test container startup |
| `@BeforeEach` | Before **every** test method | Resetting state: fresh objects, clean data |

`@BeforeAll` must be `static` (because no instance exists yet). `@BeforeEach` is instance-level.

Choose `@BeforeAll` when setup is expensive and can be shared. Choose `@BeforeEach` when each test needs a clean state (most common).

---

**Q28: What is AssertJ? Why use it over JUnit's built-in assertions?**

**A:** AssertJ is a fluent assertion library that provides more readable and powerful assertions than JUnit's built-in `assertEquals`.

```java
// JUnit 5 built-in — readable but limited
assertEquals(10, result);
assertTrue(list.contains("Alice"));

// AssertJ — fluent, chainable, and descriptive error messages
assertThat(result).isEqualTo(10);
assertThat(list).contains("Alice").hasSize(3).doesNotContain("Bob");
assertThat(user.getName()).startsWith("Ali").endsWith("ce");
```

AssertJ advantages:
- **Fluent API** — reads like English: `assertThat(x).isEqualTo(y)`
- **Better error messages** — tells you what was expected AND what was actual
- **Rich assertions** — for lists, optionals, exceptions, dates, maps, etc.
- **IDE auto-complete** — type `assertThat(x).` and see all available assertions

---

### Category 7: Test Design and Best Practices

---

**Q29: What is the AAA pattern?**

**A:** Arrange-Act-Assert. Every test should have three clearly separated sections:
1. **Arrange** — Set up preconditions and inputs.
2. **Act** — Execute the method/action being tested.
3. **Assert** — Verify the output or side effect.

This is the same as **Given-When-Then** in BDD terminology.

---

**Q30: What is a flaky test?**

**A:** A flaky test is a test that sometimes passes and sometimes fails **with the exact same code**. Common causes:
- **Timing/race conditions** — test depends on thread scheduling
- **Shared state** — tests interfere with each other
- **External dependencies** — network calls, actual time, random values
- **Order dependency** — test passes in isolation but fails in a suite

Flaky tests are dangerous because they erode trust in the test suite. Developers start ignoring test failures ("oh that one's just flaky"), which allows real bugs to slip through.

---

**Q31: What is test isolation? Why is it important?**

**A:** Test isolation means each test is **independent** — it doesn't depend on other tests running before it, and it doesn't affect tests running after it. Every test sets up its own state and cleans up after itself.

Why it matters:
- Tests can run in **any order** and still pass
- Tests can run in **parallel** without conflicts
- A failing test points to **exactly one problem** (not a cascade failure)

`@BeforeEach` is the primary tool for achieving test isolation — it gives each test a fresh, clean starting state.

---

**Q32: What is the FIRST principle of good tests?**

**A:** FIRST is an acronym for 5 qualities of good unit tests:

| Letter | Principle | Meaning |
|---|---|---|
| **F** | Fast | Tests run in milliseconds. Slow tests don't get run. |
| **I** | Isolated | Each test is independent. No shared state. |
| **R** | Repeatable | Same result every time, in any environment. |
| **S** | Self-validating | Pass or fail — no manual checking needed. |
| **T** | Timely | Written close in time to the code they test (ideally before, via TDD). |

---

**Q33: What is the Right-BICEP principle?**

**A:** A guide for what to test:

| Item | What to test | Example |
|---|---|---|
| **Right** | Are the right results returned? | `add(3, 7) == 10` |
| **B** | Boundary conditions | Minimum, maximum, empty, null, zero |
| **I** | Inverse relationships | `decode(encode(x)) == x` |
| **C** | Cross-checking with another method | Compare with a known-correct implementation |
| **E** | Error conditions | Exceptions, timeouts, invalid input |
| **P** | Performance characteristics | Within acceptable time/memory limits |

---

**Q34: Should you test private methods?**

**A:** **No, generally not.** Private methods are implementation details. Test them **indirectly** through the public methods that call them. If a private method is so complex that it needs its own tests, it's a sign that it should be **extracted into its own class** with a public API.

Testing private methods directly (via reflection or making them package-private) couples your tests to implementation details, making them break during refactoring even when behavior is unchanged.

---

**Q35: What is test-driven development's impact on design?**

**A:** TDD forces you to write testable code, which naturally leads to:
- **Small, focused classes** (single responsibility)
- **Dependency injection** (so dependencies can be replaced with test doubles)
- **Interface-based design** (program to interfaces, not implementations)
- **Low coupling** (fewer dependencies = easier to test in isolation)

Code that is hard to test is usually poorly designed. TDD uses this relationship as a design feedback mechanism.

---

### Category 8: Real-World and Scenario-Based

---

**Q36: You have a service that sends emails. How do you test it without sending real emails?**

**A:** Use a **Mock** for the `EmailService` dependency:

```java
EmailService mockEmail = mock(EmailService.class);
RegistrationService service = new RegistrationService(userRepo, mockEmail);

service.register("alice", "alice@test.com");

// Verify the email was "sent" (method was called correctly)
verify(mockEmail).sendWelcomeEmail("alice@test.com", "alice");
```

You verify the **interaction** (the email service was called with the right arguments), not the side effect (an actual email being delivered). For testing that emails are actually deliverable, use integration tests with tools like **GreenMail** (embedded SMTP server) or **MailHog**.

---

**Q37: How do you test a method that depends on the current time?**

**A:** Never call `LocalDateTime.now()` or `System.currentTimeMillis()` directly in business logic. Instead, inject a `Clock` or time provider:

```java
// BAD — untestable
public boolean isExpired(Order order) {
    return LocalDateTime.now().isAfter(order.getExpiry());  // can't control "now"
}

// GOOD — testable via dependency injection
public boolean isExpired(Order order, Clock clock) {
    return LocalDateTime.now(clock).isAfter(order.getExpiry());
}

// In test:
Clock fixedClock = Clock.fixed(Instant.parse("2024-06-15T10:00:00Z"), ZoneOffset.UTC);
assertThat(service.isExpired(order, fixedClock)).isTrue();
```

---

**Q38: How do you test database code?**

**A:** Three approaches, each at a different level:

| Approach | Speed | Fidelity | When to use |
|---|---|---|---|
| **Mock the repository** (unit test) | Fastest | Lowest (doesn't test SQL) | Testing service logic that USES the repo |
| **In-memory DB like H2** (integration test) | Medium | Medium (SQL dialect may differ) | Testing JPQL/HQL queries |
| **Testcontainers** (real DB in Docker) | Slowest | Highest (exact same DB as prod) | Testing native SQL, stored procs, constraints |

This project uses the H2 approach in `IntegrationTest_UserRepositoryTest.java`.

---

**Q39: What is Testcontainers?**

**A:** Testcontainers is a Java library that manages Docker containers during tests. It lets you start a real PostgreSQL, MySQL, Redis, Kafka, etc., run your tests against it, and tear it down automatically. This gives you **100% fidelity** — your tests run against the exact same database engine as production.

```java
@Testcontainers
class UserRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    
    @Test
    void shouldSaveUser() {
        // tests run against a REAL PostgreSQL in Docker
    }
}
```

---

**Q40: A test passes locally but fails in CI. What do you check?**

**A:** Common causes and checks:
1. **Environment differences** — different Java version, OS, timezone, locale
2. **Test order dependency** — tests run in different order in CI; a test depends on another running first
3. **Timing/concurrency** — CI machines are slower; race conditions surface
4. **External dependencies** — test hits a real API that's down or rate-limited in CI
5. **File paths** — hardcoded paths like `/Users/alice/...` that don't exist in CI
6. **Port conflicts** — test starts a server on port 8080, but something else uses it in CI
7. **Database state** — shared test database not cleaned between runs

---

**Q41: How do you decide what to test?**

**A:** Prioritize by risk and value:
1. **Business-critical logic** (payments, auth, data integrity) — high-priority, test thoroughly
2. **Complex logic** (many branches, calculations) — high bug risk, test all paths
3. **Bug-prone areas** (frequently changed code, past bug reports) — regression tests
4. **Public APIs** (contracts other code depends on) — prevent breaking changes
5. **Simple CRUD / getters / setters** — low priority, test indirectly

Don't aim for 100% coverage everywhere. Aim for **high coverage where it matters most**.

---

**Q42: What is the difference between `assertThatThrownBy` and `assertThrows`?**

**A:**

```java
// JUnit 5 built-in
ArithmeticException ex = assertThrows(ArithmeticException.class,
    () -> calculator.divide(10, 0));
assertEquals("Cannot divide by zero", ex.getMessage());

// AssertJ — fluent and chainable
assertThatThrownBy(() -> calculator.divide(10, 0))
    .isInstanceOf(ArithmeticException.class)
    .hasMessageContaining("Cannot divide by zero")
    .hasNoCause();
```

Both work. AssertJ's version is more readable and allows chaining multiple checks without storing the exception in a variable.

---

### Category 9: Advanced Topics

---

**Q43: What is Mutation Testing?**

**A:** Mutation testing evaluates the **quality** of your tests by introducing small bugs ("mutants") into your code and checking if your tests catch them.

Example mutations:
- Change `>` to `>=`
- Change `+` to `-`
- Remove a method call
- Replace `true` with `false`

If a test fails after a mutation → the mutant is "killed" (good — your tests caught it).
If all tests still pass → the mutant "survived" (bad — your tests missed it).

**Mutation score = killed mutants / total mutants.** A high mutation score means your tests are effective at catching bugs. Tools: **PIT (pitest.org)** for Java.

---

**Q44: What is Contract Testing?**

**A:** Contract testing verifies that two services (e.g., a consumer and a provider) agree on the API contract (request/response format, status codes, headers). Instead of running both services together (slow E2E test), each side tests against a shared "contract."

Tools: **Pact**, **Spring Cloud Contract**.

---

**Q45: What is Property-Based Testing?**

**A:** Instead of specifying exact input-output pairs, you specify **properties** (invariants) that should always hold, and the framework generates hundreds of random inputs.

```java
// Traditional: test ONE specific input
assertThat(StringUtils.reverse("hello")).isEqualTo("olleh");

// Property-based: test a PROPERTY over MANY random inputs
@Property
void reversingTwiceReturnsOriginal(@ForAll String s) {
    assertThat(StringUtils.reverse(StringUtils.reverse(s))).isEqualTo(s);
}
```

The framework tries hundreds of random strings and finds the **smallest input** that violates the property. Tools: **jqwik** for JUnit 5.

---

**Q46: What is the Arrange-Act-Assert anti-pattern?**

**A:** Common violations of the AAA pattern:
1. **Multiple Acts** — testing two things in one test. If the first fails, you never know if the second works.
2. **Assert in Arrange** — asserting preconditions (acceptable in some cases, but clutters the test).
3. **No clear separation** — all three phases blended together, making the test hard to read.
4. **Act and Assert mixed** — `assertThat(service.process(input)).isEqualTo(...)` is fine, but complex processing mixed with assertions is not.

---

**Q47: What is the difference between `@Mock` and `@InjectMocks` in Mockito?**

**A:**

| Annotation | Purpose |
|---|---|
| `@Mock` | Creates a mock instance of the annotated type |
| `@InjectMocks` | Creates a real instance of the class and auto-injects `@Mock` fields into it |

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;         // mock

    @Mock
    EmailService emailService;          // mock

    @InjectMocks
    UserService service;                // REAL object, with mocks injected via constructor

    @Test
    void test() {
        when(repository.findById(1L)).thenReturn(Optional.of(user));
        // service.repository is the mock above
    }
}
```

---

**Q48: What is an `ArgumentCaptor` in Mockito?**

**A:** An `ArgumentCaptor` captures the actual arguments passed to a mock method, so you can inspect them later.

```java
@Captor
ArgumentCaptor<Email> emailCaptor;

@Test
void shouldSendFormattedEmail() {
    service.register("alice", "alice@test.com");
    
    verify(emailService).send(emailCaptor.capture());
    
    Email sentEmail = emailCaptor.getValue();
    assertThat(sentEmail.getTo()).isEqualTo("alice@test.com");
    assertThat(sentEmail.getSubject()).contains("Welcome");
    assertThat(sentEmail.getBody()).contains("Alice");
}
```

Use `ArgumentCaptor` when you need to verify **complex objects** passed to mocks, beyond what simple argument matchers (`eq()`, `any()`) can express.

---

### Category 10: Behavioral / Soft Questions

---

**Q49: A developer says "I don't have time to write tests." How do you respond?**

**A:** Tests are an investment, not a tax. Without tests:
- Every code change requires **manual testing** (slower over time)
- **Bugs reach production** more often (expensive to fix)
- **Refactoring is risky** (no safety net → code rots)
- **Onboarding is harder** (new devs have no spec to read)

The real cost is not writing tests — it's the debugging, hotfixes, and regression firefighting that follows. Start small: test the most critical business logic first. Even 30% coverage of the right code is better than 0%.

---

**Q50: How do you handle a test suite that takes 30 minutes to run?**

**A:** Strategies to speed it up:
1. **Parallelize** — run tests in parallel (JUnit 5 supports this natively)
2. **Optimize test doubles** — replace slow integration tests with faster fakes where appropriate
3. **Test categorization** — tag tests as `@Tag("slow")` and run them separately (e.g., only in CI nightly builds)
4. **Reduce redundancy** — remove duplicate tests that check the same behavior
5. **Use in-memory DBs** — H2 instead of real PostgreSQL for most integration tests
6. **Profile** — find the 5 slowest tests and optimize them specifically
7. **Test pyramid** — if you have too many slow E2E tests, push some assertions down to unit tests

---

**Q51: When is it okay to NOT write tests?**

**A:** Testing is about managing risk. It's acceptable to skip tests for:
- **Prototypes / throwaway code** — code that won't live past a demo
- **Generated code** — DTOs, boilerplate that a tool produces
- **Configuration** — simple wiring that integration/smoke tests cover
- **Trivial methods** — simple getters/setters with no logic

But **never skip tests for:** payment logic, authentication, data integrity, public APIs, or anything where a bug costs real money or trust.

---

**Q52: What makes a good test name?**

**A:** A good test name describes the **scenario** and **expected outcome** so you can understand the failure without reading the test body.

```java
// BAD — what does "test1" tell you when it fails?
@Test void test1() { ... }

// BAD — describes the method, not the behavior
@Test void testDeposit() { ... }

// GOOD — describes scenario and expectation
@Test void shouldRejectWithdrawalExceedingBalance() { ... }

// GOOD — with @DisplayName for readability in reports
@Test
@DisplayName("should throw InsufficientFundsException when withdrawal exceeds balance")
void withdrawExceedingBalance() { ... }
```

Pattern: `should[ExpectedBehavior]When[Scenario]` or `[method]_[scenario]_[expectedResult]`

---

**Q53: What is the difference between assertEquals and assertThat?**

**A:**

```java
// JUnit assertEquals — parameter order matters (expected, actual)
assertEquals(10, result);        // which is expected? which is actual?
assertEquals("wrong order is confusing", result, 10);  // message first in JUnit 4!

// AssertJ assertThat — reads naturally, no parameter confusion
assertThat(result).isEqualTo(10);    // "assert that result is equal to 10"
```

`assertThat` from AssertJ is preferred because:
- No parameter order confusion
- Fluent chaining: `assertThat(x).isNotNull().isGreaterThan(5).isLessThan(100)`
- Better error messages: shows both expected and actual values clearly
- Rich assertion library for collections, exceptions, optionals, etc.

---

**Q54: How do you test asynchronous code?**

**A:** Several approaches:
1. **Awaitility** — a DSL for asserting async conditions with timeouts:
   ```java
   await().atMost(5, SECONDS).until(() -> service.isProcessed(orderId));
   ```
2. **CompletableFuture** — call `.join()` or `.get()` to block until the result is available, then assert.
3. **CountDownLatch** — signal completion from the async thread, wait in the test thread.
4. **Mockito timeout verification:**
   ```java
   verify(mock, timeout(1000)).method(args);  // wait up to 1 second for the call
   ```

---

**Q55: What is a test fixture?**

**A:** A test fixture is the **set of preconditions** needed to run a test — the known state that tests start from. In JUnit, fixtures are set up using `@BeforeAll` / `@BeforeEach` and cleaned up with `@AfterAll` / `@AfterEach`.

```java
class BankAccountTest {
    private BankAccount account;  // ← fixture
    
    @BeforeEach
    void setUp() {
        account = new BankAccount("Alice", 100.0);  // ← fixture setup
    }
    
    @Test
    void shouldDeposit() {
        account.deposit(50);
        assertThat(account.getBalance()).isEqualTo(150.0);
    }
}
```

The `account` with an initial balance of $100 is the fixture — the known starting state for every test.
