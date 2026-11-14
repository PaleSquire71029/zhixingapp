package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.LinearEquation2D
import ci.us.zhixing.app.domain.math.model.LinearEquationSystem
import ci.us.zhixing.app.domain.math.model.LinearSystemSolutionResult
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinearEquationSystemSolverTest {
    @Test
    fun independentEquationsReturnUniqueSolution() {
        // 2x + y = 5; x - y = 1 -> (x, y) = (2, 1)
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(2.0, 1.0, 5.0),
                second = LinearEquation2D(1.0, -1.0, 1.0),
            ),
        )

        assertTrue(result is LinearSystemSolutionResult.UniqueSolution)
        val solution = result as LinearSystemSolutionResult.UniqueSolution
        assertEquals(2.0, solution.x, 1e-12)
        assertEquals(1.0, solution.y, 1e-12)
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun negativeCoefficientsReturnUniqueSolution() {
        // -x + 2y = 3; 3x + y = 5 -> (x, y) = (1, 2)
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(-1.0, 2.0, 3.0),
                second = LinearEquation2D(3.0, 1.0, 5.0),
            ),
        )

        assertTrue(result is LinearSystemSolutionResult.UniqueSolution)
        val solution = result as LinearSystemSolutionResult.UniqueSolution
        assertEquals(1.0, solution.x, 1e-12)
        assertEquals(2.0, solution.y, 1e-12)
    }

    @Test
    fun proportionalEquationsHaveInfinitelyManySolutions() {
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(1.0, 2.0, 3.0),
                second = LinearEquation2D(2.0, 4.0, 6.0),
            ),
        )

        assertTrue(result === LinearSystemSolutionResult.AllRealPairs)
    }

    @Test
    fun proportionalLeftSidesWithDifferentConstantsHaveNoSolution() {
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(1.0, 2.0, 3.0),
                second = LinearEquation2D(2.0, 4.0, 7.0),
            ),
        )

        assertTrue(result === LinearSystemSolutionResult.NoSolutions)
    }

    @Test
    fun zeroEquationTogetherWithOneLinearEquationHasInfinitelyManySolutions() {
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(0.0, 0.0, 0.0),
                second = LinearEquation2D(1.0, -1.0, 1.0),
            ),
        )

        assertTrue(result === LinearSystemSolutionResult.AllRealPairs)
    }

    @Test
    fun twoZeroCoefficientRowsAreClassifiedByTheirConstants() {
        val identity = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(0.0, 0.0, 0.0),
                second = LinearEquation2D(0.0, 0.0, 0.0),
            ),
        )
        val contradiction = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(0.0, 0.0, 1.0),
                second = LinearEquation2D(0.0, 0.0, 0.0),
            ),
        )

        assertTrue(identity === LinearSystemSolutionResult.AllRealPairs)
        assertTrue(contradiction === LinearSystemSolutionResult.NoSolutions)
    }

    @Test
    fun nearSingularButIndependentEquationsRemainUnique() {
        // x + y = 3; x + (1 + 1e-12)y = 3 + 1e-12 -> (2, 1)
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(1.0, 1.0, 3.0),
                second = LinearEquation2D(1.0, 1.0 + 1e-12, 3.0 + 1e-12),
            ),
        )

        assertTrue(result is LinearSystemSolutionResult.UniqueSolution)
        val solution = result as LinearSystemSolutionResult.UniqueSolution
        assertEquals(2.0, solution.x, 1e-9)
        assertEquals(1.0, solution.y, 1e-9)
    }

    @Test
    fun nonFiniteCoefficientReturnsTypedInputError() {
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(Double.NaN, 1.0, 2.0),
                second = LinearEquation2D(1.0, 1.0, 3.0),
            ),
        )

        assertTrue(result is LinearSystemSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_INPUT,
            (result as LinearSystemSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun solutionOverflowReturnsTypedNonFiniteError() {
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(Double.MIN_VALUE, 0.0, 1.0),
                second = LinearEquation2D(0.0, 1.0, 0.0),
            ),
        )

        assertTrue(result is LinearSystemSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as LinearSystemSolutionResult.Failure).error.code,
        )
    }

    @Test
    fun nonZeroSolutionUnderflowReturnsPrecisionLoss() {
        val result = LinearEquationSystemSolver.solve(
            LinearEquationSystem(
                first = LinearEquation2D(Double.MAX_VALUE, 0.0, Double.MIN_VALUE),
                second = LinearEquation2D(0.0, 1.0, 0.0),
            ),
        )

        assertTrue(result is LinearSystemSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
            (result as LinearSystemSolutionResult.Failure).error.code,
        )
    }
}
