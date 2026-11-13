package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicEquation
import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicErrorCode
import ci.us.zhixing.app.domain.math.model.Polynomial
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExponentialLogarithmicEquationSolverTest {
    @Test
    fun exponentialEquationWithSameBaseReducesTheExponents() {
        // 2^(2x + 1) = 2^(x + 3) -> x = 2
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialExpressionsEqual(
                leftBase = 2.0,
                leftExponent = Polynomial.of(1.0, 2.0),
                rightBase = 2.0,
                rightExponent = Polynomial.of(3.0, 1.0),
            ),
        )

        assertSingleSolution(result, 2.0)
    }

    @Test
    fun constantExponentialIdentityAndContradictionAreClassifiedDirectly() {
        val identity = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 2.0,
                exponent = Polynomial.constant(3.0),
                value = 8.0,
            ),
        )
        val contradiction = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 2.0,
                exponent = Polynomial.constant(3.0),
                value = 9.0,
            ),
        )

        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(emptyList()),
            identity,
        )
        assertTrue(contradiction === ExponentialLogarithmicEquationSolutionResult.NoSolutions)
    }

    @Test
    fun exponentialEquationUsesChangeOfBaseForConstantRightSide() {
        // 2^x = 8 -> x = log_2(8) = 3
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 2.0,
                exponent = Polynomial.variable(),
                value = 8.0,
            ),
        )

        assertSingleSolution(result, 3.0)
    }

    @Test
    fun exponentialEquationSupportsBasesBelowOne() {
        // (1/2)^x = 8 -> x = -3
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 0.5,
                exponent = Polynomial.variable(),
                value = 8.0,
            ),
        )

        assertSingleSolution(result, -3.0)
    }

    @Test
    fun unitBaseIsHandledAsAConstantExpression() {
        val identity = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 1.0,
                exponent = Polynomial.variable(),
                value = 1.0,
            ),
        )
        val contradiction = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 1.0,
                exponent = Polynomial.variable(),
                value = 2.0,
            ),
        )

        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(emptyList()),
            identity,
        )
        assertTrue(contradiction === ExponentialLogarithmicEquationSolutionResult.NoSolutions)
    }

    @Test
    fun equalExpressionsWithDifferentBasesCanHaveOneSolution() {
        // 2^x = 4^(x - 1) -> x = 2
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialExpressionsEqual(
                leftBase = 2.0,
                leftExponent = Polynomial.variable(),
                rightBase = 4.0,
                rightExponent = Polynomial.of(-1.0, 1.0),
            ),
        )

        assertSingleSolution(result, 2.0)
    }

    @Test
    fun bothUnitExponentialBasesProduceAnIdentity() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialExpressionsEqual(
                leftBase = 1.0,
                leftExponent = Polynomial.of(5.0, 2.0),
                rightBase = 1.0,
                rightExponent = Polynomial.of(-3.0, 7.0),
            ),
        )

        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(emptyList()),
            result,
        )
    }

    @Test
    fun logarithmEqualsConstantSolvesItsPositiveArgument() {
        // log_2(x + 1) = 3 -> x = 7
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 2.0,
                argument = Polynomial.of(1.0, 1.0),
                value = 3.0,
            ),
        )

        assertSingleSolution(result, 7.0)
    }

    @Test
    fun logarithmSupportsBaseBetweenZeroAndOne() {
        // log_(1/2)(x) = -2 -> x = 4
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 0.5,
                argument = Polynomial.variable(),
                value = -2.0,
            ),
        )

        assertSingleSolution(result, 4.0)
    }

    @Test
    fun constantLogarithmIdentityDoesNotLoseExactIntegerPowersToExpRounding() {
        val identity = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 2.0,
                argument = Polynomial.constant(8.0),
                value = 3.0,
            ),
        )
        val contradiction = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 2.0,
                argument = Polynomial.constant(8.0),
                value = 2.0,
            ),
        )

        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(emptyList()),
            identity,
        )
        assertTrue(contradiction === ExponentialLogarithmicEquationSolutionResult.NoSolutions)
    }

    @Test
    fun invalidLogarithmBaseIsRejected() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 1.0,
                argument = Polynomial.variable(),
                value = 2.0,
            ),
        )

        assertFailure(result, ExponentialLogarithmicErrorCode.INVALID_BASE)
    }

    @Test
    fun logarithmTargetOutsideDoublePositiveRangeIsAtypedPrecisionFailure() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 2.0,
                argument = Polynomial.variable(),
                value = 2000.0,
            ),
        )

        assertFailure(result, ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun sameBaseLogarithmsReduceToTheirArgumentsAndCheckBothDomains() {
        // log_2(x + 1) = log_2(3x - 3) -> x = 2; both arguments are positive.
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.SameBaseLogarithmsEqual(
                base = 2.0,
                leftArgument = Polynomial.of(1.0, 1.0),
                rightArgument = Polynomial.of(-3.0, 3.0),
            ),
        )

        assertSingleSolution(result, 2.0)
    }

    @Test
    fun exponentialChangeOfBaseHandlesDifferentBases() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 2.0,
                exponent = Polynomial.variable(),
                value = 16.0,
            ),
        )
        assertSingleSolution(result, 4.0)
    }

    @Test
    fun sameBaseLogarithmsAlsoWorkWhenTheBaseIsBetweenZeroAndOne() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.SameBaseLogarithmsEqual(
                base = 0.5,
                leftArgument = Polynomial.of(1.0, 1.0),
                rightArgument = Polynomial.of(-3.0, 3.0),
            ),
        )

        assertSingleSolution(result, 2.0)
    }

    @Test
    fun logarithmTargetTooCloseToOneIsReportedAsPrecisionLoss() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 1.0000000000000002,
                argument = Polynomial.variable(),
                value = 0.25,
            ),
        )

        assertFailure(result, ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS)
    }

    @Test
    fun logarithmCandidatesAtZeroArgumentsAreRejected() {
        // log_2(x) = log_2(-x) has the algebraic candidate x=0, outside both domains.
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.SameBaseLogarithmsEqual(
                base = 2.0,
                leftArgument = Polynomial.variable(),
                rightArgument = Polynomial.of(0.0, -1.0),
            ),
        )

        assertTrue(result === ExponentialLogarithmicEquationSolutionResult.NoSolutions)
    }

    @Test
    fun logarithmIdentityRetainsStrictlyPositiveDomainConditions() {
        // log_2(x + 1) = log_2(x + 1) is true only for x > -1.
        val argument = Polynomial.of(1.0, 1.0)
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.SameBaseLogarithmsEqual(
                base = 2.0,
                leftArgument = argument,
                rightArgument = argument,
            ),
        )

        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(listOf(argument)),
            result,
        )
    }

    @Test
    fun logarithmIdentityWithNonPositiveConstantArgumentHasNoSolutions() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.SameBaseLogarithmsEqual(
                base = 10.0,
                leftArgument = Polynomial.constant(-1.0),
                rightArgument = Polynomial.constant(-1.0),
            ),
        )

        assertTrue(result === ExponentialLogarithmicEquationSolutionResult.NoSolutions)
    }

    @Test
    fun higherDegreeExponentsAndLogarithmArgumentsAreExplicitlyUnsupported() {
        val exponential = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 2.0,
                exponent = Polynomial.of(0.0, 0.0, 1.0),
                value = 4.0,
            ),
        )
        val logarithm = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.LogarithmEqualsConstant(
                base = 2.0,
                argument = Polynomial.of(0.0, 0.0, 1.0),
                value = 2.0,
            ),
        )

        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.UnsupportedStructure(2, 0),
            exponential,
        )
        assertEquals(
            ExponentialLogarithmicEquationSolutionResult.UnsupportedStructure(2, 0),
            logarithm,
        )
    }

    @Test
    fun nonPositiveExponentialBaseIsRejected() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = -2.0,
                exponent = Polynomial.variable(),
                value = 8.0,
            ),
        )

        assertFailure(result, ExponentialLogarithmicErrorCode.INVALID_BASE)
    }

    @Test
    fun nonPositiveExponentialRightSideIsRejected() {
        val result = ExponentialLogarithmicEquationSolver.solve(
            ExponentialLogarithmicEquation.ExponentialEqualsConstant(
                base = 2.0,
                exponent = Polynomial.variable(),
                value = 0.0,
            ),
        )

        assertFailure(result, ExponentialLogarithmicErrorCode.INVALID_LOGARITHM_VALUE)
    }

    private fun assertSingleSolution(
        result: ExponentialLogarithmicEquationSolutionResult,
        expected: Double,
    ) {
        assertTrue(result is ExponentialLogarithmicEquationSolutionResult.FiniteSolutions)
        val solution = (result as ExponentialLogarithmicEquationSolutionResult.FiniteSolutions)
            .solutions.single()
        assertEquals(expected, solution.value, 1e-11)
        assertTrue(solution.normalizedResidual <= 1e-12)
    }

    private fun assertFailure(
        result: ExponentialLogarithmicEquationSolutionResult,
        expectedCode: ExponentialLogarithmicErrorCode,
    ) {
        assertTrue(result is ExponentialLogarithmicEquationSolutionResult.Failure)
        assertEquals(
            expectedCode,
            (result as ExponentialLogarithmicEquationSolutionResult.Failure).error.code,
        )
    }
}
