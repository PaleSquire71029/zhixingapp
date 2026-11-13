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
    fun cubicEquationWithThreeDistinctRealRootsReturnsAllRootsInOrder() {
        // x³ - 6x² + 11x - 6 = (x - 1)(x - 2)(x - 3)
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(-6.0, 11.0, -6.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(3, solutions.size)
        listOf(1.0, 2.0, 3.0).forEachIndexed { index, expected ->
            assertEquals(expected, solutions[index].value, 1e-12)
        }
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun cubicEquationWithOneRealRootUsesStableCardanoBranch() {
        // x³ + 1 = 0
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, 0.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solution = (result as EquationSolutionResult.FiniteSolutions).solutions.single()
        assertEquals(-1.0, solution.value, 1e-12)
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun cubicEquationWithDoubleRootReturnsTwoDistinctSolutions() {
        // x³ - 3x + 2 = (x - 1)²(x + 2)
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(2.0, -3.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(2, solutions.size)
        assertEquals(-2.0, solutions[0].value, 1e-12)
        assertEquals(1.0, solutions[1].value, 1e-12)
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun cubicEquationWithTripleRootReturnsOneSolution() {
        // (x - 1)³ = 0
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(-1.0, 3.0, -3.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(1, solutions.size)
        assertEquals(1.0, solutions.single().value, 1e-12)
        assertTrue(solutions.single().normalizedResidual <= 1e-12)
    }

    @Test
    fun cubicCardanoCancellationStillRecoversSmallRoot() {
        // x³ + x + 1e-10 = 0 has a root close to -1e-10.
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1e-10, 1.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solution = (result as EquationSolutionResult.FiniteSolutions).solutions.single()
        assertEquals(-1e-10, solution.value, 1e-22)
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun cubicEquationCanHaveARepresentableLargeRoot() {
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(-Double.MAX_VALUE, 0.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solution = (result as EquationSolutionResult.FiniteSolutions).solutions.single()
        assertTrue(solution.value > 1e102)
        assertTrue(solution.value.isFinite())
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun quarticEquationRemainsExplicitlyUnsupported() {
        val quartic = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, 0.0, 0.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertEquals(EquationSolutionResult.UnsupportedDegree(4), quartic)
    }


    @Test
    fun solvesQuadraticEquationWithTwoDistinctRealSolutions() {
        // x² - 3x + 2 = 0
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(2.0, -3.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(listOf(1.0, 2.0), solutions.map { it.value })
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun quadraticEquationWithRepeatedRootReturnsOneSolution() {
        // (x - 1)² = 0
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, -2.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(1, solutions.size)
        assertEquals(1.0, solutions.single().value, 0.0)
        assertTrue(solutions.single().normalizedResidual <= 1e-12)
    }

    @Test
    fun quadraticEquationWithoutRealRootsHasNoSolutions() {
        // x² + 1 = 0
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result === EquationSolutionResult.NoSolutions)
    }

    @Test
    fun quadraticEquationPreservesStableSmallRootFromRootEngine() {
        // x² - 1e16*x + 1 = 0
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, -1e16, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.FiniteSolutions)
        val solutions = (result as EquationSolutionResult.FiniteSolutions).solutions
        assertEquals(2, solutions.size)
        assertTrue(kotlin.math.abs(solutions[0].value - 1e-16) <= 1e-28)
        assertEquals(1e16, solutions[1].value, 2.0)
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun quadraticEquationMapsRootEngineFailuresWithoutChangingErrorCodes() {
        val result = PolynomialEquationSolver.solve(
            PolynomialEquation(
                left = Polynomial.of(1.0, Double.MAX_VALUE, Polynomial.ZERO_TOLERANCE),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is EquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as EquationSolutionResult.Failure).error.code,
        )
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
