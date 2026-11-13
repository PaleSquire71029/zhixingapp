package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialComparison
import ci.us.zhixing.app.domain.math.model.PolynomialInequality
import ci.us.zhixing.app.domain.math.model.PolynomialInequalitySolutionResult
import ci.us.zhixing.app.domain.math.model.RealInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolynomialInequalitySolverTest {
    @Test
    fun quadraticGreaterThanOrEqualZeroReturnsTwoOuterClosedIntervals() {
        // x² - 1 >= 0
        val result = solve(
            Polynomial.of(-1.0, 0.0, 1.0),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(
                RealInterval(null, false, -1.0, true),
                RealInterval(1.0, true, null, false),
            ),
            result,
        )
    }

    @Test
    fun quadraticStrictGreaterThanZeroKeepsBothRootsExcluded() {
        // x² - 1 > 0
        val result = solve(
            Polynomial.of(-1.0, 0.0, 1.0),
            PolynomialComparison.GREATER_THAN,
        )

        assertEquals(
            intervals(
                RealInterval(null, false, -1.0, false),
                RealInterval(1.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun quadraticLessThanOrEqualZeroReturnsClosedMiddleInterval() {
        // x² - 1 <= 0
        val result = solve(
            Polynomial.of(-1.0, 0.0, 1.0),
            PolynomialComparison.LESS_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(RealInterval(-1.0, true, 1.0, true)),
            result,
        )
    }

    @Test
    fun linearInequalitiesRespectStrictAndInclusiveEndpoints() {
        // -2x + 4 > 0 gives x < 2; >= 0 includes x = 2.
        val strict = solve(
            Polynomial.of(4.0, -2.0),
            PolynomialComparison.GREATER_THAN,
        )
        val nonStrict = solve(
            Polynomial.of(4.0, -2.0),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(RealInterval(null, false, 2.0, false)),
            strict,
        )
        assertEquals(
            intervals(RealInterval(null, false, 2.0, true)),
            nonStrict,
        )
    }

    @Test
    fun quadraticWithNoRealRootsHasConstantSignOnTheWholeLine() {
        // x² + 1 > 0
        val positive = solve(
            Polynomial.of(1.0, 0.0, 1.0),
            PolynomialComparison.GREATER_THAN,
        )
        val negative = solve(
            Polynomial.of(1.0, 0.0, 1.0),
            PolynomialComparison.LESS_THAN,
        )

        assertTrue(positive === PolynomialInequalitySolutionResult.AllRealNumbers)
        assertTrue(negative === PolynomialInequalitySolutionResult.NoSolutions)
    }

    @Test
    fun doubleRootDoesNotFlipSignAndStrictInequalityKeepsTheHole() {
        // (x - 1)² > 0
        val strict = solve(
            Polynomial.of(1.0, -2.0, 1.0),
            PolynomialComparison.GREATER_THAN,
        )
        val nonStrict = solve(
            Polynomial.of(1.0, -2.0, 1.0),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(
                RealInterval(null, false, 1.0, false),
                RealInterval(1.0, false, null, false),
            ),
            strict,
        )
        assertTrue(nonStrict === PolynomialInequalitySolutionResult.AllRealNumbers)
    }

    @Test
    fun doubleRootLessThanOrEqualZeroReturnsOnlyTheRepeatedRoot() {
        // (x - 1)² <= 0
        val result = solve(
            Polynomial.of(1.0, -2.0, 1.0),
            PolynomialComparison.LESS_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(RealInterval(1.0, true, 1.0, true)),
            result,
        )
    }

    @Test
    fun cubicWithThreeRootsBuildsAlternatingIntervals() {
        // (x + 2)(x - 1)(x - 3) > 0
        // x³ - 2x² - 5x + 6 > 0
        val result = solve(
            Polynomial.of(6.0, -5.0, -2.0, 1.0),
            PolynomialComparison.GREATER_THAN,
        )

        assertIntervals(
            expected = listOf(
                RealInterval(-2.0, false, 1.0, false),
                RealInterval(3.0, false, null, false),
            ),
            actual = result,
        )
    }

    @Test
    fun cubicNonStrictInequalityIncludesAllRootsOfTheNegativeRegions() {
        // (x + 2)(x - 1)(x - 3) <= 0
        val result = solve(
            Polynomial.of(6.0, -5.0, -2.0, 1.0),
            PolynomialComparison.LESS_THAN_OR_EQUAL,
        )

        assertIntervals(
            expected = listOf(
                RealInterval(null, false, -2.0, true),
                RealInterval(1.0, true, 3.0, true),
            ),
            actual = result,
        )
    }

    @Test
    fun cubicWithOneDoubleRootAndOneSimpleRootDoesNotFlipAtTheDoubleRoot() {
        // (x - 1)²(x + 2) <= 0: x <= -2, plus the isolated double root x = 1.
        val result = solve(
            Polynomial.of(2.0, -3.0, 0.0, 1.0),
            PolynomialComparison.LESS_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(
                RealInterval(null, false, -2.0, true),
                RealInterval(1.0, true, 1.0, true),
            ),
            result,
        )
    }

    @Test
    fun tripleRootUsesTheSameSignChartRules() {
        // (x - 2)³ >= 0
        val result = solve(
            Polynomial.of(-8.0, 12.0, -6.0, 1.0),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertEquals(
            intervals(RealInterval(2.0, true, null, false)),
            result,
        )
    }

    @Test
    fun zeroPolynomialDistinguishesStrictAndNonStrictComparisons() {
        val all = solve(Polynomial.constant(0.0), PolynomialComparison.LESS_THAN_OR_EQUAL)
        val none = solve(Polynomial.constant(0.0), PolynomialComparison.LESS_THAN)

        assertTrue(all === PolynomialInequalitySolutionResult.AllRealNumbers)
        assertTrue(none === PolynomialInequalitySolutionResult.NoSolutions)
    }

    @Test
    fun negativeAndPositiveConstantsAreClassified() {
        val positive = solve(Polynomial.constant(3.0), PolynomialComparison.GREATER_THAN)
        val negative = solve(Polynomial.constant(-3.0), PolynomialComparison.GREATER_THAN)

        assertTrue(positive === PolynomialInequalitySolutionResult.AllRealNumbers)
        assertTrue(negative === PolynomialInequalitySolutionResult.NoSolutions)
    }

    @Test
    fun fourthDegreeInequalityIsExplicitlyUnsupported() {
        val result = solve(
            Polynomial.of(0.0, 0.0, 0.0, 0.0, 1.0),
            PolynomialComparison.GREATER_THAN,
        )

        assertEquals(
            PolynomialInequalitySolutionResult.UnsupportedDegree(4),
            result,
        )
    }

    private fun assertIntervals(
        expected: List<RealInterval>,
        actual: PolynomialInequalitySolutionResult,
    ) {
        assertTrue(actual is PolynomialInequalitySolutionResult.Intervals)
        val actualIntervals = (actual as PolynomialInequalitySolutionResult.Intervals).intervals
        assertEquals(expected.size, actualIntervals.size)
        expected.zip(actualIntervals).forEach { (expectedInterval, actualInterval) ->
            assertBound(expectedInterval.lowerBound, actualInterval.lowerBound)
            assertEquals(expectedInterval.lowerInclusive, actualInterval.lowerInclusive)
            assertBound(expectedInterval.upperBound, actualInterval.upperBound)
            assertEquals(expectedInterval.upperInclusive, actualInterval.upperInclusive)
        }
    }

    private fun assertBound(expected: Double?, actual: Double?) {
        if (expected == null) {
            assertEquals(null, actual)
        } else {
            assertTrue(actual != null)
            assertEquals(expected, actual!!, 1e-8)
        }
    }

    private fun solve(
        polynomial: Polynomial,
        comparison: PolynomialComparison,
    ) = PolynomialInequalitySolver.solve(PolynomialInequality(polynomial, comparison))

    private fun intervals(vararg values: RealInterval) =
        PolynomialInequalitySolutionResult.Intervals(values.toList())
}
