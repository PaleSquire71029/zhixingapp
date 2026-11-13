package ci.us.zhixing.app.domain.math.model

/** Result of fitting y = ax^2 + bx + c to a coordinate point set. */
internal sealed interface QuadraticFunctionFitResult {
    data class Success(
        val function: QuadraticFunction,
        val pointCount: Int,
    ) : QuadraticFunctionFitResult

    data class InvalidInput(
        val reason: CoordinatePointSetValidationResult,
    ) : QuadraticFunctionFitResult

    data class Failure(
        val error: FunctionFitError,
    ) : QuadraticFunctionFitResult
}
