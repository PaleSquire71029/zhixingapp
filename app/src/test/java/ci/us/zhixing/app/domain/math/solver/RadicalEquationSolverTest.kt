package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.RadicalEquation
import ci.us.zhixing.app.domain.math.model.RadicalEquationSolutionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadicalEquationSolverTest {
    @Test
    fun rejectsExtraneousRootAfterSquaring() {
        // sqrt(x) = x - 1 -> squared candidates (3 ± √5)/2; the smaller is extraneous.
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.variable(),
                right = Polynomial.of(-1.0, 1.0),
            ),
        )

        assertTrue(result is RadicalEquationSolutionResult.FiniteSolutions)
        val solutions = (result as RadicalEquationSolutionResult.FiniteSolutions).solutions
        assertEquals(1, solutions.size)
        assertEquals((3.0 + kotlin.math.sqrt(5.0)) / 2.0, solutions.single().value, 1e-12)
        assertTrue(solutions.single().normalizedResidual <= 1e-12)
    }

    @Test
    fun verifiesTheOriginalEquationForAQuadraticRadicand() {
        // sqrt(x + 1) = x - 1 -> squared candidates 0 and 3; x=0 is extraneous.
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.of(1.0, 1.0),
                right = Polynomial.of(-1.0, 1.0),
            ),
        )

        assertTrue(result is RadicalEquationSolutionResult.FiniteSolutions)
        val solutions = (result as RadicalEquationSolutionResult.FiniteSolutions).solutions
        assertEquals(1, solutions.size)
        assertEquals(3.0, solutions.single().value, 1e-12)
    }

    @Test
    fun negativeRightSideRejectsSquaredCandidate() {
        // sqrt(x² + 1) = x - 1 squares to 2x = 0, but x=0 has a negative right side.
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.of(1.0, 0.0, 1.0),
                right = Polynomial.of(-1.0, 1.0),
            ),
        )

        assertTrue(result === RadicalEquationSolutionResult.NoSolutions)
    }

    @Test
    fun identityPreservesTheNonNegativeDomainAsACondition() {
        // sqrt(x²) = x is true exactly when x >= 0.
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.of(0.0, 0.0, 1.0),
                right = Polynomial.variable(),
            ),
        )

        assertTrue(result is RadicalEquationSolutionResult.AllRealNumbersOnDomain)
        val conditions =
            (result as RadicalEquationSolutionResult.AllRealNumbersOnDomain).nonNegativeConditions
        assertEquals(2, conditions.size)
        val radicandCondition = conditions.first { it.degree == 2 }
        val rightCondition = conditions.first { it.degree == 1 }
        assertEquals(1, PolynomialArithmetic.signAt(radicandCondition, 1.0))
        assertEquals(1, PolynomialArithmetic.signAt(radicandCondition, -1.0))
        assertEquals(1, PolynomialArithmetic.signAt(rightCondition, 1.0))
        assertEquals(-1, PolynomialArithmetic.signAt(rightCondition, -1.0))
    }

    @Test
    fun constantIdentityWithNegativeRightSideHasNoSolutions() {
        // sqrt(4) = -2 is never true, despite equality after squaring.
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.constant(4.0),
                right = Polynomial.constant(-2.0),
            ),
        )

        assertTrue(result === RadicalEquationSolutionResult.NoSolutions)
    }

    @Test
    fun zeroRadicandAndZeroRightSideReturnsTheBoundaryRootOnce() {
        // sqrt(x²) = 0 -> x = 0
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.of(0.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is RadicalEquationSolutionResult.FiniteSolutions)
        val solutions = (result as RadicalEquationSolutionResult.FiniteSolutions).solutions
        assertEquals(1, solutions.size)
        assertEquals(0.0, solutions.single().value, 0.0)
    }

    @Test
    fun identityWithoutDomainRestrictionsRepresentsAllRealNumbers() {
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.constant(4.0),
                right = Polynomial.constant(2.0),
            ),
        )

        assertEquals(
            RadicalEquationSolutionResult.AllRealNumbersOnDomain(emptyList()),
            result,
        )
    }

    @Test
    fun unsupportedDegreesAreExplicit() {
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.of(0.0, 0.0, 0.0, 1.0),
                right = Polynomial.variable(),
            ),
        )

        assertEquals(
            RadicalEquationSolutionResult.UnsupportedStructure(
                radicandDegree = 3,
                rightDegree = 1,
            ),
            result,
        )
    }

    @Test
    fun squaringOverflowRemainsATypedFailure() {
        val result = RadicalEquationSolver.solve(
            RadicalEquation(
                radicand = Polynomial.constant(1.0),
                right = Polynomial.of(Double.MAX_VALUE, 1.0),
            ),
        )

        assertTrue(result is RadicalEquationSolutionResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as RadicalEquationSolutionResult.Failure).error.code,
        )
    }
}
