package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.RationalEquation
import ci.us.zhixing.app.domain.math.model.RationalEquationError
import ci.us.zhixing.app.domain.math.model.RationalEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.RationalFunction
import ci.us.zhixing.app.domain.math.model.RationalFunctionOperationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RationalEquationSolverTest {
    @Test
    fun solvesOneOverXEqualsTwo() {
        // 1/x = 2 -> x = 1/2
        val result = RationalEquationSolver.solve(
            RationalEquation(
                left = rational(Polynomial.constant(1.0), Polynomial.variable()),
                right = rational(Polynomial.constant(2.0), Polynomial.constant(1.0)),
            ),
        )

        assertTrue(result is RationalEquationSolutionResult.FiniteSolutions)
        val solution = (result as RationalEquationSolutionResult.FiniteSolutions).solutions.single()
        assertEquals(0.5, solution.value, 1e-12)
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    @Test
    fun rationalIdentityKeepsTheOriginalDenominatorHole() {
        // x/x = 1 is true for every x except x = 0.
        val result = RationalEquationSolver.solve(
            RationalEquation(
                left = rational(Polynomial.variable(), Polynomial.variable()),
                right = rational(Polynomial.constant(1.0), Polynomial.constant(1.0)),
            ),
        )

        assertTrue(result is RationalEquationSolutionResult.AllRealNumbersInDomain)
        val restrictions =
            (result as RationalEquationSolutionResult.AllRealNumbersInDomain).domainRestrictions
        assertTrue(restrictions.isNotEmpty())
        assertTrue(restrictions.any { PolynomialArithmetic.isExactlyZeroAt(it, 0.0) })
    }

    @Test
    fun candidateRootAtAnOriginalDenominatorZeroIsRemoved() {
        // x/x = 0 produces the algebraic candidate x = 0, but x = 0 is undefined.
        val result = RationalEquationSolver.solve(
            RationalEquation(
                left = rational(Polynomial.variable(), Polynomial.variable()),
                right = rational(Polynomial.constant(0.0), Polynomial.constant(1.0)),
            ),
        )

        assertTrue(result === RationalEquationSolutionResult.NoSolutions)
    }

    @Test
    fun denominatorRestrictionsDoNotRemoveAValidNonzeroRoot() {
        // 1/x = 1 has the valid solution x = 1; only x = 0 is excluded.
        val result = RationalEquationSolver.solve(
            RationalEquation(
                left = rational(Polynomial.constant(1.0), Polynomial.variable()),
                right = rational(Polynomial.constant(1.0), Polynomial.constant(1.0)),
            ),
        )

        assertTrue(result is RationalEquationSolutionResult.FiniteSolutions)
        assertEquals(
            1.0,
            (result as RationalEquationSolutionResult.FiniteSolutions).solutions.single().value,
            1e-12,
        )
    }

    @Test
    fun quarticRationalEquationIsExplicitlyUnsupportedForNow() {
        val result = RationalEquationSolver.solve(
            RationalEquation(
                left = rational(
                    Polynomial.of(1.0, 0.0, 0.0, 0.0, 1.0),
                    Polynomial.constant(1.0),
                ),
                right = rational(Polynomial.constant(0.0), Polynomial.constant(1.0)),
            ),
        )

        assertEquals(RationalEquationSolutionResult.UnsupportedDegree(4), result)
    }

    @Test
    fun rationalReductionFailureRemainsTyped() {
        val result = RationalEquationSolver.solve(
            RationalEquation(
                left = rational(
                    Polynomial.constant(Double.MAX_VALUE),
                    Polynomial.constant(1.0),
                ),
                right = rational(
                    Polynomial.constant(-Double.MAX_VALUE),
                    Polynomial.constant(1.0),
                ),
            ),
        )

        assertTrue(result is RationalEquationSolutionResult.Failure)
        val error = (result as RationalEquationSolutionResult.Failure).error
        assertTrue(error is RationalEquationError.RationalFunctionOperation)
    }

    private fun rational(numerator: Polynomial, denominator: Polynomial) =
        (RationalFunction.create(numerator, denominator) as RationalFunctionOperationResult.Success)
            .value
}
