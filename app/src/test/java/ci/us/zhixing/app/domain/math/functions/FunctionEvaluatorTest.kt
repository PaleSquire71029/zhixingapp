package ci.us.zhixing.app.domain.math.functions

import ci.us.zhixing.app.domain.math.model.CubicFunction
import ci.us.zhixing.app.domain.math.model.FittedFunction
import ci.us.zhixing.app.domain.math.model.FunctionEvaluationErrorCode
import ci.us.zhixing.app.domain.math.model.FunctionEvaluationInputError
import ci.us.zhixing.app.domain.math.model.FunctionEvaluationResult
import ci.us.zhixing.app.domain.math.model.InverseProportionFunction
import ci.us.zhixing.app.domain.math.model.LinearFunction
import ci.us.zhixing.app.domain.math.model.QuadraticFunction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FunctionEvaluatorTest {
    @Test
    fun linearFunctionIsEvaluated() {
        assertEvaluation(
            FittedFunction.Linear(LinearFunction(2.0, 3.0)),
            x = 4.0,
            expectedY = 11.0,
        )
    }

    @Test
    fun quadraticFunctionUsesHornerEvaluation() {
        assertEvaluation(
            FittedFunction.Quadratic(QuadraticFunction(2.0, -3.0, 4.0)),
            x = 2.0,
            expectedY = 6.0,
        )
    }

    @Test
    fun cubicFunctionUsesHornerEvaluation() {
        assertEvaluation(
            FittedFunction.Cubic(CubicFunction(1.0, -2.0, 0.5, 1.0)),
            x = 3.0,
            expectedY = 11.5,
        )
    }

    @Test
    fun inverseProportionFunctionIsEvaluatedAwayFromZero() {
        assertEvaluation(
            FittedFunction.InverseProportion(InverseProportionFunction(12.0)),
            x = -3.0,
            expectedY = -4.0,
        )
    }

    @Test
    fun everyFunctionRejectsNonFiniteX() {
        val function = FittedFunction.Linear(LinearFunction(1.0, 0.0))
        assertEquals(
            FunctionEvaluationResult.InvalidInput(FunctionEvaluationInputError.NON_FINITE_X),
            FunctionEvaluator.evaluate(function, Double.NaN),
        )
        assertEquals(
            FunctionEvaluationResult.InvalidInput(FunctionEvaluationInputError.NON_FINITE_X),
            FunctionEvaluator.evaluate(function, Double.POSITIVE_INFINITY),
        )
    }

    @Test
    fun inverseProportionExcludesZeroEvenWhenItsConstantIsZero() {
        val function = FittedFunction.InverseProportion(InverseProportionFunction(0.0))
        assertEquals(
            FunctionEvaluationResult.OutsideDomain(0.0),
            FunctionEvaluator.evaluate(function, -0.0),
        )
    }

    @Test
    fun nonFiniteCoefficientsReturnTypedFailure() {
        val result = FunctionEvaluator.evaluate(
            FittedFunction.Cubic(CubicFunction(Double.NaN, 0.0, 0.0, 0.0)),
            1.0,
        )

        assertEquals(
            FunctionEvaluationErrorCode.NON_FINITE_COEFFICIENT,
            (result as FunctionEvaluationResult.Failure).code,
        )
    }

    @Test
    fun polynomialOverflowReturnsTypedNonFiniteResult() {
        val result = FunctionEvaluator.evaluate(
            FittedFunction.Cubic(CubicFunction(1.0, 0.0, 0.0, 0.0)),
            1e200,
        )

        assertEquals(
            FunctionEvaluationErrorCode.NON_FINITE_RESULT,
            (result as FunctionEvaluationResult.Failure).code,
        )
    }

    private fun assertEvaluation(
        function: FittedFunction,
        x: Double,
        expectedY: Double,
    ) {
        val result = FunctionEvaluator.evaluate(function, x)
        assertTrue("Expected success but got $result", result is FunctionEvaluationResult.Success)
        assertEquals(expectedY, (result as FunctionEvaluationResult.Success).y, 1e-10)
    }
}
