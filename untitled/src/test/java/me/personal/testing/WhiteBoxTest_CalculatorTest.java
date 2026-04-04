package me.personal.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * =====================================================================
 *  WHITE BOX TEST
 * =====================================================================
 *
 * WHAT:
 *   Tests based on KNOWLEDGE OF THE SOURCE CODE.
 *   You READ the implementation and design tests to cover:
 *     - Every branch (if/else)
 *     - Every path through the code
 *     - Edge cases you see in the logic
 *
 * WHY:
 *   - Ensures every line of code is exercised
 *   - Catches dead code, unreachable branches
 *   - Finds bugs hidden in specific code paths
 *   - Maximizes code coverage
 *
 * WHEN TO WRITE:
 *   - When you need high coverage for critical business logic
 *   - When the spec is incomplete (you derive tests from the code itself)
 *   - For complex methods with many branches
 *
 * COVERAGE LEVELS (from least to most thorough):
 *
 *   1. Statement Coverage
 *      → Every LINE of code is executed at least once.
 *      → Weakest level. A single test can achieve high statement coverage
 *        but miss entire branches.
 *
 *   2. Branch Coverage (Decision Coverage)
 *      → Every IF/ELSE branch is taken at least once.
 *      → Tests both the TRUE and FALSE paths of every condition.
 *
 *   3. Path Coverage
 *      → Every possible PATH through the code is tested.
 *      → Combinations of branches. Most thorough but exponential growth.
 *      → For 3 independent if-statements: 2^3 = 8 paths.
 *
 * KEY DIFFERENCE FROM BLACK BOX:
 *   Black box: "spec says grade A for score >= 90" → test 90 and 95
 *   White box: "I see if/else at 90, 80, 70, 60" → test 89, 90 (every boundary in the CODE)
 */
@DisplayName("White Box Test — Calculator (testing every branch and path)")
class WhiteBoxTest_CalculatorTest {

    private final Calculator calculator = new Calculator();

    // =================================================================
    //  Branch Coverage — test every if/else in getGrade()
    // =================================================================
    //
    //  Looking at the source code of getGrade():
    //
    //    if (score < 0 || score > 100) throw ...   ← Branch 1
    //    if (score >= 90) return "A"                ← Branch 2
    //    if (score >= 80) return "B"                ← Branch 3
    //    if (score >= 70) return "C"                ← Branch 4
    //    if (score >= 60) return "D"                ← Branch 5
    //    return "F"                                 ← Branch 6 (else)
    //
    //  We need at least 1 test per branch:
    // =================================================================

    @Nested
    @DisplayName("getGrade() — Branch Coverage")
    class GetGradeBranchCoverage {

