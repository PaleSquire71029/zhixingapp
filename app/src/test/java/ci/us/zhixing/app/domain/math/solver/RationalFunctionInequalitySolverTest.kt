package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.RationalFunctionArithmetic
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialComparison
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.RationalFunction
import ci.us.zhixing.app.domain.math.model.RationalFunctionInequality
import ci.us.zhixing.app.domain.math.model.RationalFunctionInequalitySolutionResult
import ci.us.zhixing.app.domain.math.model.RationalFunctionOperationResult
import ci.us.zhixing.app.domain.math.model.RealInterval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RationalFunctionInequalitySolverTest {
    @Test
    fun reciprocalGreaterThanZeroReturnsPositiveHalfLine() {
        // 1/x > 0
        val result = solve(
            rational(Polynomial.constant(1.0), Polynomial.variable()),
            PolynomialComparison.GREATER_THAN,
        )

        assertIntervals(listOf(RealInterval(0.0, false, null, false)), result)
    }

    @Test
    fun reciprocalLessThanZeroReturnsNegativeHalfLine() {
        // 1/x < 0
        val result = solve(
            rational(Polynomial.constant(1.0), Polynomial.variable()),
            PolynomialComparison.LESS_THAN,
        )

        assertIntervals(listOf(RealInterval(null, false, 0.0, false)), result)
    }

    @Test
    fun numeratorZeroCanBeIncludedButDenominatorZeroNeverCan() {
        // x/(x - 1) >= 0
        val result = solve(
            rational(Polynomial.variable(), Polynomial.of(-1.0, 1.0)),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertIntervals(
            listOf(
                RealInterval(null, false, 0.0, true),
                RealInterval(1.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun strictFractionInequalityExcludesBothNumeratorAndDenominatorRoots() {
        // x/(x - 1) > 0
        val result = solve(
            rational(Polynomial.variable(), Polynomial.of(-1.0, 1.0)),
            PolynomialComparison.GREATER_THAN,
        )

        assertIntervals(
            listOf(
                RealInterval(null, false, 0.0, false),
                RealInterval(1.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun removableHoleSurvivesEvenWhenNumeratorAndDenominatorShareAFactor() {
        // (x² - 1)/(x - 1) >= 0; x = 1 is excluded by the original denominator.
        val result = solve(
            rational(Polynomial.of(-1.0, 0.0, 1.0), Polynomial.of(-1.0, 1.0)),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertIntervals(
            listOf(
                RealInterval(-1.0, true, 1.0, false),
                RealInterval(1.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun xOverXDoesNotBecomeAllRealNumbersAfterCancellation() {
        // Reduced algebraic expression is 1, but the original x/x excludes x=0.
        val result = solve(
            rational(Polynomial.variable(), Polynomial.variable()),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertIntervals(
            listOf(
                RealInterval(null, false, 0.0, false),
                RealInterval(0.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun repeatedDenominatorRootRemainsAnExcludedPointWithoutChangingItsSign() {
        // 1/(x - 1)² > 0
        val result = solve(
            rational(Polynomial.constant(1.0), Polynomial.of(1.0, -2.0, 1.0)),
            PolynomialComparison.GREATER_THAN,
        )

        assertIntervals(
            listOf(
                RealInterval(null, false, 1.0, false),
                RealInterval(1.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun inheritedDomainHoleSplitsOtherwiseValidNonStrictSolution() {
        // (x + 1)/1 >= 0, but the source expression had an excluded x = 0.
        val function = rational(
            numerator = Polynomial.of(1.0, 1.0),
            denominator = Polynomial.constant(1.0),
            restrictions = listOf(Polynomial.variable()),
        )
        val result = solve(function, PolynomialComparison.GREATER_THAN_OR_EQUAL)

        assertIntervals(
            listOf(
                RealInterval(-1.0, true, 0.0, false),
                RealInterval(0.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun inheritedDomainHoleOutsideTheAcceptedRegionDoesNotChangeItsEndpoint() {
        // (x - 1)/1 >= 0, with an unrelated original restriction x != 2.
        val function = rational(
            numerator = Polynomial.of(-1.0, 1.0),
            denominator = Polynomial.constant(1.0),
            restrictions = listOf(Polynomial.of(-2.0, 1.0)),
        )
        val result = solve(function, PolynomialComparison.GREATER_THAN_OR_EQUAL)

        assertIntervals(
            listOf(
                RealInterval(1.0, true, 2.0, false),
                RealInterval(2.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun dividingByARationalFunctionAddsItsNumeratorAsAnExcludedPoint() {
        val dividend = rational(Polynomial.constant(1.0), Polynomial.constant(1.0))
        val divisor = rational(Polynomial.of(-1.0, 1.0), Polynomial.variable())
        val quotient = success(RationalFunctionArithmetic.divide(dividend, divisor))
        val result = solve(quotient, PolynomialComparison.GREATER_THAN_OR_EQUAL)

        // Quotient is x/(x - 1), but division also preserves x != 0.
        assertIntervals(
            listOf(
                RealInterval(null, false, 0.0, false),
                RealInterval(1.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun zeroNumeratorNonStrictInequalityReturnsTheOriginalDomain() {
        // 0/x >= 0; the only excluded point is x = 0.
        val result = solve(
            rational(Polynomial.constant(0.0), Polynomial.variable()),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertIntervals(
            listOf(
                RealInterval(null, false, 0.0, false),
                RealInterval(0.0, false, null, false),
            ),
            result,
        )
    }

    @Test
    fun zeroNumeratorStrictInequalityHasNoSolutions() {
        val result = solve(
            rational(Polynomial.constant(0.0), Polynomial.variable()),
            PolynomialComparison.LESS_THAN,
        )

        assertTrue(result === RationalFunctionInequalitySolutionResult.NoSolutions)
    }

    @Test
    fun zeroNumeratorOnEverywhereDefinedFunctionCanReturnAllRealNumbers() {
        val result = solve(
            rational(Polynomial.constant(0.0), Polynomial.constant(1.0)),
            PolynomialComparison.LESS_THAN_OR_EQUAL,
        )

        assertTrue(result === RationalFunctionInequalitySolutionResult.AllRealNumbers)
    }

    @Test
    fun squaredNumeratorRootMergesAcrossBothSidesOnlyForNonStrictComparison() {
        // x²/(x² + 1) >= 0 is all real numbers; > 0 excludes x = 0.
        val function = rational(
            Polynomial.of(0.0, 0.0, 1.0),
            Polynomial.of(1.0, 0.0, 1.0),
        )
        val nonStrict = solve(function, PolynomialComparison.GREATER_THAN_OR_EQUAL)
        val strict = solve(function, PolynomialComparison.GREATER_THAN)

        assertTrue(nonStrict === RationalFunctionInequalitySolutionResult.AllRealNumbers)
        assertIntervals(
            listOf(
                RealInterval(null, false, 0.0, false),
                RealInterval(0.0, false, null, false),
            ),
            strict,
        )
    }

    @Test
    fun commonIrrationalZerosRemainExcludedAfterAlgebraicCancellation() {
        // (x² - 2)/(2x² - 4) is 1/2 except at x = ±√2.
        val result = solve(
            rational(
                Polynomial.of(-2.0, 0.0, 1.0),
                Polynomial.of(-4.0, 0.0, 2.0),
            ),
            PolynomialComparison.GREATER_THAN,
        )

        assertIntervals(
            listOf(
                RealInterval(null, false, -kotlin.math.sqrt(2.0), false),
                RealInterval(-kotlin.math.sqrt(2.0), false, kotlin.math.sqrt(2.0), false),
                RealInterval(kotlin.math.sqrt(2.0), false, null, false),
            ),
            result,
        )
    }

    @Test
    fun positiveAndNegativeConstantFractionsAreClassified() {
        val positive = solve(
            rational(Polynomial.constant(2.0), Polynomial.constant(4.0)),
            PolynomialComparison.GREATER_THAN,
        )
        val negative = solve(
            rational(Polynomial.constant(-2.0), Polynomial.constant(4.0)),
            PolynomialComparison.LESS_THAN,
        )

        assertTrue(positive === RationalFunctionInequalitySolutionResult.AllRealNumbers)
        assertTrue(negative === RationalFunctionInequalitySolutionResult.AllRealNumbers)
    }

    @Test
    fun fourthDegreeInAnyRelevantPolynomialIsExplicitlyUnsupported() {
        val highDegree = Polynomial.of(0.0, 0.0, 0.0, 0.0, 1.0)
        val numerator = solve(
            rational(highDegree, Polynomial.constant(1.0)),
            PolynomialComparison.GREATER_THAN,
        )
        val domainRestriction = solve(
            rational(
                Polynomial.constant(1.0),
                Polynomial.constant(1.0),
                restrictions = listOf(highDegree),
            ),
            PolynomialComparison.GREATER_THAN,
        )

        assertEquals(
            RationalFunctionInequalitySolutionResult.UnsupportedDegree(4),
            numerator,
        )
        assertEquals(
            RationalFunctionInequalitySolutionResult.UnsupportedDegree(4),
            domainRestriction,
        )
    }

    @Test
    fun adjacentUnresolvableNumeratorAndDenominatorRootsReturnPrecisionFailure() {
        val next = Math.nextUp(1.0)
        val result = solve(
            rational(
                numerator = Polynomial.of(-1.0, 1.0),
                denominator = Polynomial.of(-next, 1.0),
            ),
            PolynomialComparison.GREATER_THAN_OR_EQUAL,
        )

        assertTrue(result is RationalFunctionInequalitySolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as RationalFunctionInequalitySolutionResult.Failure).error.code,
        )
    }

    private fun solve(
        function: RationalFunction,
        comparison: PolynomialComparison,
    ) = RationalFunctionInequalitySolver.solve(RationalFunctionInequality(function, comparison))

    private fun rational(
        numerator: Polynomial,
        denominator: Polynomial,
        restrictions: List<Polynomial> = emptyList(),
    ): RationalFunction = success(
        RationalFunction.create(numerator, denominator, restrictions),
    )

    private fun success(result: RationalFunctionOperationResult): RationalFunction {
        assertTrue(result is RationalFunctionOperationResult.Success)
        return (result as RationalFunctionOperationResult.Success).value
    }

    private fun assertIntervals(
        expected: List<RealInterval>,
        result: RationalFunctionInequalitySolutionResult,
    ) {
        assertTrue(result is RationalFunctionInequalitySolutionResult.Intervals)
        val actual = (result as RationalFunctionInequalitySolutionResult.Intervals).intervals
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (left, right) ->
            assertBound(left.lowerBound, right.lowerBound)
            assertEquals(left.lowerInclusive, right.lowerInclusive)
            assertBound(left.upperBound, right.upperBound)
            assertEquals(left.upperInclusive, right.upperInclusive)
        }
    }

    private fun assertBound(expected: Double?, actual: Double?) {
        if (expected == null) {
            assertEquals(null, actual)
        } else {
            assertTrue(actual != null)
            assertEquals(expected, actual!!, 1e-9)
        }
    }
}
