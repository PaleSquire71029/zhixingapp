package ci.us.zhixing.app.domain.math.evaluator

import ci.us.zhixing.app.domain.math.golden.CalculatorGoldenCases
import ci.us.zhixing.app.domain.math.model.BinaryExpression
import ci.us.zhixing.app.domain.math.model.BinaryMathOperator
import ci.us.zhixing.app.domain.math.model.MathCalculationResult
import ci.us.zhixing.app.domain.math.model.MathCalculationStage
import ci.us.zhixing.app.domain.math.model.MathEvaluationErrorCode
import ci.us.zhixing.app.domain.math.model.MathEvaluationResult
import ci.us.zhixing.app.domain.math.model.MathSourceSpan
import ci.us.zhixing.app.domain.math.model.NumberExpression
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MathExpressionEvaluatorTest {
    @Test
    fun evaluatesEverySuccessfulArithmeticGoldenCase() {
        CalculatorGoldenCases.arithmetic
            .filter { it.expectedValue != null }
            .forEach { golden ->
                val result = MathArithmeticCalculator.calculate(golden.input)
                assertTrue("Expected a value for " + golden.id, result is MathCalculationResult.Success)
                val actual = (result as MathCalculationResult.Success).value
                val expected = checkNotNull(golden.expectedValue)
                val tolerance = maxOf(ABSOLUTE_TOLERANCE, abs(expected) * RELATIVE_TOLERANCE)
                assertEquals(golden.id, expected, actual, tolerance)
            }
    }

    @Test
    fun returnsEveryArithmeticGoldenErrorWithoutChangingItsMessage() {
        CalculatorGoldenCases.arithmetic
            .filter { it.expectedError != null }
            .forEach { golden ->
                val result = MathArithmeticCalculator.calculate(golden.input)
                assertTrue("Expected an error for " + golden.id, result is MathCalculationResult.Failure)
                assertEquals(
                    golden.id,
                    golden.expectedError,
                    (result as MathCalculationResult.Failure).error.message,
                )
            }
    }

    @Test
    fun tokenizationAndParsingErrorsKeepTheirStageAndSourceOffset() {
        val tokenError = MathArithmeticCalculator.calculate("2@3")
        assertTrue(tokenError is MathCalculationResult.Failure)
        val tokenFailure = (tokenError as MathCalculationResult.Failure).error
        assertEquals(MathCalculationStage.TOKENIZER, tokenFailure.stage)
        assertEquals(1, tokenFailure.offset)
        assertEquals("@", tokenFailure.lexeme)

        val parseError = MathArithmeticCalculator.calculate("2+")
        assertTrue(parseError is MathCalculationResult.Failure)
        val parseFailure = (parseError as MathCalculationResult.Failure).error
        assertEquals(MathCalculationStage.PARSER, parseFailure.stage)
        assertEquals(2, parseFailure.offset)
    }

    @Test
    fun divisionByPositiveOrNegativeZeroIsRejected() {
        listOf("1/0", "1/(-0)").forEach { input ->
            val result = MathArithmeticCalculator.calculate(input)
            assertTrue("Expected division by zero for " + input, result is MathCalculationResult.Failure)
            val error = (result as MathCalculationResult.Failure).error
            assertEquals(MathCalculationStage.EVALUATOR, error.stage)
            assertEquals("除数不能为 0", error.message)
        }
    }

    @Test
    fun overflowAndNonFiniteLiteralAreReportedInsteadOfReturned() {
        val span = MathSourceSpan(0, 1)
        val maximum = NumberExpression(Double.MAX_VALUE, "max", span)
        val overflow = BinaryExpression(
            operator = BinaryMathOperator.MULTIPLY,
            left = maximum,
            right = maximum,
            span = span,
        )
        val overflowResult = MathExpressionEvaluator.evaluate(overflow)
        assertTrue(overflowResult is MathEvaluationResult.Failure)
        assertEquals(
            MathEvaluationErrorCode.NON_FINITE_RESULT,
            (overflowResult as MathEvaluationResult.Failure).error.code,
        )

        val infiniteResult = MathExpressionEvaluator.evaluate(
            NumberExpression(Double.POSITIVE_INFINITY, "infinity", span),
        )
        assertTrue(infiniteResult is MathEvaluationResult.Failure)
        assertEquals(
            MathEvaluationErrorCode.NON_FINITE_RESULT,
            (infiniteResult as MathEvaluationResult.Failure).error.code,
        )
    }

    @Test
    fun percentScalingAndZeroPowerZeroFollowLegacyArithmeticRules() {
        assertValue("50%%", 0.005)
        assertValue("0^0", 1.0)
        assertValue("+3", 3.0)
    }

    @Test
    fun variableErrorsPointToTheVariableRatherThanReturningAPartialValue() {
        val result = MathArithmeticCalculator.calculate("2x+1")
        assertTrue(result is MathCalculationResult.Failure)
        val error = (result as MathCalculationResult.Failure).error
        assertEquals(MathCalculationStage.EVALUATOR, error.stage)
        assertEquals("含 x 的式子请用「方程求解」", error.message)
        assertEquals(1, error.offset)
    }

    private fun assertValue(input: String, expected: Double) {
        val result = MathArithmeticCalculator.calculate(input)
        assertTrue("Expected a value for " + input, result is MathCalculationResult.Success)
        assertEquals(
            input,
            expected,
            (result as MathCalculationResult.Success).value,
            ABSOLUTE_TOLERANCE,
        )
    }

    private companion object {
        const val ABSOLUTE_TOLERANCE = 1e-10
        const val RELATIVE_TOLERANCE = 1e-10
    }
}
