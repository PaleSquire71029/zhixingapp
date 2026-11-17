package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionInterval
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalResult
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineEqualityEquation
import ci.us.zhixing.app.domain.math.model.SineEqualityEquationSolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class SineEqualityEquationSolverTest {
    @Test
    fun differentAngularCoefficientsProduceBothPeriodicBranches() {
        val equation = SineEqualityEquation(2.0, 0.0, 1.0, 0.0)
        val result = solve(equation)

        val families = periodicFamilies(result)
        assertEquals(4, families.size)
        assertOriginalEquation(equation, result)
    }

    @Test
    fun equalAngularCoefficientsWithDifferentPhasesProducePeriodicSolutions() {
        val equation = SineEqualityEquation(1.0, 0.0, 1.0, PI / 2.0)
        val families = periodicFamilies(solve(equation))

        assertEquals(2, families.size)
        assertTrue(families.all { abs(it.period - 2.0 * PI) <= 1e-12 })
        assertEquals(PI / 4.0, families[0].firstSolution, 1e-12)
        assertEquals(5.0 * PI / 4.0, families[1].firstSolution, 1e-12)
    }

    @Test
    fun identicalSineExpressionsReturnAllRealNumbers() {
        val result = solve(SineEqualityEquation(3.0, -0.75, 3.0, -0.75))
        assertTrue(result === SineEqualityEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun oppositeAngularCoefficientsAndOddPiPhaseSumReturnIdentity() {
        val result = solve(SineEqualityEquation(1.0, 0.0, -1.0, PI))
        assertTrue(result === SineEqualityEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun distinctConstantPhasesWithEqualSineReturnAllRealNumbers() {
        val result = solve(SineEqualityEquation(0.0, PI / 6.0, 0.0, 5.0 * PI / 6.0))
        assertTrue(result === SineEqualityEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun unequalConstantSinesReturnNoSolutions() {
        val result = solve(SineEqualityEquation(0.0, 0.0, 0.0, PI / 2.0))
        assertTrue(result === SineEqualityEquationSolutionResult.NoSolutions)
    }

    @Test
    fun negativeCoefficientsAndPhasesKeepValidPeriodicFamilies() {
        val equation = SineEqualityEquation(-2.0, PI / 3.0, 1.0, -PI / 5.0)
        val result = solve(equation)

        assertTrue(periodicFamilies(result).isNotEmpty())
        assertOriginalEquation(equation, result)
    }

    @Test
    fun nonFiniteInputsReturnTypedFailure() {
        val invalid = listOf(
            SineEqualityEquation(Double.NaN, 0.0, 1.0, 0.0),
            SineEqualityEquation(1.0, Double.POSITIVE_INFINITY, 1.0, 0.0),
            SineEqualityEquation(1.0, 0.0, Double.NEGATIVE_INFINITY, 0.0),
            SineEqualityEquation(1.0, 0.0, 1.0, Double.NaN),
        )

        invalid.forEach { equation ->
            assertFailure(
                solve(equation),
                PolynomialErrorCode.NON_FINITE_INPUT,
            )
        }
    }

    @Test
    fun oppositeExtremeCoefficientsAvoidDifferenceOverflow() {
        val equation = SineEqualityEquation(
            Double.MAX_VALUE,
            0.0,
            -Double.MAX_VALUE,
            0.0,
        )
        val result = solve(equation)

        assertEquals(2, periodicFamilies(result).size)
        assertOriginalEquation(equation, result)
    }

    @Test
    fun sameExtremeCoefficientsAvoidSumOverflow() {
        val equation = SineEqualityEquation(
            Double.MAX_VALUE,
            0.0,
            Double.MAX_VALUE,
            PI / 2.0,
        )
        val result = solve(equation)

        assertEquals(2, periodicFamilies(result).size)
        assertOriginalEquation(equation, result)
    }

    @Test
    fun nonZeroHalfCoefficientThatUnderflowsReturnsPrecisionFailure() {
        val result = solve(SineEqualityEquation(Double.MIN_VALUE, 0.0, 0.0, 0.0))
        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun phaseTooLargeToPreserveTheSecondZeroBranchReturnsPrecisionFailure() {
        val result = solve(SineEqualityEquation(1.0, 1.0e18, 0.0, 0.0))
        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun nearConstantMultipleOfPiIsNotSilentlyClassifiedAsIdentity() {
        val result = solve(
            SineEqualityEquation(
                0.0,
                0.0,
                0.0,
                Math.nextUp(2.0 * PI),
            ),
        )
        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun unrepresentablyLongPeriodReturnsTypedNonFiniteFailure() {
        val result = solve(SineEqualityEquation(1.0e-320, 0.0, 0.0, 0.0))
        assertFailure(result, PolynomialErrorCode.NON_FINITE_RESULT)
    }

    @Test
    fun generatedFamiliesCanBeFilteredToFiniteInterval() {
        val equation = SineEqualityEquation(2.0, 0.0, 1.0, 0.0)
        val families = periodicFamilies(solve(equation))
        val filtered = PeriodicSolutionIntervalFilter.filter(
            families,
            PeriodicSolutionInterval(-2.0 * PI, 2.0 * PI),
        )

        assertTrue(filtered is PeriodicSolutionIntervalResult.FiniteSolutions)
        val values = (filtered as PeriodicSolutionIntervalResult.FiniteSolutions).values
        assertTrue(values.isNotEmpty())
        values.forEach { value ->
            val left = sin(2.0 * value)
            val right = sin(value)
            assertTrue(abs(left - right) <= 1e-12)
        }
    }

    private fun solve(equation: SineEqualityEquation) =
        SineEqualityEquationSolver.solve(equation)

    private fun periodicFamilies(
        result: SineEqualityEquationSolutionResult,
    ) = when (result) {
        is SineEqualityEquationSolutionResult.PeriodicSolutions -> result.families
        SineEqualityEquationSolutionResult.AllRealNumbers ->
            error("Expected periodic solutions, got all real numbers")
        SineEqualityEquationSolutionResult.NoSolutions ->
            error("Expected periodic solutions, got no solutions")
        is SineEqualityEquationSolutionResult.Failure ->
            error("Expected periodic solutions, got ${result.error}")
    }

    private fun assertFailure(
        result: SineEqualityEquationSolutionResult,
        expected: PolynomialErrorCode,
    ) {
        assertTrue(
            "Expected failure, got $result",
            result is SineEqualityEquationSolutionResult.Failure,
        )
        assertEquals(
            expected,
            (result as SineEqualityEquationSolutionResult.Failure).error.code,
        )
    }

    private fun assertOriginalEquation(
        equation: SineEqualityEquation,
        result: SineEqualityEquationSolutionResult,
    ) {
        periodicFamilies(result).forEach { family ->
            val leftAngle = Math.fma(
                equation.leftCoefficient,
                family.firstSolution,
                equation.leftPhase,
            )
            val rightAngle = Math.fma(
                equation.rightCoefficient,
                family.firstSolution,
                equation.rightPhase,
            )
            assertTrue(leftAngle.isFinite())
            assertTrue(rightAngle.isFinite())
            assertTrue(
                abs(sin(leftAngle) - sin(rightAngle)) <= 1e-12,
            )
        }
    }
}
