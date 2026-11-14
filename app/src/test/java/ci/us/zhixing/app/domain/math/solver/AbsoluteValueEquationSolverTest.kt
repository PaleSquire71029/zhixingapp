package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.AbsoluteValueEquation
import ci.us.zhixing.app.domain.math.model.EquationSolutionResult
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AbsoluteValueEquationSolverTest {
    @Test
    fun linearAbsoluteValueEquationReturnsTwoOrderedSolutions() {
        // |2x - 1| = 3 -> x = -1, 2
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(-1.0, 2.0),
                target = 3.0,
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(listOf(-1.0, 2.0), solutions.map { it.value })
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun zeroTargetProducesOneSolutionRatherThanDuplicateBranches() {
        // |2x + 1| = 0 -> x = -1/2
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(1.0, 2.0),
                target = 0.0,
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(1, solutions.size)
        assertEquals(-0.5, solutions.single().value, 0.0)
    }

    @Test
    fun negativeTargetHasNoSolutions() {
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(1.0, 1.0),
                target = -2.0,
            ),
        )

        assertTrue(result === EquationSolutionResult.NoSolutions)
    }

    @Test
    fun constantEquationCanBeAnIdentityOrContradiction() {
        val identity = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(Polynomial.constant(-4.0), 4.0),
        )
        val contradiction = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(Polynomial.constant(4.0), 3.0),
        )

        assertTrue(identity === EquationSolutionResult.AllRealNumbers)
        assertTrue(contradiction === EquationSolutionResult.NoSolutions)
    }

    @Test
    fun quadraticInnerExpressionReturnsBothRealSolutions() {
        // |x^2 - 1| = 3 -> x^2 = 4 -> x = -2, 2
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(-1.0, 0.0, 1.0),
                target = 3.0,
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(listOf(-2.0, 2.0), solutions.map { it.value })
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun quadraticAbsoluteValueEquationCombinesRootsFromBothBranches() {
        // |x^2 - 1| = 1 -> x = -sqrt(2), 0, sqrt(2)
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(-1.0, 0.0, 1.0),
                target = 1.0,
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(3, solutions.size)
        assertEquals(-kotlin.math.sqrt(2.0), solutions[0].value, 1e-12)
        assertEquals(0.0, solutions[1].value, 1e-12)
        assertEquals(kotlin.math.sqrt(2.0), solutions[2].value, 1e-12)
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun quadraticZeroTargetReturnsItsRootsOnce() {
        // |x^2 - 1| = 0 -> x = -1, 1
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(-1.0, 0.0, 1.0),
                target = 0.0,
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(listOf(-1.0, 1.0), solutions.map { it.value })
    }

    @Test
    fun cubicInnerExpressionRemainsExplicitlyUnsupported() {
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.of(-1.0, 0.0, 0.0, 1.0),
                target = 2.0,
            ),
        )

        assertEquals(EquationSolutionResult.UnsupportedDegree(3), result)
    }

    @Test
    fun nonFiniteTargetRemainsATypedInputError() {
        val result = AbsoluteValueEquationSolver.solve(
            AbsoluteValueEquation(
                expression = Polynomial.variable(),
                target = Double.NaN,
            ),
        )

        assertTrue(result is EquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_INPUT,
            (result as EquationSolutionResult.Failure).error.code,
        )
    }
}
