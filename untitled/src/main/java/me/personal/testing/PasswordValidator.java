package me.personal.testing;

/**
 * Validates password strength — used in Black Box Test demo.
 *
 * Rules:
 *   - At least 8 characters
 *   - At least 1 uppercase letter
 *   - At least 1 lowercase letter
 *   - At least 1 digit
 *   - At least 1 special character (!@#$%^&*)
 *
 * Black box testers don't know these rules — they test based on the specification.
 */
public class PasswordValidator {

    public enum Strength {
        WEAK,       // fails 2+ rules
        FAIR,       // fails 1 rule
        STRONG      // passes all rules
    }

    public Strength validate(String password) {
        if (password == null || password.isEmpty()) {
            return Strength.WEAK;
        }

        int failedRules = 0;

        if (password.length() < 8) failedRules++;
        if (!password.matches(".*[A-Z].*")) failedRules++;
        if (!password.matches(".*[a-z].*")) failedRules++;
        if (!password.matches(".*\\d.*")) failedRules++;
        if (!password.matches(".*[!@#$%^&*].*")) failedRules++;

        if (failedRules >= 2) return Strength.WEAK;
        if (failedRules == 1) return Strength.FAIR;
        return Strength.STRONG;
    }

    public boolean isAcceptable(String password) {
        Strength strength = validate(password);
        return strength == Strength.STRONG || strength == Strength.FAIR;
    }
}
