package ci.us.zhixing.app.domain.math.model

/** Input errors specific to the domain of an inverse-proportion function. */
internal sealed interface InverseProportionFitInputError {
    data class PointSetValidation(
        val reason: CoordinatePointSetValidationResult,
    ) : InverseProportionFitInputError

    data class ZeroXCoordinate(
        val pointIndex: Int,
        val x: Double,
    ) : InverseProportionFitInputError
}

/** Result of fitting y = k / x to a coordinate point set. */
internal sealed interface InverseProportionFunctionFitResult {
    data class Success(
        val function: InverseProportionFunction,
        val pointCount: Int,
    ) : InverseProportionFunctionFitResult

    data class InvalidInput(
        val reason: InverseProportionFitInputError,
    ) : InverseProportionFunctionFitResult

    data class Failure(
        val error: FunctionFitError,
    ) : InverseProportionFunctionFitResult
}
