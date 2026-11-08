package ci.us.zhixing.app.domain.math.evaluator

import ci.us.zhixing.app.domain.math.model.BinaryExpression
import ci.us.zhixing.app.domain.math.model.BinaryMathOperator
import ci.us.zhixing.app.domain.math.model.GroupExpression
import ci.us.zhixing.app.domain.math.model.MathEvaluationError
import ci.us.zhixing.app.domain.math.model.MathEvaluationErrorCode
import ci.us.zhixing.app.domain.math.model.MathEvaluationResult
import ci.us.zhixing.app.domain.math.model.MathExpression
import ci.us.zhixing.app.domain.math.model.NumberExpression
import ci.us.zhixing.app.domain.math.model.PercentExpression
import ci.us.zhixing.app.domain.math.model.UnaryExpression
import ci.us.zhixing.app.domain.math.model.UnaryMathOperator
import ci.us.zhixing.app.domain.math.model.VariableExpression
import kotlin.math.pow

/**
 * Evaluates constant arithmetic ASTs with the legacy calculator's Double semantics.
 * Formatting and display rounding belong to a later presentation layer.
 */
internal object MathExpressionEvaluator {
    fun evaluate(expression: MathExpression): MathEvaluationResult =
        evaluateNode(expression)

    private fun evaluateNode(expression: MathExpression): MathEvaluationResult =
        when (expression) {
            is NumberExpression -> finiteResult(expression.value, expression.span.startOffset)

            is VariableExpression -> failure(
                MathEvaluationErrorCode.VARIABLE_NOT_ALLOWED,
                "含 x 的式子请用「方程求解」",
                expression.span.startOffset,
            )

            is GroupExpression -> evaluateNode(expression.expression)

            is UnaryExpression -> when (val operand = evaluateNode(expression.operand)) {
                is MathEvaluationResult.Failure -> operand
                is MathEvaluationResult.Success -> {
                    val value = when (expression.operator) {
                        UnaryMathOperator.PLUS -> operand.value
                        UnaryMathOperator.MINUS -> -operand.value
                    }
                    finiteResult(value, expression.span.startOffset)
                }
            }

            is PercentExpression -> when (val operand = evaluateNode(expression.operand)) {
                is MathEvaluationResult.Failure -> operand
                is MathEvaluationResult.Success ->
                    finiteResult(operand.value * PERCENT_SCALE, expression.span.startOffset)
            }

            is BinaryExpression -> evaluateBinary(expression)
        }

    private fun evaluateBinary(expression: BinaryExpression): MathEvaluationResult {
        val left = when (val result = evaluateNode(expression.left)) {
            is MathEvaluationResult.Failure -> return result
            is MathEvaluationResult.Success -> result.value
        }
        val right = when (val result = evaluateNode(expression.right)) {
            is MathEvaluationResult.Failure -> return result
            is MathEvaluationResult.Success -> result.value
        }

        if (expression.operator == BinaryMathOperator.DIVIDE && right == 0.0) {
            return failure(
                MathEvaluationErrorCode.DIVISION_BY_ZERO,
                "除数不能为 0",
                expression.right.span.startOffset,
            )
        }

        val value = when (expression.operator) {
            BinaryMathOperator.ADD -> left + right
            BinaryMathOperator.SUBTRACT -> left - right
            BinaryMathOperator.MULTIPLY -> left * right
            BinaryMathOperator.DIVIDE -> left / right
            BinaryMathOperator.POWER -> left.pow(right)
        }
        return finiteResult(value, expression.span.startOffset)
    }

    private fun finiteResult(value: Double, offset: Int): MathEvaluationResult =
        if (value.isFinite()) {
            MathEvaluationResult.Success(value)
        } else {
            failure(
                MathEvaluationErrorCode.NON_FINITE_RESULT,
                "结果不是有限数",
                offset,
            )
        }

    private fun failure(
        code: MathEvaluationErrorCode,
        message: String,
        offset: Int,
    ) = MathEvaluationResult.Failure(
        MathEvaluationError(code = code, message = message, offset = offset),
    )

    private const val PERCENT_SCALE = 0.01
}
