package ci.us.zhixing.app.ui.calculator

import ci.us.zhixing.app.domain.math.evaluator.MathArithmeticCalculator
import ci.us.zhixing.app.domain.math.model.MathCalculationError
import ci.us.zhixing.app.domain.math.model.MathCalculationResult
import java.math.BigDecimal

internal sealed interface CalculatorPresentationState {
    data object Empty : CalculatorPresentationState

    data class Success(
        val value: Double,
        val displayText: String,
    ) : CalculatorPresentationState

    data class Failure(
        val error: MathCalculationError,
    ) : CalculatorPresentationState
}

/**
 * Presentation adapter for standard arithmetic.
 *
 * All parsing and evaluation is delegated to the existing pure-Kotlin
 * MathArithmeticCalculator entry point; this layer only maps its result and
 * formats a finite value for display.
 */
internal object CalculatorPresenter {
    fun present(expression: String): CalculatorPresentationState {
        if (expression.isBlank()) return CalculatorPresentationState.Empty

        return when (val result = MathArithmeticCalculator.calculate(expression)) {
            is MathCalculationResult.Success -> CalculatorPresentationState.Success(
                value = result.value,
                displayText = CalculatorNumberFormatter.format(result.value),
            )

            is MathCalculationResult.Failure -> CalculatorPresentationState.Failure(
                error = result.error,
            )
        }
    }
}

/** Locale-independent, non-rounding display of the evaluator's Double value. */
internal object CalculatorNumberFormatter {
    fun format(value: Double): String {
        if (!value.isFinite()) return "未定义"
        if (value == 0.0) return "0"
        return BigDecimal.valueOf(value)
            .stripTrailingZeros()
            .toString()
            .replace('E', 'e')
    }
}
