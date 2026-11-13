package ci.us.zhixing.app.domain.math.model

internal enum class FunctionFitErrorCode {
    NON_FINITE_RESULT,
    NUMERIC_PRECISION_LOSS,
}

internal data class FunctionFitError(
    val code: FunctionFitErrorCode,
    val message: String,
)

/** Result of fitting y = mx + b to a validated coordinate point set. */
internal sealed interface LinearFunctionFitResult {
    data class Success(
        val function: LinearFunction,
        val pointCount: Int,
    ) : LinearFunctionFitResult

    data class InvalidInput(
        val reason: CoordinatePointSetValidationResult,
    ) : LinearFunctionFitResult

    data class Failure(
        val error: FunctionFitError,
    ) : LinearFunctionFitResult
}
