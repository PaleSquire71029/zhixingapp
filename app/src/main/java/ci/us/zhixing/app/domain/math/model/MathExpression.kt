package ci.us.zhixing.app.domain.math.model

/** Half-open offsets in the original input string (UTF-16 code units). */
internal data class MathSourceSpan(
    val startOffset: Int,
    val endOffset: Int,
) {
    init {
        require(startOffset >= 0)
        require(endOffset >= startOffset)
    }
}

internal sealed interface MathExpression {
    val span: MathSourceSpan
}

internal data class NumberExpression(
    val value: Double,
    val lexeme: String,
    override val span: MathSourceSpan,
) : MathExpression

internal data class VariableExpression(
    override val span: MathSourceSpan,
) : MathExpression

internal enum class UnaryMathOperator {
    PLUS,
    MINUS,
}

internal data class UnaryExpression(
    val operator: UnaryMathOperator,
    val operand: MathExpression,
    override val span: MathSourceSpan,
) : MathExpression

internal enum class BinaryMathOperator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    POWER,
}

internal data class BinaryExpression(
    val operator: BinaryMathOperator,
    val left: MathExpression,
    val right: MathExpression,
    /** True only when multiplication was inferred from adjacent factors. */
    val isImplicitMultiplication: Boolean = false,
    override val span: MathSourceSpan,
) : MathExpression

internal data class PercentExpression(
    val operand: MathExpression,
    override val span: MathSourceSpan,
) : MathExpression

/** Explicit parentheses are retained so later formatters can preserve grouping. */
internal data class GroupExpression(
    val expression: MathExpression,
    override val span: MathSourceSpan,
) : MathExpression

internal enum class MathParserErrorCode {
    EMPTY_EXPRESSION,
    INCOMPLETE_EXPRESSION,
    UNMATCHED_LEFT_PARENTHESIS,
    EXTRA_RIGHT_PARENTHESIS,
    UNEXPECTED_TOKEN,
    TRAILING_TOKENS,
    INVALID_EXPONENT,
    EXPONENT_TOO_LARGE,
}

internal data class MathParserError(
    val code: MathParserErrorCode,
    val message: String,
    val offset: Int,
    val lexeme: String = "",
)

internal sealed interface ExpressionParseResult {
    data class Success(val expression: MathExpression) : ExpressionParseResult
    data class Failure(val error: MathParserError) : ExpressionParseResult
}
