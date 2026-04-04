package me.personal.testing;

/**
 * String utility methods — used in Regression Test demo.
 * Contains a "fixed bug" that regression tests protect against.
 */
public class StringUtils {

    /**
     * Reverses a string.
     *
     * BUG HISTORY:
     *   v1 (buggy):  didn't handle null → NullPointerException
     *   v2 (fixed):  returns null for null input
     *   Regression test ensures the fix stays.
     */
    public static String reverse(String input) {
        if (input == null) return null;  // fix for the null bug
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * Checks if a string is a palindrome (reads same forwards and backwards).
     *
     * BUG HISTORY:
     *   v1 (buggy):  was case-sensitive → "Racecar" returned false
     *   v2 (fixed):  case-insensitive comparison
     *   Regression test ensures the fix stays.
     */
    public static boolean isPalindrome(String input) {
        if (input == null) return false;
        String cleaned = input.toLowerCase().replaceAll("[^a-z0-9]", "");
        return cleaned.equals(new StringBuilder(cleaned).reverse().toString());
    }

    /**
     * Truncates a string to maxLength, adding "..." if truncated.
     *
     * BUG HISTORY:
     *   v1 (buggy):  threw StringIndexOutOfBoundsException when input.length() < maxLength
     *   v2 (fixed):  returns input as-is if shorter than maxLength
     *   Regression test ensures the fix stays.
     */
    public static String truncate(String input, int maxLength) {
        if (input == null) return null;
        if (maxLength < 0) throw new IllegalArgumentException("maxLength must be non-negative");
        if (input.length() <= maxLength) return input;
        if (maxLength <= 3) return input.substring(0, maxLength);
        return input.substring(0, maxLength - 3) + "...";
    }
}
