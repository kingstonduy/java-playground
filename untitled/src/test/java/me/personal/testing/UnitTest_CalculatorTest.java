package me.personal.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * =====================================================================
 *  UNIT TEST
 * =====================================================================
 *
 * WHAT:
 *   Tests a SINGLE class/method in ISOLATION.
 *   No database, no network, no file system, no external dependencies.
 *   Just pure logic.
 *
 * WHY:
 *   - Fastest tests (milliseconds)
 *   - Easy to write and debug
 *   - Pinpoints exactly what broke
 *   - Run thousands of them in seconds
 *
 * WHEN TO WRITE:
 *   - For every public method with logic (calculations, validations, transformations)
 *   - NOT for simple getters/setters or pass-through methods
 *
 * CHARACTERISTICS:
 *   - No external dependencies (DB, network, files)
 *   - Tests one "unit" (method or small group of methods)
 *   - Runs in milliseconds
 *   - Deterministic (same input �� same output, every time)
 *
 * STRUCTURE:
 *   Every test follows the AAA pattern:
 *     Arrange — set up the data
 *     Act     — call the method
 *     Assert  — verify the result
 */
@DisplayName("Unit Test — Calculator")
class UnitTest_CalculatorTest {

    private final Calculator calculator = new Calculator();

    // =================================================================
    //  Basic arithmetic — straightforward unit tests
    // =================================================================

    @Nested
    @DisplayName("add()")
    class AddTests {

        @Test
        @DisplayName("should add two positive numbers")
        void addPositiveNumbers() {
            // Arrange — nothing to set up

            // Act
            int result = calculator.add(3, 7);

            // Assert
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("should handle negative numbers")
        void addNegativeNumbers() {
            assertThat(calculator.add(-3, -7)).isEqualTo(-10);
            assertThat(calculator.add(-3, 7)).isEqualTo(4);
        }

        @Test
        @DisplayName("should handle zero")
        void addZero() {
            assertThat(calculator.add(5, 0)).isEqualTo(5);
            assertThat(calculator.add(0, 0)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("divide()")
    class DivideTests {

        @Test
        @DisplayName("should divide two numbers")
        void divideNumbers() {
            assertThat(calculator.divide(10, 2)).isEqualTo(5);
        }

        @Test
        @DisplayName("should do integer division (truncate)")
        void integerDivision() {
            assertThat(calculator.divide(7, 2)).isEqualTo(3);  // not 3.5
        }

        @Test
        @DisplayName("should throw ArithmeticException when dividing by zero")
        void divideByZero() {
            // assertThatThrownBy — a clean way to test exceptions
            assertThatThrownBy(() -> calculator.divide(10, 0))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessageContaining("Cannot divide by zero");
        }
    }

    // =================================================================
    //  BankAccount — unit testing business rules
    // =================================================================

    @Nested
    @DisplayName("BankAccount unit tests")
    class BankAccountTests {

        @Test
        @DisplayName("should deposit money")
        void deposit() {
            BankAccount account = new BankAccount("Alice", 100.0);

            account.deposit(50.0);

            assertThat(account.getBalance()).isEqualTo(150.0);
        }

        @Test
        @DisplayName("should withdraw money")
        void withdraw() {
            BankAccount account = new BankAccount("Alice", 100.0);

            account.withdraw(30.0);

            assertThat(account.getBalance()).isEqualTo(70.0);
        }

        @Test
        @DisplayName("should reject withdrawal exceeding balance")
        void withdrawExceedingBalance() {
            BankAccount account = new BankAccount("Alice", 100.0);

            assertThatThrownBy(() -> account.withdraw(150.0))
                    .isInstanceOf(InsufficientFundsException.class);
        }

        @Test
        @DisplayName("should reject negative deposit")
        void negativeDeposit() {
            BankAccount account = new BankAccount("Alice", 100.0);

            assertThatThrownBy(() -> account.deposit(-50.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should transfer between accounts")
        void transfer() {
            BankAccount alice = new BankAccount("Alice", 100.0);
            BankAccount bob = new BankAccount("Bob", 50.0);

            BankAccount.transfer(alice, bob, 30.0);

            assertThat(alice.getBalance()).isEqualTo(70.0);
            assertThat(bob.getBalance()).isEqualTo(80.0);
        }
    }
}
