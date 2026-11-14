package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.AbsoluteValueEquationWithLinearRightSide
import ci.us.zhixing.app.domain.math.model.AbsoluteValueLinearRightEquationResult
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AbsoluteValueLinearRightEquationSolverTest {
    @Test
    fun bothBranchesProduceTwoValidSolutions() {
        // |2x - 1| = x + 2 -> x = -1/3 or 3.
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.of(-1.0, 2.0),
                right = Polynomial.of(2.0, 1.0),
            ),
        )

        assertTrue(result is AbsoluteValueLinearRightEquationResult.FiniteSolutions)
        val solutions = (result as AbsoluteValueLinearRightEquationResult.FiniteSolutions).solutions
        assertEquals(listOf(-1.0 / 3.0, 3.0), solutions.map { it.value })
        assertTrue(solutions.all { it.normalizedResidual <= 1e-12 })
    }

    @Test
    fun candidateWithNegativeRightSideIsRejected() {
        // |x| = x - 1 has an algebraic candidate x=1/2, but the right side is negative.
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.variable(),
                right = Polynomial.of(-1.0, 1.0),
            ),
        )

        assertTrue(result === AbsoluteValueLinearRightEquationResult.NoSolutions)
    }

    @Test
    fun positiveIdentityBranchPreservesRightSideDomain() {
        // |x| = x exactly for x >= 0.
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.variable(),
                right = Polynomial.variable(),
            ),
        )

        assertTrue(result is AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain)
        val conditions =
            (result as AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain)
                .nonNegativeConditions
        assertEquals(1, conditions.size)
        assertEquals(1, PolynomialArithmetic.signAt(conditions.single(), 1.0))
        assertEquals(-1, PolynomialArithmetic.signAt(conditions.single(), -1.0))
        assertEquals(0, PolynomialArithmetic.signAt(conditions.single(), 0.0))
    }

    @Test
    fun negativeIdentityBranchPreservesRightSideDomain() {
        // |x| = -x exactly for x <= 0.
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.variable(),
                right = Polynomial.of(0.0, -1.0),
            ),
        )

        assertTrue(result is AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain)
        val condition =
            (result as AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain)
                .nonNegativeConditions.single()
        assertEquals(1, PolynomialArithmetic.signAt(condition, -1.0))
        assertEquals(-1, PolynomialArithmetic.signAt(condition, 1.0))
    }

    @Test
    fun zeroIdentityReturnsAllRealNumbers() {
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.constant(0.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertEquals(
            AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain(emptyList()),
            result,
        )
    }

    @Test
    fun positiveConstantRightSideReturnsTwoSolutions() {
        // |x - 1| = 2 -> x = -1, 3.
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.of(-1.0, 1.0),
                right = Polynomial.constant(2.0),
            ),
        )

        assertTrue(result is AbsoluteValueLinearRightEquationResult.FiniteSolutions)
        val solutions = (result as AbsoluteValueLinearRightEquationResult.FiniteSolutions).solutions
        assertEquals(listOf(-1.0, 3.0), solutions.map { it.value })
    }

    @Test
    fun zeroRightSideDeduplicatesRootsFromBothBranches() {
        // |x² - 1| = 0 -> x = -1, 1.
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.of(-1.0, 0.0, 1.0),
                right = Polynomial.constant(0.0),
            ),
        )

        assertTrue(result is AbsoluteValueLinearRightEquationResult.FiniteSolutions)
        val solutions = (result as AbsoluteValueLinearRightEquationResult.FiniteSolutions).solutions
        assertEquals(listOf(-1.0, 1.0), solutions.map { it.value })
    }

    @Test
    fun negativeConstantRightSideHasNoSolutions() {
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.variable(),
                right = Polynomial.constant(-1.0),
            ),
        )

        assertTrue(result === AbsoluteValueLinearRightEquationResult.NoSolutions)
    }

    @Test
    fun unsupportedDegreesAreExplicit() {
        val highExpression = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.of(0.0, 0.0, 0.0, 1.0),
                right = Polynomial.variable(),
            ),
        )
        val highRight = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.variable(),
                right = Polynomial.of(0.0, 0.0, 1.0),
            ),
        )

        assertEquals(
            AbsoluteValueLinearRightEquationResult.UnsupportedStructure(3, 1),
            highExpression,
        )
        assertEquals(
            AbsoluteValueLinearRightEquationResult.UnsupportedStructure(1, 2),
            highRight,
        )
    }

    @Test
    fun nonFiniteBranchOperationReturnsTypedFailure() {
        val result = AbsoluteValueLinearRightEquationSolver.solve(
            AbsoluteValueEquationWithLinearRightSide(
                expression = Polynomial.constant(Double.MAX_VALUE),
                right = Polynomial.constant(-Double.MAX_VALUE),
            ),
        )

        assertTrue(result is AbsoluteValueLinearRightEquationResult.Failure)
        assertEquals(
            PolynomialErrorCode.NON_FINITE_RESULT,
            (result as AbsoluteValueLinearRightEquationResult.Failure).error.code,
        )
    }
}
