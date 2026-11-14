package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.CubeRootEquation
import ci.us.zhixing.app.domain.math.model.CubeRootEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CubeRootEquationSolverTest {
    @Test
    fun cubicRadicandAndLinearRightSideReturnTwoOrderedSolutions() {
        // cbrt(x^3 - 1) = x - 1 -> x = 0 or 1
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.of(-1.0, 0.0, 0.0, 1.0),
                right = Polynomial.of(-1.0, 1.0),
            ),
        )

        assertTrue(result is CubeRootEquationSolutionResult.FiniteSolutions)
        val solutions = (result as CubeRootEquationSolutionResult.FiniteSolutions).solutions
        assertEquals(listOf(0.0, 1.0), solutions.map { it.value })
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun negativeRadicandCanProduceAValidNegativeSolution() {
        // cbrt(x) = -2 -> x = -8; cube-root equations have no square-root sign restriction.
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.variable(),
                right = Polynomial.constant(-2.0),
            ),
        )

        assertTrue(result is CubeRootEquationSolutionResult.FiniteSolutions)
        val solution = (result as CubeRootEquationSolutionResult.FiniteSolutions).solutions.single()
        assertEquals(-8.0, solution.value, 0.0)
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun equationIdentityReturnsAllRealNumbers() {
        // cbrt(8x^3) = 2x for every real x.
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.of(0.0, 0.0, 0.0, 8.0),
                right = Polynomial.of(0.0, 2.0),
            ),
        )

        assertTrue(result === CubeRootEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun negativeConstantIdentityIsNotRestrictedToNonNegativeValues() {
        // cbrt(-8) = -2 for every real x.
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.constant(-8.0),
                right = Polynomial.constant(-2.0),
            ),
        )

        assertTrue(result === CubeRootEquationSolutionResult.AllRealNumbers)
    }

    @Test
    fun constantContradictionHasNoSolutions() {
        // cbrt(8) = 1 is false for every x.
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.constant(8.0),
                right = Polynomial.constant(1.0),
            ),
        )

        assertTrue(result === CubeRootEquationSolutionResult.NoSolutions)
    }

    @Test
    fun equationWithNoRealRootsReturnsNoSolutions() {
        // cbrt(x^3 + 1) = x -> x^3 + 1 = x^3 + 0, a contradiction.
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.of(1.0, 0.0, 0.0, 1.0),
                right = Polynomial.variable(),
            ),
        )

        assertTrue(result === CubeRootEquationSolutionResult.NoSolutions)
    }

    @Test
    fun quadraticRadicandCanHaveTwoSolutions() {
        // cbrt(x^2) = x -> x^2 = x^3 -> x = 0 or 1.
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.of(0.0, 0.0, 1.0),
                right = Polynomial.variable(),
            ),
        )

        assertTrue(result is CubeRootEquationSolutionResult.FiniteSolutions)
        val solutions = (result as CubeRootEquationSolutionResult.FiniteSolutions).solutions
        assertEquals(listOf(0.0, 1.0), solutions.map { it.value })
    }

    @Test
    fun quadraticAndLinearStructureLimitsAreExplicit() {
        val highRadicand = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.of(0.0, 0.0, 0.0, 0.0, 1.0),
                right = Polynomial.variable(),
            ),
        )
        val highRight = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.variable(),
                right = Polynomial.of(0.0, 0.0, 1.0),
            ),
        )

        assertEquals(
            CubeRootEquationSolutionResult.UnsupportedStructure(4, 1),
            highRadicand,
        )
        assertEquals(
            CubeRootEquationSolutionResult.UnsupportedStructure(1, 2),
            highRight,
        )
    }

    @Test
    fun cubingOverflowReturnsTypedNumericFailure() {
        val result = CubeRootEquationSolver.solve(
            CubeRootEquation(
                radicand = Polynomial.constant(1.0),
                right = Polynomial.constant(Double.MAX_VALUE),
            ),
        )

        assertTrue(result is CubeRootEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as CubeRootEquationSolutionResult.Failure).error.code,
        )
    }
}
