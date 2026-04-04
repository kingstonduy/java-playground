package me.personal.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * =====================================================================
 *  REGRESSION TEST
 * =====================================================================
 *
 * WHAT:
 *   Tests that PREVIOUSLY FIXED BUGS don't come back.
 *   Each test documents: what was the bug, when it was fixed, what input triggered it.
 *
 * WHY:
 *   - Bugs love to come back (especially during refactoring)
 *   - Documents the bug history of the codebase
 *   - Gives developers confidence to refactor without breaking old fixes
 *   - "If this test fails, we've re-introduced bug #XXX"
 *
 * WHEN TO WRITE:
 *   - Every time you fix a bug, write a test for it FIRST (before fixing)
 *   - The test should FAIL with the bug present and PASS after the fix
 *   - Keep forever — never delete regression tests
 *
 * NAMING CONVENTION:
 *   Test name should reference the bug / ticket number if available.
 *   Comment should explain what the bug was and how it was triggered.
 *
 * WORKFLOW:
 *   1. Bug reported: "reverse(null) throws NullPointerException"
 *   2. Write test: reverse(null) should return null ← test FAILS (bug confirmed)
 *   3. Fix the code: add null check
 *   4. Run test ← test PASSES (bug fixed)
 *   5. Keep the test forever ← prevents regression
 */
@DisplayName("Regression Test — StringUtils (protecting fixed bugs)")
class RegressionTest_StringUtilsTest {

    // =================================================================
    //  Bug #1: reverse(null) threw NullPointerException
    //  Fixed: 2024-01-15
    //  Root cause: no null check before calling StringBuilder
    // =================================================================

    @Nested
    @DisplayName("reverse() — Bug #1: NullPointerException on null input")
    class ReverseBugFixes {

        @Test
        @DisplayName("BUG #1: reverse(null) should return null, not throw NPE")
        void reverseNull_shouldReturnNull() {
            // Before fix: new StringBuilder(null).reverse() → NullPointerException
            // After fix: returns null
            assertThat(StringUtils.reverse(null)).isNull();
        }

        @Test
        @DisplayName("reverse still works for normal inputs (sanity check)")
        void reverseNormalInputs() {
            assertThat(StringUtils.reverse("hello")).isEqualTo("olleh");
            assertThat(StringUtils.reverse("")).isEqualTo("");
            assertThat(StringUtils.reverse("a")).isEqualTo("a");
        }
    }

    // =================================================================
    //  Bug #2: isPalindrome("Racecar") returned false (case-sensitive)
    //  Fixed: 2024-02-20
    //  Root cause: was comparing without toLowerCase()
    // =================================================================

    @Nested
    @DisplayName("isPalindrome() — Bug #2: case-sensitive comparison")
    class PalindromeBugFixes {

        @Test
        @DisplayName("BUG #2: isPalindrome should be case-insensitive")
        void palindromeCaseInsensitive() {
            // Before fix: "Racecar".equals("racecaR") → false
            // After fix: compared in lowercase → true
            assertThat(StringUtils.isPalindrome("Racecar")).isTrue();
            assertThat(StringUtils.isPalindrome("RaCeCaR")).isTrue();
        }

        @Test
        @DisplayName("BUG #2 extension: should handle spaces and punctuation")
        void palindromeWithSpecialChars() {
            assertThat(StringUtils.isPalindrome("A man a plan a canal Panama")).isTrue();
            assertThat(StringUtils.isPalindrome("Was it a car or a cat I saw?")).isTrue();
        }

        @Test
        @DisplayName("isPalindrome still rejects non-palindromes")
        void notPalindrome() {
            assertThat(StringUtils.isPalindrome("hello")).isFalse();
            assertThat(StringUtils.isPalindrome("abc")).isFalse();
        }

        @Test
        @DisplayName("isPalindrome handles null")
        void palindromeNull() {
            assertThat(StringUtils.isPalindrome(null)).isFalse();
        }
    }

    // =================================================================
    //  Bug #3: truncate("hi", 10) threw StringIndexOutOfBoundsException
    //  Fixed: 2024-03-10
    //  Root cause: substring(0, maxLength - 3) when input was shorter than maxLength
    // =================================================================

    @Nested
    @DisplayName("truncate() — Bug #3: StringIndexOutOfBoundsException")
    class TruncateBugFixes {

        @Test
        @DisplayName("BUG #3: truncate should return input as-is when shorter than maxLength")
        void truncateShorterThanMax() {
            // Before fix: "hi".substring(0, 10 - 3) → StringIndexOutOfBoundsException
            // After fix: returns "hi" as-is
            assertThat(StringUtils.truncate("hi", 10)).isEqualTo("hi");
        }

        @Test
        @DisplayName("BUG #3: truncate when input length equals maxLength")
        void truncateExactLength() {
            assertThat(StringUtils.truncate("hello", 5)).isEqualTo("hello");
        }

        @Test
        @DisplayName("truncate still works for long inputs")
        void truncateLongInput() {
            assertThat(StringUtils.truncate("Hello World!", 8)).isEqualTo("Hello...");
            assertThat(StringUtils.truncate("abcdefghij", 7)).isEqualTo("abcd...");
        }

        @Test
        @DisplayName("truncate handles null")
        void truncateNull() {
            assertThat(StringUtils.truncate(null, 10)).isNull();
        }

        @Test
        @DisplayName("truncate handles very small maxLength")
        void truncateSmallMax() {
            assertThat(StringUtils.truncate("hello", 3)).isEqualTo("hel");
            assertThat(StringUtils.truncate("hello", 1)).isEqualTo("h");
        }
    }
}
