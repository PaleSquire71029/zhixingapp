package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.CosineEquation
import ci.us.zhixing.app.domain.math.model.CosineEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class CosineEquationSolverTest {
    @Test
    fun cosineOfXEqualsZeroReturnsTwoFamilies() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 1.0, phase = 0.0, target = 0.0),
        )

        assertTrue(result is CosineEquationSolutionResult.PeriodicSolutions)
        val families = (result as CosineEquationSolutionResult.PeriodicSolutions).families
        assertEquals(2, families.size)
        assertEquals(PI / 2.0, families[0].firstSolution, 1e-12)
        assertEquals(3.0 * PI / 2.0, families[1].firstSolution, 1e-12)
        assertTrue(families.all { assertPeriod(it, 2.0 * PI) })
    }

    @Test
    fun positiveBoundaryTargetReturnsOneFamily() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 1.0, phase = 0.0, target = 1.0),
        )

        assertTrue(result is CosineEquationSolutionResult.PeriodicSolutions)
        val family = (result as CosineEquationSolutionResult.PeriodicSolutions).families.single()
        assertEquals(0.0, family.firstSolution, 1e-12)
        assertEquals(2.0 * PI, family.period, 1e-12)
    }

    @Test
    fun negativeBoundaryTargetReturnsOneFamily() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 1.0, phase = 0.0, target = -1.0),
        )

        assertTrue(result is CosineEquationSolutionResult.PeriodicSolutions)
        val family = (result as CosineEquationSolutionResult.PeriodicSolutions).families.single()
        assertEquals(PI, family.firstSolution, 1e-12)
        assertEquals(2.0 * PI, family.period, 1e-12)
    }

    @Test
    fun linearArgumentAndNonZeroPhaseReturnExpectedFamilies() {
        // cos(2x + pi/3) = 1/2 -> x = 0 or 2pi/3 (mod pi).
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 2.0, phase = PI / 3.0, target = 0.5),
        )

        assertTrue(result is CosineEquationSolutionResult.PeriodicSolutions)
        val families = (result as CosineEquationSolutionResult.PeriodicSolutions).families
        assertEquals(2, families.size)
        assertEquals(0.0, families[0].firstSolution, 1e-12)
        assertEquals(2.0 * PI / 3.0, families[1].firstSolution, 1e-12)
        assertTrue(families.all { assertPeriod(it, PI) })
    }

    @Test
    fun negativeCoefficientStillReturnsPositivePeriods() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = -1.0, phase = 0.0, target = 0.0),
        )

        assertTrue(result is CosineEquationSolutionResult.PeriodicSolutions)
        val families = (result as CosineEquationSolutionResult.PeriodicSolutions).families
        assertEquals(listOf(PI / 2.0, 3.0 * PI / 2.0), families.map { it.firstSolution })
        assertTrue(families.all { it.period > 0.0 })
    }

    @Test
    fun targetOutsideCosineRangeHasNoSolutions() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 1.0, phase = 0.0, target = -1.1),
        )

        assertTrue(result === CosineEquationSolutionResult.NoSolutions)
    }

    @Test
    fun zeroCoefficientIdentityReturnsAllRealNumbers() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 0.0, phase = PI / 3.0, target = 0.5),
        )

        assertTrue(result === CosineEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun zeroCoefficientContradictionHasNoSolutions() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 0.0, phase = PI / 3.0, target = 0.75),
        )

        assertTrue(result === CosineEquationSolutionResult.NoSolutions)
    }

    @Test
    fun nonFiniteInputReturnsTypedFailure() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 1.0, phase = 0.0, target = Double.NaN),
        )

        assertTrue(result is CosineEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_INPUT,
            (result as CosineEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun overflowingPeriodReturnsTypedFailure() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = Double.MIN_VALUE, phase = 0.0, target = 0.0),
        )

        assertTrue(result is CosineEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as CosineEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun phaseBeyondReliableReductionRangeReturnsPrecisionLoss() {
        val result = CosineEquationSolver.solve(
            CosineEquation(coefficient = 1.0, phase = 1e18, target = 0.0),
        )

        assertTrue(result is CosineEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as CosineEquationSolutionResult.Failure).error.code,
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
