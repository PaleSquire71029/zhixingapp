package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionInterval
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionIntervalResult
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineCosineEqualityEquation
import ci.us.zhixing.app.domain.math.model.SineCosineEqualityEquationSolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SineCosineEqualityEquationSolverTest {
    @Test
    fun differentAngularCoefficientsProducePeriodicFamilies() {
        val equation = SineCosineEqualityEquation(2.0, 0.0, 1.0, 0.0)
        val result = solve(equation)

        assertTrue(periodicFamilies(result).isNotEmpty())
        assertOriginalEquation(equation, result)
    }

    @Test
    fun identicalSineAndCosineFunctionsReturnAllRealNumbers() {
        val result = solve(
            SineCosineEqualityEquation(
                sineCoefficient = 1.0,
                sinePhase = 0.0,
                cosineCoefficient = -1.0,
                cosinePhase = PI / 2.0,
            ),
        )
        assertTrue(result === SineCosineEqualityEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun equalConstantValuesReturnAllRealNumbers() {
        val result = solve(
            SineCosineEqualityEquation(
                sineCoefficient = 0.0,
                sinePhase = PI / 6.0,
                cosineCoefficient = 0.0,
                cosinePhase = PI / 3.0,
            ),
        )
        assertTrue(result === SineCosineEqualityEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun unequalConstantValuesReturnNoSolutions() {
        val result = solve(SineCosineEqualityEquation(0.0, 0.0, 0.0, 0.0))
        assertTrue(result === SineCosineEqualityEquationSolutionResult.NoSolutions)
    }

    @Test
    fun sinXEqualsCosXProducesExpectedPeriodicFamilies() {
        val equation = SineCosineEqualityEquation(1.0, 0.0, 1.0, 0.0)
        val families = periodicFamilies(solve(equation))

        assertEquals(2, families.size)
        assertTrue(families.all { abs(it.period - 2.0 * PI) <= 1e-12 })
        assertEquals(PI / 4.0, families[0].firstSolution, 1e-12)
        assertEquals(5.0 * PI / 4.0, families[1].firstSolution, 1e-12)
        assertOriginalEquation(equation, SineCosineEqualityEquationSolutionResult.PeriodicSolutions(families))
    }

    @Test
    fun constantSineAgainstVariableCosineProducesTwoFamilies() {
        val equation = SineCosineEqualityEquation(0.0, PI / 6.0, 1.0, 0.0)
        val families = periodicFamilies(solve(equation))

        assertEquals(2, families.size)
        assertOriginalEquation(equation, SineCosineEqualityEquationSolutionResult.PeriodicSolutions(families))
    }

    @Test
    fun variableSineAgainstConstantCosineProducesTwoFamilies() {
        val equation = SineCosineEqualityEquation(1.0, 0.0, 0.0, PI / 3.0)
        val families = periodicFamilies(solve(equation))

        assertEquals(2, families.size)
        assertOriginalEquation(equation, SineCosineEqualityEquationSolutionResult.PeriodicSolutions(families))
    }

    @Test
    fun negativeCoefficientsAndPhasesKeepValidFamilies() {
        val equation = SineCosineEqualityEquation(-2.0, PI / 3.0, 1.0, -PI / 5.0)
        val result = solve(equation)

        assertTrue(periodicFamilies(result).isNotEmpty())
        assertOriginalEquation(equation, result)
    }

    @Test
    fun nonFiniteInputReturnsTypedFailure() {
        val invalid = listOf(
            SineCosineEqualityEquation(Double.NaN, 0.0, 1.0, 0.0),
            SineCosineEqualityEquation(1.0, Double.POSITIVE_INFINITY, 1.0, 0.0),
            SineCosineEqualityEquation(1.0, 0.0, Double.NEGATIVE_INFINITY, 0.0),
            SineCosineEqualityEquation(1.0, 0.0, 1.0, Double.NaN),
        )

        invalid.forEach { equation ->
            assertFailure(solve(equation), PolynomialErrorCode.NON_FINITE_INPUT)
        }
    }

    @Test
    fun largeCosinePhaseThatLosesTheHalfPiShiftReturnsPrecisionFailure() {
        val result = solve(
            SineCosineEqualityEquation(
                sineCoefficient = 0.0,
                sinePhase = 0.0,
                cosineCoefficient = 0.0,
                cosinePhase = 1.0e15,
            ),
        )
        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun maximumCosinePhaseThatSwallowsTheHalfPiShiftReturnsPrecisionFailure() {
        val result = solve(
            SineCosineEqualityEquation(
                sineCoefficient = 0.0,
                sinePhase = 0.0,
                cosineCoefficient = 0.0,
                cosinePhase = Double.MAX_VALUE,
            ),
        )
        assertFailure(result, PolynomialErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun unrepresentablyLongPeriodReturnsTypedFailure() {
        val result = solve(
            SineCosineEqualityEquation(
                sineCoefficient = 0.0,
                sinePhase = 0.0,
                cosineCoefficient = 1.0e-320,
                cosinePhase = 0.0,
            ),
        )
        assertFailure(result, PolynomialErrorCode.NON_FINITE_RESULT)
    }

    @Test
    fun extremeFiniteCoefficientsRemainSolvableWithoutAvoidableOverflow() {
        val equation = SineCosineEqualityEquation(
            sineCoefficient = Double.MAX_VALUE,
            sinePhase = 0.0,
            cosineCoefficient = -Double.MAX_VALUE,
            cosinePhase = 0.0,
        )
        val result = solve(equation)

        assertTrue(periodicFamilies(result).isNotEmpty())
        assertOriginalEquation(equation, result)
    }

    @Test
    fun generatedFamiliesCanBeFilteredToFiniteInterval() {
        val equation = SineCosineEqualityEquation(2.0, 0.0, 1.0, 0.0)
        val families = periodicFamilies(solve(equation))
        val filtered = PeriodicSolutionIntervalFilter.filter(
            families,
            PeriodicSolutionInterval(-2.0 * PI, 2.0 * PI),
        )

        assertTrue(filtered is PeriodicSolutionIntervalResult.FiniteSolutions)
        val values = (filtered as PeriodicSolutionIntervalResult.FiniteSolutions).values
        assertTrue(values.isNotEmpty())
        values.forEach { x ->
            assertTrue(
                abs(
                    sin(Math.fma(equation.sineCoefficient, x, equation.sinePhase)) -
                        cos(Math.fma(equation.cosineCoefficient, x, equation.cosinePhase)),
                ) <= 1e-12,
            )
        }
    }

    private fun solve(equation: SineCosineEqualityEquation) =
        SineCosineEqualityEquationSolver.solve(equation)

    private fun periodicFamilies(
        result: SineCosineEqualityEquationSolutionResult,
    ) = when (result) {
        is SineCosineEqualityEquationSolutionResult.PeriodicSolutions -> result.families
        SineCosineEqualityEquationSolutionResult.AllRealNumbers ->
            error("Expected periodic solutions, got all real numbers")
        SineCosineEqualityEquationSolutionResult.NoSolutions ->
            error("Expected periodic solutions, got no solutions")
        is SineCosineEqualityEquationSolutionResult.Failure ->
            error("Expected periodic solutions, got ${result.error}")
    }

    private fun assertFailure(
        result: SineCosineEqualityEquationSolutionResult,
        expected: PolynomialErrorCode,
    ) {
        assertTrue("Expected failure, got $result", result is SineCosineEqualityEquationSolutionResult.Failure)
        assertEquals(
            expected,
            (result as SineCosineEqualityEquationSolutionResult.Failure).error.code,
        )
    }

    private fun assertOriginalEquation(
        equation: SineCosineEqualityEquation,
        result: SineCosineEqualityEquationSolutionResult,
    ) {
        val families = periodicFamilies(result)
        families.forEach { family ->
            val sineAngle = Math.fma(equation.sineCoefficient, family.firstSolution, equation.sinePhase)
            val cosineAngle = Math.fma(equation.cosineCoefficient, family.firstSolution, equation.cosinePhase)
            assertTrue(sineAngle.isFinite())
            assertTrue(cosineAngle.isFinite())
            assertTrue(abs(sin(sineAngle) - cos(cosineAngle)) <= 1e-12)
        }
    }
}
