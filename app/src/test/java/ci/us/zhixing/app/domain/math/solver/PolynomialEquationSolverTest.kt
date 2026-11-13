package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.EquationSolutionResult
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialEquation
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolynomialEquationSolverTest {
    @Test
    fun identicalConstantSidesAreAnIdentity() {
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.constant(3.0),
                right = Polynomial.constant(3.0),
            ),
        )

        assertTrue(result === EquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun differentConstantSidesHaveNoSolution() {
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.constant(5.0),
                right = Polynomial.constant(3.0),
            ),
        )

        assertTrue(result === EquationSolutionResult.NoSolutions)
    }

    @Test
    fun solvesLinearEquationByMovingRightSideToLeft() {
        // 2x + 3 = 7  ->  2x - 4 = 0  ->  x = 2
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(3.0, 2.0),
                right = Polynomial.constant(7.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solution = (result as EquationSolutionResult.FiniteSolutions).solutions.single()
        assertEquals(2.0, solution.value, 0.0)
        assertTrue(solution.normalizedResidual.isFinite())
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun reducesVariablesFromBothSidesBeforeSolving() {
        // x + 1 = 3x - 3  ->  4 - 2x = 0  ->  x = 2
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, 1.0),
                right = Polynomial.of(-3.0, 3.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        assertEquals(
            2.0,
            (result as EquationSolutionResult.FiniteSolutions).solutions.single().value,
            0.0,
        )
    }

    @Test
    fun linearEquationCanHaveZeroAsItsSolution() {
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(0.0, 4.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        assertEquals(
            0.0,
            (result as EquationSolutionResult.FiniteSolutions).solutions.single().value,
            0.0,
        )
    }

    @Test
    fun higherDegreeEquationsAreExplicitlyDeferredToLaterSlices() {
        val quadratic = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(-1.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )
        val cubic = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(0.0, -1.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertEquals(EquationSolutionResult.UnsupportedDegree(2), quadratic)
        assertEquals(EquationSolutionResult.UnsupportedDegree(3), cubic)
    }

    @Test
    fun nonFiniteReductionFailureIsPreservedAsATypedError() {
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.constant(Double.MAX_VALUE),
                right = Polynomial.constant(-Double.MAX_VALUE),
            ),
        )

        assertTrue(result is EquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as EquationSolutionResult.Failure).error.code,
        )
    }
}
