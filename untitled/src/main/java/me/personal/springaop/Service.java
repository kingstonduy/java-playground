package me.personal.springaop;

import org.springframework.stereotype.Component;

/**
 * Spring bean — AOP only works on beans managed by the Spring container.
 * "new Service()" will NOT trigger any aspect. You must get it from Spring context.
 */
@Component
public class Service {

    @LogRequestResponse
    public String greet(String name) {
        return "Hello, " + name;
    }

    @Auditable(action = "FIND_USER")
    public String findUser(String name) {
        return "Found user: " + name;
    }

    public int add(int a, int b) {
        return a + b;
    }

    public double calculateTotal(double price, int quantity) {
        return price * quantity;
    }

    public void riskyMethod() {
        throw new IllegalStateException("Something went wrong!");
    }

    public void processPayment(String orderId) {
        throw new RuntimeException("Payment gateway timeout for order: " + orderId);
    }
}
