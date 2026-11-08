package ci.us.zhixing.app.domain.math.model

internal enum class MathEvaluationErrorCode {
    VARIABLE_NOT_ALLOWED,
    DIVISION_BY_ZERO,
    NON_FINITE_RESULT,
}

internal data class MathEvaluationError(
    val code: MathEvaluationErrorCode,
    val message: String,
    val offset: Int,
)

internal sealed interface MathEvaluationResult {
    data class Success(val value: Double) : MathEvaluationResult
    data class Failure(val error: MathEvaluationError) : MathEvaluationResult
}

internal enum class MathCalculationStage {
    TOKENIZER,
    PARSER,
    EVALUATOR,
}

internal data class MathCalculationError(
    val stage: MathCalculationStage,
    val message: String,
    val offset: Int,
    val lexeme: String = "",
)

internal sealed interface MathCalculationResult {
    data class Success(val value: Double) : MathCalculationResult
    data class Failure(val error: MathCalculationError) : MathCalculationResult
}
