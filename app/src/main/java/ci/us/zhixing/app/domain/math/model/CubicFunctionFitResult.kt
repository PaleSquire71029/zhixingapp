package ci.us.zhixing.app.domain.math.model

/** Result of fitting y = ax^3 + bx^2 + cx + d to coordinate points. */
internal sealed interface CubicFunctionFitResult {
    data class Success(
        val function: CubicFunction,
        val pointCount: Int,
    ) : CubicFunctionFitResult

    data class InvalidInput(
        val reason: CoordinatePointSetValidationResult,
    ) : CubicFunctionFitResult

    data class Failure(
        val error: FunctionFitError,
    ) : CubicFunctionFitResult
}
