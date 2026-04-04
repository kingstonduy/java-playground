package me.personal.testing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * =====================================================================
 *  SYSTEM TEST (End-to-End Test)
 * =====================================================================
 *
 * WHAT:
 *   Tests the ENTIRE SYSTEM / WORKFLOW from start to finish.
 *   All real components — no fakes, no shortcuts.
 *   Simulates what a real user would do.
 *
 * WHY:
 *   - Unit tests pass, integration tests pass, but the WORKFLOW can still fail
 *   - Catches issues in the interaction between ALL components
 *   - Verifies business requirements end-to-end
 *   - "Does the whole thing actually work?"
 *
 * WHEN TO WRITE:
 *   - For critical business workflows (checkout, signup, payment)
 *   - For acceptance criteria / user stories
 *   - After all unit and integration tests pass
 *
 * CHARACTERISTICS:
 *   - Slowest tests (uses all real components)
 *   - Tests the full workflow, not individual pieces
 *   - Closest to real user behavior
 *   - Fewest in number (testing pyramid: many unit, fewer integration, fewest system)
 *
 * TESTING PYRAMID:
 *
 *         /\
 *        /  \       ← System/E2E Tests (few, slow, high confidence)
 *       /----\
 *      /      \     ← Integration Tests (moderate number)
 *     /--------\
 *    /          \   ← Unit Tests (many, fast, focused)
 *   /____________\
 *
 * HERE:
 *   We test the full order workflow:
 *     Customer has money → places order → money transfers → order recorded
 *   All real objects: BankAccount, OrderProcessor, Order.
 */
@DisplayName("System Test — Full Order Workflow")
class SystemTest_OrderProcessorTest {

    private BankAccount merchantAccount;
    private OrderProcessor processor;

    @BeforeEach
    void setup() {
        merchantAccount = new BankAccount("Shop", 0);
        processor = new OrderProcessor(merchantAccount);
    }

    // =================================================================
    //  Happy path — the complete workflow works
    // =================================================================

    @Test
    @DisplayName("complete order workflow: validate → calculate → charge → record")
    void fullOrderWorkflow() {
        // Arrange — customer has $500
        BankAccount customer = new BankAccount("Alice", 500.0);

        // Act — place an order: 2 items at $100 each
        OrderProcessor.Order order = processor.placeOrder(customer, "Keyboard", 2, 100.0);

        // Assert — verify EVERY step of the workflow:

        // Step 1: Order is recorded correctly
        assertThat(order.item()).isEqualTo("Keyboard");
        assertThat(order.quantity()).isEqualTo(2);
        assertThat(order.unitPrice()).isEqualTo(100.0);
        assertThat(order.status()).isEqualTo(OrderProcessor.Order.Status.COMPLETED);

        // Step 2: Tax calculated correctly (10%)
        assertThat(order.tax()).isEqualTo(20.0);         // 200 * 0.10
        assertThat(order.total()).isEqualTo(220.0);       // 200 + 20

        // Step 3: Money transferred correctly
        assertThat(customer.getBalance()).isEqualTo(280.0);     // 500 - 220
        assertThat(merchantAccount.getBalance()).isEqualTo(220.0); // 0 + 220

        // Step 4: Order recorded in processor
        assertThat(processor.getOrders()).hasSize(1);
        assertThat(processor.getOrders().get(0)).isEqualTo(order);
    }

    // =================================================================
    //  Multiple orders — state accumulates correctly
    // =================================================================

    @Test
    @DisplayName("multiple orders accumulate correctly")
    void multipleOrders() {
        BankAccount customer = new BankAccount("Bob", 10000.0);

        processor.placeOrder(customer, "Mouse", 1, 50.0);     // total: 55
        processor.placeOrder(customer, "Monitor", 1, 500.0);   // total: 550
        processor.placeOrder(customer, "Cable", 5, 10.0);      // total: 55

        // All 3 orders recorded
        assertThat(processor.getOrders()).hasSize(3);

        // Merchant received all payments: 55 + 550 + 55 = 660
        assertThat(merchantAccount.getBalance()).isEqualTo(660.0);

        // Customer spent 660
        assertThat(customer.getBalance()).isEqualTo(10000.0 - 660.0);
    }

    // =================================================================
    //  Failure scenarios — workflow fails gracefully
    // =================================================================

    @Nested
    @DisplayName("Failure scenarios")
    class FailureScenarios {

        @Test
        @DisplayName("order fails when customer has insufficient funds")
        void insufficientFunds() {
            BankAccount poorCustomer = new BankAccount("Charlie", 10.0);

            // Order total = 110 (100 + 10% tax), but customer only has $10
            assertThatThrownBy(() ->
                    processor.placeOrder(poorCustomer, "Laptop", 1, 100.0))
                    .isInstanceOf(InsufficientFundsException.class);

            // IMPORTANT: verify the system is in a consistent state after failure
            assertThat(poorCustomer.getBalance()).isEqualTo(10.0);  // unchanged
            assertThat(merchantAccount.getBalance()).isEqualTo(0);   // unchanged
            assertThat(processor.getOrders()).isEmpty();              // no order recorded
        }

        @Test
        @DisplayName("order fails with invalid item")
        void invalidItem() {
            BankAccount customer = new BankAccount("Dave", 1000.0);

            assertThatThrownBy(() ->
                    processor.placeOrder(customer, "", 1, 100.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("empty");

            // System unchanged
            assertThat(customer.getBalance()).isEqualTo(1000.0);
            assertThat(processor.getOrders()).isEmpty();
        }

        @Test
        @DisplayName("order fails with invalid quantity")
        void invalidQuantity() {
            BankAccount customer = new BankAccount("Eve", 1000.0);

            assertThatThrownBy(() ->
                    processor.placeOrder(customer, "Item", 0, 100.0))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() ->
                    processor.placeOrder(customer, "Item", -1, 100.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
