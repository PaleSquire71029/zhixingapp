package ci.us.zhixing.app.ui.calculator

import ci.us.zhixing.app.domain.math.model.MathCalculationStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorPresenterTest {
    @Test
    fun presentsBasicArithmeticThroughTheExistingDomainEntryPoint() {
        val result = CalculatorPresenter.present("2*(3+4)")

        assertTrue(result is CalculatorPresentationState.Success)
        result as CalculatorPresentationState.Success
        assertEquals(14.0, result.value, 0.0)
        assertEquals("14", result.displayText)
    }

    @Test
    fun blankAndWhitespaceExpressionsAreEmptyNotZero() {
        assertEquals(CalculatorPresentationState.Empty, CalculatorPresenter.present(""))
        assertEquals(CalculatorPresentationState.Empty, CalculatorPresenter.present("   "))
    }

    @Test
    fun incompleteExpressionPreservesParserFailureAndSourceOffset() {
        val result = CalculatorPresenter.present("2+")

        assertTrue(result is CalculatorPresentationState.Failure)
        result as CalculatorPresentationState.Failure
        assertEquals(MathCalculationStage.PARSER, result.error.stage)
        assertEquals(2, result.error.offset)
        assertEquals("表达式不完整", result.error.message)
    }

    @Test
    fun divisionByZeroPreservesEvaluatorFailure() {
        val result = CalculatorPresenter.present("1/0")

        assertTrue(result is CalculatorPresentationState.Failure)
        result as CalculatorPresentationState.Failure
        assertEquals(MathCalculationStage.EVALUATOR, result.error.stage)
        assertEquals("除数不能为 0", result.error.message)
    }

    @Test
    fun variableExpressionPreservesDomainGuidanceInsteadOfReturningZero() {
        val result = CalculatorPresenter.present("2x+1")

        assertTrue(result is CalculatorPresentationState.Failure)
        result as CalculatorPresentationState.Failure
        assertEquals(MathCalculationStage.EVALUATOR, result.error.stage)
        assertEquals("含 x 的式子请用「方程求解」", result.error.message)
        assertEquals(1, result.error.offset)
    }

    @Test
    fun formatterDoesNotRoundFiniteResultsAndNormalizesZero() {
        assertEquals("0", CalculatorNumberFormatter.format(-0.0))
        assertEquals("0.5", CalculatorNumberFormatter.format(0.5))
        assertEquals("1.2345678901234567", CalculatorNumberFormatter.format(1.2345678901234567))
        assertEquals("1e+20", CalculatorNumberFormatter.format(1e20))
        assertEquals("未定义", CalculatorNumberFormatter.format(Double.POSITIVE_INFINITY))
    }
}
