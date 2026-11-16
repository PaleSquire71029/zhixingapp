package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.LinearCombinationTrigonometricEquation
import ci.us.zhixing.app.domain.math.model.LinearCombinationTrigonometricEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionInterval
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalResult
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineEquation
import ci.us.zhixing.app.domain.math.model.SineEquationSolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class LinearCombinationTrigonometricEquationSolverTest {
    @Test
    fun auxiliaryAngleIdentityProducesExpectedSingleBoundaryFamily() {
        // 3 sin(x) + 4 cos(x) = 5 -> 5 sin(x + atan2(4, 3)) = 5.
        val result = solve(a = 3.0, b = 4.0, w = 1.0, phase = 0.0, target = 5.0)
        val family = singleFamily(result)

        assertEquals(PI / 2.0 - atan2(4.0, 3.0), family.firstSolution, 1e-12)
        assertEquals(2.0 * PI, family.period, 1e-12)
        assertOriginalEquation(result, a = 3.0, b = 4.0, w = 1.0, phase = 0.0, target = 5.0)
    }

    @Test
    fun mixedCombinationReturnsTwoFamiliesAndSupportsNegativeTarget() {
        val result = solve(a = 3.0, b = 4.0, w = 2.0, phase = PI / 6.0, target = -2.5)
        val families = periodicFamilies(result)

        assertEquals(2, families.size)
        assertTrue(families.all { it.period == PI })
        assertTrue(families[0].firstSolution < families[1].firstSolution)
        assertOriginalEquation(result, a = 3.0, b = 4.0, w = 2.0, phase = PI / 6.0, target = -2.5)
    }

    @Test
    fun zeroCosineCoefficientMatchesExistingSineEquation() {
        val result = solve(a = 2.0, b = 0.0, w = -1.0, phase = PI / 4.0, target = 1.0)
        val actualFamilies = periodicFamilies(result)
        val expected = SineEquationSolver.solve(
            SineEquation(
                coefficient = -1.0,
                phase = PI / 4.0,
                target = 0.5,
            ),
        )

        assertTrue(expected is SineEquationSolutionResult.PeriodicSolutions)
        assertEquals(
            (expected as SineEquationSolutionResult.PeriodicSolutions).families,
            actualFamilies,
        )
        assertEquals(2, actualFamilies.size)
    }

    @Test
    fun zeroSineCoefficientMatchesCosineEquation() {
        val result = solve(a = 0.0, b = 1.0, w = 1.0, phase = 0.0, target = 0.0)
        val families = periodicFamilies(result)

        assertEquals(2, families.size)
        assertEquals(PI / 2.0, families[0].firstSolution, 1e-12)
        assertEquals(3.0 * PI / 2.0, families[1].firstSolution, 1e-12)
    }

    @Test
    fun negativeCoefficientsKeepPositivePeriodAndCorrectSolutions() {
        val result = solve(a = -3.0, b = -4.0, w = -2.0, phase = -PI / 3.0, target = 1.0)
        val families = periodicFamilies(result)

        assertEquals(2, families.size)
        assertTrue(families.all { it.period > 0.0 })
        assertOriginalEquation(result, a = -3.0, b = -4.0, w = -2.0, phase = -PI / 3.0, target = 1.0)
    }

    @Test
    fun targetBeyondAmplitudeHasNoSolutions() {
        val result = solve(a = 3.0, b = 4.0, w = 1.0, phase = 0.0, target = 5.0001)
        assertTrue(result === LinearCombinationTrigonometricEquationSolutionResult.NoSolutions)
    }

    @Test
    fun positiveAndNegativeAmplitudeBoundariesEachProduceOneFamily() {
        val positive = singleFamily(
            solve(a = 3.0, b = 4.0, w = 1.0, phase = 0.0, target = 5.0),
        )
        val negative = singleFamily(
            solve(a = 3.0, b = 4.0, w = 1.0, phase = 0.0, target = -5.0),
        )
        assertTrue(positive.firstSolution != negative.firstSolution)
    }

    @Test
    fun zeroAmplitudeAndZeroTargetIsIdentity() {
        val result = solve(a = 0.0, b = 0.0, w = 7.0, phase = 2.0, target = 0.0)
        assertTrue(result === LinearCombinationTrigonometricEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun zeroAmplitudeAndNonZeroTargetIsContradiction() {
        val result = solve(a = 0.0, b = 0.0, w = 7.0, phase = 2.0, target = 0.1)
        assertTrue(result === LinearCombinationTrigonometricEquationSolutionResult.NoSolutions)
    }

    @Test
    fun zeroAngularCoefficientClassifiesConstantCombination() {
        val identity = solve(a = 3.0, b = 4.0, w = 0.0, phase = 0.0, target = 4.0)
        val contradiction = solve(a = 3.0, b = 4.0, w = 0.0, phase = 0.0, target = 3.0)

        assertTrue(identity === LinearCombinationTrigonometricEquationSolutionResult.AllRealNumbers)
        assertTrue(contradiction === LinearCombinationTrigonometricEquationSolutionResult.NoSolutions)
    }

    @Test
    fun nonFiniteInputsReturnTypedFailure() {
        val invalid = listOf(
            LinearCombinationTrigonometricEquation(Double.NaN, 1.0, 1.0, 0.0, 0.0),
            LinearCombinationTrigonometricEquation(1.0, Double.POSITIVE_INFINITY, 1.0, 0.0, 0.0),
            LinearCombinationTrigonometricEquation(1.0, 1.0, Double.NaN, 0.0, 0.0),
            LinearCombinationTrigonometricEquation(1.0, 1.0, 1.0, Double.NEGATIVE_INFINITY, 0.0),
            LinearCombinationTrigonometricEquation(1.0, 1.0, 1.0, 0.0, Double.NaN),
        )

        invalid.forEach { equation ->
            val result = LinearCombinationTrigonometricEquationSolver.solve(equation)
            assertFailure(result, PolynomialErrorCode.NON_FINITE_INPUT)
        }
    }

    @Test
    fun amplitudeOverflowReturnsTypedFailure() {
        val result = solve(
            a = Double.MAX_VALUE,
            b = Double.MAX_VALUE,
            w = 1.0,
            phase = 0.0,
            target = 0.0,
        )

        assertFailure(result, PolynomialErrorCode.NON_FINITE_RESULT)
    }

    @Test
    fun nonZeroTargetThatUnderflowsDuringNormalizationReturnsPrecisionFailure() {
        val result = solve(
            a = 1.0e308,
            b = 1.0e308,
            w = 1.0,
            phase = 0.0,
            target = Double.MIN_VALUE,
        )

        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun lostAuxiliaryPhaseShiftReturnsPrecisionFailure() {
        val result = solve(a = 1.0, b = 1.0, w = 1.0, phase = 1.0e18, target = 0.0)
        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun generatedFamiliesCanBeFilteredToFiniteInterval() {
        val result = solve(a = 3.0, b = 4.0, w = 1.0, phase = 0.0, target = 0.0)
        val families = periodicFamilies(result)
        val filtered = PeriodicSolutionIntervalFilter.filter(
            families,
            PeriodicSolutionInterval(0.0, 2.0 * PI),
        )

        assertTrue(filtered is PeriodicSolutionIntervalResult.FiniteSolutions)
        assertTrue((filtered as PeriodicSolutionIntervalResult.FiniteSolutions).values.isNotEmpty())
    }

    private fun solve(
        a: Double,
        b: Double,
        w: Double,
        phase: Double,
        target: Double,
    ) = LinearCombinationTrigonometricEquationSolver.solve(
        LinearCombinationTrigonometricEquation(
            sineCoefficient = a,
            cosineCoefficient = b,
            angularCoefficient = w,
            phase = phase,
            target = target,
        ),
    )

    private fun singleFamily(
        result: LinearCombinationTrigonometricEquationSolutionResult,
    ) = periodicFamilies(result).single()

    private fun periodicFamilies(
        result: LinearCombinationTrigonometricEquationSolutionResult,
    ) = when (result) {
        is LinearCombinationTrigonometricEquationSolutionResult.PeriodicSolutions -> result.families
        LinearCombinationTrigonometricEquationSolutionResult.AllRealNumbers ->
            error("Expected periodic solutions, got all real numbers")
        LinearCombinationTrigonometricEquationSolutionResult.NoSolutions ->
            error("Expected periodic solutions, got no solutions")
        is LinearCombinationTrigonometricEquationSolutionResult.Failure ->
            error("Expected periodic solutions, got ${result.error}")
    }

    private fun assertFailure(
        result: LinearCombinationTrigonometricEquationSolutionResult,
        expected: PolynomialErrorCode,
    ) {
        assertTrue("Expected failure, got $result", result is LinearCombinationTrigonometricEquationSolutionResult.Failure)
        assertEquals(
            expected,
            (result as LinearCombinationTrigonometricEquationSolutionResult.Failure).error.code,
        )
    }

    private fun assertOriginalEquation(
        result: LinearCombinationTrigonometricEquationSolutionResult,
        a: Double,
        b: Double,
        w: Double,
        phase: Double,
        target: Double,
    ) {
        periodicFamilies(result).forEach { family ->
            val angle = w * family.firstSolution + phase
            val actual = a * sin(angle) + b * cos(angle)
            val scale = maxOf(1.0, kotlin.math.abs(target), kotlin.math.hypot(a, b))
            assertTrue("Original equation residual too large: actual=$actual target=$target",
                kotlin.math.abs(actual - target) / scale <= 1e-12)
        }
    }
}
