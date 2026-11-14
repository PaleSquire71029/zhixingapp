package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineEquation
import ci.us.zhixing.app.domain.math.model.SineEquationSolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class SineEquationSolverTest {
    @Test
    fun sineOfXEqualsZeroReturnsTwoFamilies() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 1.0, phase = 0.0, target = 0.0),
        )

        assertTrue(result is SineEquationSolutionResult.PeriodicSolutions)
        val families = (result as SineEquationSolutionResult.PeriodicSolutions).families
        assertEquals(2, families.size)
        assertEquals(0.0, families[0].firstSolution, 1e-12)
        assertEquals(PI, families[1].firstSolution, 1e-12)
        assertTrue(families.all { assertPeriod(it, 2.0 * PI) })
    }

    @Test
    fun positiveBoundaryTargetReturnsOneFamily() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 1.0, phase = 0.0, target = 1.0),
        )

        assertTrue(result is SineEquationSolutionResult.PeriodicSolutions)
        val family = (result as SineEquationSolutionResult.PeriodicSolutions).families.single()
        assertEquals(PI / 2.0, family.firstSolution, 1e-12)
        assertEquals(2.0 * PI, family.period, 1e-12)
    }

    @Test
    fun negativeBoundaryTargetReturnsOneCanonicalFamily() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 1.0, phase = 0.0, target = -1.0),
        )

        assertTrue(result is SineEquationSolutionResult.PeriodicSolutions)
        val family = (result as SineEquationSolutionResult.PeriodicSolutions).families.single()
        assertEquals(3.0 * PI / 2.0, family.firstSolution, 1e-12)
        assertEquals(2.0 * PI, family.period, 1e-12)
    }

    @Test
    fun linearArgumentAndNonZeroPhaseReturnExpectedFamilies() {
        // sin(2x + pi/6) = 1/2 -> x = 0 or pi/3 (mod pi).
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 2.0, phase = PI / 6.0, target = 0.5),
        )

        assertTrue(result is SineEquationSolutionResult.PeriodicSolutions)
        val families = (result as SineEquationSolutionResult.PeriodicSolutions).families
        assertEquals(2, families.size)
        assertEquals(0.0, families[0].firstSolution, 1e-12)
        assertEquals(PI / 3.0, families[1].firstSolution, 1e-12)
        assertTrue(families.all { assertPeriod(it, PI) })
    }

    @Test
    fun negativeCoefficientStillReturnsPositivePeriods() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = -1.0, phase = 0.0, target = 0.0),
        )

        assertTrue(result is SineEquationSolutionResult.PeriodicSolutions)
        val families = (result as SineEquationSolutionResult.PeriodicSolutions).families
        assertEquals(listOf(0.0, PI), families.map { it.firstSolution })
        assertTrue(families.all { it.period > 0.0 })
    }

    @Test
    fun targetOutsideSineRangeHasNoSolutions() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 1.0, phase = 0.0, target = 1.0001),
        )

        assertTrue(result === SineEquationSolutionResult.NoSolutions)
    }

    @Test
    fun zeroCoefficientIdentityReturnsAllRealNumbers() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 0.0, phase = PI / 6.0, target = 0.5),
        )

        assertTrue(result === SineEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun zeroCoefficientContradictionHasNoSolutions() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 0.0, phase = PI / 6.0, target = 0.75),
        )

        assertTrue(result === SineEquationSolutionResult.NoSolutions)
    }

    @Test
    fun nonFiniteInputReturnsTypedFailure() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = Double.NaN, phase = 0.0, target = 0.0),
        )

        assertTrue(result is SineEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_INPUT,
            (result as SineEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun overflowingPeriodReturnsTypedFailure() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = Double.MIN_VALUE, phase = 0.0, target = 0.0),
        )

        assertTrue(result is SineEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as SineEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun phaseBeyondReliableReductionRangeReturnsPrecisionLoss() {
        val result = SineEquationSolver.solve(
            SineEquation(coefficient = 1.0, phase = 1e18, target = 0.0),
        )

        assertTrue(result is SineEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as SineEquationSolutionResult.Failure).error.code,
        )
    }

    private fun assertPeriod(
        family: PeriodicSolutionFamily,
        expected: Double,
    ): Boolean {
        assertEquals(expected, family.period, 1e-12)
        assertTrue(family.firstSolution >= 0.0)
        assertTrue(family.firstSolution < family.period)
        return true
    }
}
