package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.TangentEquation
import ci.us.zhixing.app.domain.math.model.TangentEquationSolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.tan

class TangentEquationSolverTest {
    @Test
    fun tangentOfXEqualsZeroReturnsOneFamily() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 1.0, phase = 0.0, target = 0.0),
        )

        assertTrue(result is TangentEquationSolutionResult.PeriodicSolutions)
        val family = (result as TangentEquationSolutionResult.PeriodicSolutions).family
        assertEquals(0.0, family.firstSolution, 1e-12)
        assertPeriod(family, PI)
    }

    @Test
    fun positiveTargetReturnsArctangentBaseAndPiPeriod() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 1.0, phase = 0.0, target = 1.0),
        )

        assertTrue(result is TangentEquationSolutionResult.PeriodicSolutions)
        val family = (result as TangentEquationSolutionResult.PeriodicSolutions).family
        assertEquals(PI / 4.0, family.firstSolution, 1e-12)
        assertPeriod(family, PI)
    }

    @Test
    fun negativeTargetReturnsCanonicalBase() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 1.0, phase = 0.0, target = -1.0),
        )

        assertTrue(result is TangentEquationSolutionResult.PeriodicSolutions)
        val family = (result as TangentEquationSolutionResult.PeriodicSolutions).family
        assertEquals(3.0 * PI / 4.0, family.firstSolution, 1e-12)
        assertPeriod(family, PI)
    }

    @Test
    fun linearArgumentAndPhaseOffsetAreSupported() {
        // tan(2x + pi/4) = 1 -> x = k*pi/2.
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 2.0, phase = PI / 4.0, target = 1.0),
        )

        assertTrue(result is TangentEquationSolutionResult.PeriodicSolutions)
        val family = (result as TangentEquationSolutionResult.PeriodicSolutions).family
        assertEquals(0.0, family.firstSolution, 1e-12)
        assertPeriod(family, PI / 2.0)
    }

    @Test
    fun negativeCoefficientStillReturnsPositivePeriod() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = -1.0, phase = 0.0, target = 1.0),
        )

        assertTrue(result is TangentEquationSolutionResult.PeriodicSolutions)
        val family = (result as TangentEquationSolutionResult.PeriodicSolutions).family
        assertEquals(3.0 * PI / 4.0, family.firstSolution, 1e-12)
        assertPeriod(family, PI)
    }

    @Test
    fun zeroCoefficientIdentityReturnsAllRealNumbers() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 0.0, phase = PI / 4.0, target = 1.0),
        )

        assertTrue(result === TangentEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun zeroCoefficientContradictionHasNoSolutions() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 0.0, phase = PI / 4.0, target = 0.0),
        )

        assertTrue(result === TangentEquationSolutionResult.NoSolutions)
    }

    @Test
    fun zeroCoefficientAtTangentPoleHasNoSolutions() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 0.0, phase = PI / 2.0, target = 0.0),
        )

        assertTrue(result === TangentEquationSolutionResult.NoSolutions)
    }

    @Test
    fun angleImmediatelyNextToPoleRemainsAValidFiniteConstant() {
        val angle = Math.nextDown(PI / 2.0)
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 0.0, phase = angle, target = tan(angle)),
        )

        assertTrue(result === TangentEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun nonFiniteInputReturnsTypedFailure() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 1.0, phase = Double.NaN, target = 0.0),
        )

        assertTrue(result is TangentEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_INPUT,
            (result as TangentEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun overflowingPeriodReturnsTypedFailure() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = Double.MIN_VALUE, phase = 0.0, target = 0.0),
        )

        assertTrue(result is TangentEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as TangentEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun phaseBeyondReliableReductionRangeReturnsPrecisionLoss() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 1.0, phase = 1e18, target = 0.0),
        )

        assertTrue(result is TangentEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as TangentEquationSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun veryLargeFiniteTargetIsRejectedWhenBaseAngleCannotPassResidualCheck() {
        val result = TangentEquationSolver.solve(
            TangentEquation(coefficient = 1.0, phase = 0.0, target = Double.MAX_VALUE),
        )

        assertTrue(result is TangentEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as TangentEquationSolutionResult.Failure).error.code,
        )
    }

    private fun assertPeriod(
        family: PeriodicSolutionFamily,
        expected: Double,
    ) {
        assertEquals(expected, family.period, 1e-12)
        assertTrue(family.period > 0.0)
        assertTrue(family.firstSolution >= 0.0)
        assertTrue(family.firstSolution < family.period)
    }
}
