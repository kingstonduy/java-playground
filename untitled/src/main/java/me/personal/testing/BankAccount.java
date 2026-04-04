package me.personal.testing;

/**
 * Bank account with business rules — used in multiple test demos.
 *
 * Rules:
 *   - Cannot withdraw more than balance
 *   - Cannot withdraw negative amounts
 *   - Transfer between accounts must be atomic
 *   - Overdraft fee of $35 if balance goes below $0 (for accounts with overdraft enabled)
 */
public class BankAccount {

    private final String owner;
    private double balance;
    private final boolean overdraftEnabled;

    public BankAccount(String owner, double initialBalance, boolean overdraftEnabled) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.owner = owner;
        this.balance = initialBalance;
        this.overdraftEnabled = overdraftEnabled;
    }

    public BankAccount(String owner, double initialBalance) {
        this(owner, initialBalance, false);
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (!overdraftEnabled && amount > balance) {
            throw new InsufficientFundsException(
                    "Cannot withdraw " + amount + " from balance " + balance);
        }
        balance -= amount;
        if (overdraftEnabled && balance < 0) {
            balance -= 35.0;  // overdraft fee
        }
    }

    public static void transfer(BankAccount from, BankAccount to, double amount) {
        from.withdraw(amount);
        to.deposit(amount);
    }

    public double getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isOverdraftEnabled() {
        return overdraftEnabled;
    }
}
