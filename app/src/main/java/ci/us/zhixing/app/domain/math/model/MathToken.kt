package ci.us.zhixing.app.domain.math.model

internal enum class MathTokenKind(val goldenLabel: String) {
    NUMBER("num"),
    VARIABLE_X("x"),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    POWER("^"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    PERCENT("%");

    companion object {
        fun fromSymbol(symbol: Char): MathTokenKind? = when (symbol) {
            'x' -> VARIABLE_X
            '+' -> PLUS
            '-' -> MINUS
            '*' -> MULTIPLY
            '/' -> DIVIDE
            '^' -> POWER
            '(' -> LEFT_PAREN
            ')' -> RIGHT_PAREN
            '%' -> PERCENT
            else -> null
        }
    }
}

internal data class MathToken(
    val kind: MathTokenKind,
    val lexeme: String,
    val numericValue: Double? = null,
    /** Inclusive index of the first source UTF-16 code unit. */
    val startOffset: Int,
    /** Exclusive index in the original source string. */
    val endOffset: Int,
) {
    fun toGoldenLabel(): String = when (kind) {
        MathTokenKind.NUMBER -> "${kind.goldenLabel}:${numericValue}"
        else -> kind.goldenLabel
    }
}

internal enum class MathTokenizerErrorCode {
    INVALID_NUMBER,
    UNKNOWN_CHARACTER,
}

internal data class MathTokenizerError(
    val code: MathTokenizerErrorCode,
    val message: String,
    val offset: Int,
    val lexeme: String,
)

internal sealed interface TokenizationResult {
    data class Success(val tokens: List<MathToken>) : TokenizationResult
    data class Failure(val error: MathTokenizerError) : TokenizationResult
}
