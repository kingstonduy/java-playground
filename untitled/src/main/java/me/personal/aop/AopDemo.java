package me.personal.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run this to see all AOP advice types in action.
 */
public class AopDemo {

    private static final Logger log = LoggerFactory.getLogger(AopDemo.class);

    public static void main(String[] args) {
        Service service = new Service();

        log.info("========== @Around + @annotation(@LogRequestResponse) ==========");
        service.greet("John");

        log.info("\n========== @Before + execution(int, int) ==========");
        int sum = service.add(3, 7);
        log.info("Sum = {}", sum);

        log.info("\n========== @AfterReturning + execution(String) ==========");
        service.findUser("Alice");

        log.info("\n========== @Around + args(double, int) ==========");
        service.calculateTotal(999.99, 3);

        log.info("\n========== @AfterThrowing ==========");
        try {
            service.riskyMethod();
        } catch (Exception e) {
            log.info("Caught in main: {}", e.getMessage());
        }

        log.info("\n========== @After (runs on success AND failure) ==========");
        try {
            service.processPayment("ORD-123");
        } catch (Exception e) {
            log.info("Caught in main: {}", e.getMessage());
        }
    }
}
