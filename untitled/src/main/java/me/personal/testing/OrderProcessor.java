package me.personal.testing;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes orders end-to-end — used in System Test demo.
 * Demonstrates a multi-step business workflow.
 */
public class OrderProcessor {

    private final List<Order> orders = new ArrayList<>();
    private final BankAccount merchantAccount;

    public OrderProcessor(BankAccount merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    /**
     * Full order workflow:
     *   1. Validate order
     *   2. Calculate total (with tax)
     *   3. Charge customer
     *   4. Record order
     */
    public Order placeOrder(BankAccount customerAccount, String item, int quantity, double unitPrice) {
        // Step 1: Validate
        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("Item cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        // Step 2: Calculate total
        double subtotal = unitPrice * quantity;
        double tax = subtotal * 0.10;  // 10% tax
        double total = subtotal + tax;

        // Step 3: Charge customer → pay merchant
        BankAccount.transfer(customerAccount, merchantAccount, total);

        // Step 4: Record order
        Order order = new Order(item, quantity, unitPrice, tax, total, Order.Status.COMPLETED);
        orders.add(order);

        return order;
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    public record Order(
            String item,
            int quantity,
            double unitPrice,
            double tax,
            double total,
            Status status
    ) {
        public enum Status { PENDING, COMPLETED, FAILED }
    }
}
