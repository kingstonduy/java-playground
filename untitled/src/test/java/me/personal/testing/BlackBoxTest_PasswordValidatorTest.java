package me.personal.testing;

import me.personal.testing.PasswordValidator.Strength;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * =====================================================================
 *  BLACK BOX TEST
 * =====================================================================
 *
 * WHAT:
 *   Tests the system based ONLY on its SPECIFICATION (requirements).
 *   You do NOT look at the source code.
 *   You only know: "given this input, I expect this output."
 *
 * WHY:
 *   - Tests from the USER's perspective
 *   - Catches issues that developer-biased tests miss
 *   - Verifies the specification, not the implementation
 *   - If the implementation changes but behavior stays the same, tests still pass
 *
 * WHEN TO WRITE:
 *   - For any public API / user-facing behavior
 *   - When testing against a specification or requirements document
 *   - When QA writes tests (they don't see the code)
 *
 * TECHNIQUES:
 *   - Equivalence Partitioning: divide inputs into groups that should behave the same
 *   - Boundary Value Analysis: test at the edges of each partition
 *   - Decision Table: test combinations of conditions
 *
 * KEY DIFFERENCE FROM WHITE BOX:
 *   Black box: "the spec says STRONG passwords have all 5 criteria" → test that
 *   White box: "I see an if/else chain with 5 checks" → test each branch
 *
 * NOTICE: We test based on the SPECIFICATION below, not by reading the code.
 *
 * SPECIFICATION:
 *   A password is STRONG if it meets ALL of these:
 *     - At least 8 characters
 *     - At least 1 uppercase letter
 *     - At least 1 lowercase letter
 *     - At least 1 digit
 *     - At least 1 special character (!@#$%^&*)
 *   FAIR = fails exactly 1 rule
 *   WEAK = fails 2+ rules
 */
@DisplayName("Black Box Test — PasswordValidator")
class BlackBoxTest_PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    // =================================================================
    //  Technique 1: Equivalence Partitioning
    //  Divide inputs into classes that should produce the same result
    // =================================================================

    @Nested
    @DisplayName("Equivalence Partitioning — group inputs by expected result")
    class EquivalencePartitioning {

        @Test
        @DisplayName("STRONG — meets all criteria")
        void strongPassword() {
            // Any password meeting all 5 rules should be STRONG
            assertThat(validator.validate("Abcdef1!")).isEqualTo(Strength.STRONG);
            assertThat(validator.validate("MyP@ssw0rd")).isEqualTo(Strength.STRONG);
            assertThat(validator.validate("C0mpl3x!Pass")).isEqualTo(Strength.STRONG);
        }

        @Test
        @DisplayName("FAIR — fails exactly 1 rule")
        void fairPassword() {
            // Missing only special character
            assertThat(validator.validate("Abcdefg1")).isEqualTo(Strength.FAIR);
            // Missing only digit
            assertThat(validator.validate("Abcdefg!")).isEqualTo(Strength.FAIR);
        }

        @Test
        @DisplayName("WEAK — fails 2+ rules")
        void weakPassword() {
            // Too short + no special char + no digit
            assertThat(validator.validate("abc")).isEqualTo(Strength.WEAK);
            // Only lowercase
            assertThat(validator.validate("abcdefgh")).isEqualTo(Strength.WEAK);
            // Only digits
            assertThat(validator.validate("12345678")).isEqualTo(Strength.WEAK);
        }
    }

    // =================================================================
    //  Technique 2: Boundary Value Analysis
    //  Test at the EDGES of each partition
    // =================================================================

    @Nested
    @DisplayName("Boundary Value Analysis — test at the edges")
    class BoundaryValueAnalysis {

        @Test
        @DisplayName("exactly 7 characters (just below minimum)")
        void justBelowMinLength() {
            // 7 chars — fails length rule
            assertThat(validator.validate("Abc1!xx")).isEqualTo(Strength.FAIR);
        }

        @Test
        @DisplayName("exactly 8 characters (at minimum)")
        void atMinLength() {
            // 8 chars — passes length rule (if all other rules met)
            assertThat(validator.validate("Abcde1!x")).isEqualTo(Strength.STRONG);
        }

        @Test
        @DisplayName("exactly 9 characters (just above minimum)")
        void justAboveMinLength() {
            assertThat(validator.validate("Abcde1!xx")).isEqualTo(Strength.STRONG);
        }
    }

    // =================================================================
    //  Technique 3: Edge Cases
    //  Test unusual, extreme, or unexpected inputs
    // =================================================================

    @Nested
    @DisplayName("Edge Cases — unusual inputs")
    class EdgeCases {

        @ParameterizedTest
        @NullAndEmptySource  // tests both null and ""
        @DisplayName("null and empty passwords should be WEAK")
        void nullAndEmpty(String password) {
            assertThat(validator.validate(password)).isEqualTo(Strength.WEAK);
        }

        @Test
        @DisplayName("single character should be WEAK")
        void singleChar() {
            assertThat(validator.validate("a")).isEqualTo(Strength.WEAK);
        }

        @Test
        @DisplayName("very long password meeting all criteria should be STRONG")
        void veryLong() {
            assertThat(validator.validate("A" + "a".repeat(100) + "1!")).isEqualTo(Strength.STRONG);
        }
    }

    // =================================================================
    //  Parameterized Tests — test many inputs at once
    //  Great for black box testing with a decision table
    // =================================================================

    @ParameterizedTest(name = "password \"{0}\" should be {1}")
    @CsvSource({
            // password,           expected strength
            "Abcdef1!,             STRONG",    // all criteria met
            "ABCDEF1!,             FAIR",      // no lowercase
            "abcdef1!,             FAIR",      // no uppercase
            "Abcdefg!,             FAIR",      // no digit
            "Abcdefg1,             FAIR",      // no special char
            "abcdefgh,             WEAK",      // no uppercase + no digit + no special
            "12345678,             WEAK",      // no letters + no special
            "ab1!,                 WEAK",      // too short + no uppercase (2 fails)
    })
    @DisplayName("Decision table — various password combinations")
    void decisionTable(String password, Strength expected) {
        assertThat(validator.validate(password)).isEqualTo(expected);
    }

    // =================================================================
    //  isAcceptable() — black box test of a higher-level method
    // =================================================================

    @Nested
    @DisplayName("isAcceptable() — should accept STRONG and FAIR passwords")
    class IsAcceptable {

        @ParameterizedTest
        @ValueSource(strings = {"Abcdef1!", "MyP@ssw0rd", "Abcdefg1"})
        @DisplayName("should accept STRONG and FAIR passwords")
        void acceptable(String password) {
            assertThat(validator.isAcceptable(password)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "12345678", "", "aaa"})
        @DisplayName("should reject WEAK passwords")
        void notAcceptable(String password) {
            assertThat(validator.isAcceptable(password)).isFalse();
        }
    }
}