        @Test
        @DisplayName("Branch 1a: score < 0 → exception")
        void negativeScore() {
            assertThatThrownBy(() -> calculator.getGrade(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Branch 1b: score > 100 → exception")
        void scoreAbove100() {
            assertThatThrownBy(() -> calculator.getGrade(101))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Branch 2: score >= 90 → A")
        void gradeA() {
            assertThat(calculator.getGrade(95)).isEqualTo("A");
        }

        @Test
        @DisplayName("Branch 3: score >= 80 → B")
        void gradeB() {
            assertThat(calculator.getGrade(85)).isEqualTo("B");
        }

        @Test
        @DisplayName("Branch 4: score >= 70 → C")
        void gradeC() {
            assertThat(calculator.getGrade(75)).isEqualTo("C");
        }

        @Test
        @DisplayName("Branch 5: score >= 60 → D")
        void gradeD() {
            assertThat(calculator.getGrade(65)).isEqualTo("D");
        }

        @Test
        @DisplayName("Branch 6: score < 60 → F")
        void gradeF() {
            assertThat(calculator.getGrade(50)).isEqualTo("F");
        }

        // Test at exact boundaries (where the branch condition changes)
        @Test
        @DisplayName("Boundary: exactly 90 → A, exactly 89 → B")
        void boundaryAB() {
            assertThat(calculator.getGrade(90)).isEqualTo("A");
            assertThat(calculator.getGrade(89)).isEqualTo("B");
        }

        @Test
        @DisplayName("Boundary: exactly 80 → B, exactly 79 → C")
        void boundaryBC() {
            assertThat(calculator.getGrade(80)).isEqualTo("B");
            assertThat(calculator.getGrade(79)).isEqualTo("C");
        }

        @Test
        @DisplayName("Boundary: exactly 0 → F, exactly 100 → A")
        void extremeBoundaries() {
            assertThat(calculator.getGrade(0)).isEqualTo("F");
            assertThat(calculator.getGrade(100)).isEqualTo("A");
        }
    }

    // =================================================================
    //  Path Coverage — test every COMBINATION of branches in calculateDiscount()
    // =================================================================
    //
    //  Looking at the source code:
    //
    //    if (isMember)            → +10%        (branch A: true/false)
    //    if (quantity >= 10)      → +15%        (branch B: true/false)
    //    else if (quantity >= 5)  → +5%         (branch C: true/false)
    //    if (price > 1000 && isMember) → +5%    (branch D: true/false)
    //
    //  Paths to cover (all combinations):
    //    Path 1: not member, qty < 5                    → 0% discount
    //    Path 2: member, qty < 5, price <= 1000         → 10%
    //    Path 3: member, qty < 5, price > 1000          → 10% + 5% = 15%
    //    Path 4: not member, qty 5-9                    → 5%
    //    Path 5: member, qty 5-9, price <= 1000         → 10% + 5% = 15%
    //    Path 6: member, qty 5-9, price > 1000          → 10% + 5% + 5% = 20%
    //    Path 7: not member, qty >= 10                  → 15%
    //    Path 8: member, qty >= 10, price <= 1000       → 10% + 15% = 25%
    //    Path 9: member, qty >= 10, price > 1000        → 10% + 15% + 5% = 30%
    // =================================================================

    @Nested
    @DisplayName("calculateDiscount() — Path Coverage")
    class CalculateDiscountPathCoverage {

        @Test
        @DisplayName("Path 1: non-member, qty < 5 → 0% discount")
        void path1_noDiscount() {
            double result = calculator.calculateDiscount(100, false, 2);
            assertThat(result).isEqualTo(100.0);  // 0% off
        }

        @Test
        @DisplayName("Path 2: member, qty < 5, price <= 1000 → 10% discount")
        void path2_memberOnly() {
            double result = calculator.calculateDiscount(100, true, 2);
            assertThat(result).isEqualTo(90.0);  // 10% off
        }

        @Test
        @DisplayName("Path 3: member, qty < 5, price > 1000 → 15% discount")
        void path3_memberHighValue() {
            double result = calculator.calculateDiscount(2000, true, 2);
            assertThat(result).isEqualTo(1700.0);  // 10% + 5% = 15% off
        }

        @Test
        @DisplayName("Path 4: non-member, qty 5-9 → 5% discount")
        void path4_smallBulk() {
            double result = calculator.calculateDiscount(100, false, 5);
            assertThat(result).isEqualTo(95.0);  // 5% off
        }

        @Test
        @DisplayName("Path 5: member, qty 5-9, price <= 1000 → 15% discount")
        void path5_memberSmallBulk() {
            double result = calculator.calculateDiscount(100, true, 7);
            assertThat(result).isEqualTo(85.0);  // 10% + 5% = 15% off
        }

        @Test
        @DisplayName("Path 6: member, qty 5-9, price > 1000 → 20% discount")
        void path6_memberSmallBulkHighValue() {
            double result = calculator.calculateDiscount(2000, true, 7);
            assertThat(result).isEqualTo(1600.0);  // 10% + 5% + 5% = 20% off
        }

        @Test
        @DisplayName("Path 7: non-member, qty >= 10 → 15% discount")
        void path7_bigBulk() {
            double result = calculator.calculateDiscount(100, false, 10);
            assertThat(result).isEqualTo(85.0);  // 15% off
        }

        @Test
        @DisplayName("Path 8: member, qty >= 10, price <= 1000 → 25% discount")
        void path8_memberBigBulk() {
            double result = calculator.calculateDiscount(100, true, 10);
            assertThat(result).isEqualTo(75.0);  // 10% + 15% = 25% off
        }

        @Test
        @DisplayName("Path 9: member, qty >= 10, price > 1000 → 30% discount")
        void path9_allDiscounts() {
            double result = calculator.calculateDiscount(2000, true, 10);
            assertThat(result).isEqualTo(1400.0);  // 10% + 15% + 5% = 30% off
        }
    }
}
