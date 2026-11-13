package ci.us.zhixing.app.domain.math.model

internal enum class FunctionEvaluationInputError {
    NON_FINITE_X,
}

internal enum class FunctionEvaluationErrorCode {
    NON_FINITE_COEFFICIENT,
    NON_FINITE_RESULT,
}

/** Typed result of evaluating a supported fitted function at one x value. */
internal sealed interface FunctionEvaluationResult {
    data class Success(
        val y: Double,
    ) : FunctionEvaluationResult

    data class InvalidInput(
        val reason: FunctionEvaluationInputError,
    ) : FunctionEvaluationResult

    data class OutsideDomain(
        val excludedX: Double,
    ) : FunctionEvaluationResult

    data class Failure(
        val code: FunctionEvaluationErrorCode,
        val x: Double,
    ) : FunctionEvaluationResult
}
