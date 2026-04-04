package me.personal.testing;

/**
 * Simple calculator — used in Unit Test and White Box Test demos.
 */
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return a / b;
    }

    /**
     * Returns the grade letter based on score.
     * Used in White Box testing to demonstrate branch coverage.
     */
    public String getGrade(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be 0-100");
        }
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    /**
     * Calculates discount based on multiple conditions.
     * Used in White Box testing to demonstrate path coverage.
     */
    public double calculateDiscount(double price, boolean isMember, int quantity) {
        double discount = 0;

        if (isMember) {
            discount += 0.10;  // 10% member discount
        }

        if (quantity >= 10) {
            discount += 0.15;  // 15% bulk discount
        } else if (quantity >= 5) {
            discount += 0.05;  // 5% small bulk discount
        }

        if (price > 1000 && isMember) {
            discount += 0.05;  // extra 5% for high-value member orders
        }

        return price * (1 - discount);
    }
}
