package ci.us.zhixing.app.domain.math.model

internal enum class PolynomialErrorCode {
    NON_FINITE_RESULT,
    NON_FINITE_INPUT,
    NUMERIC_PRECISION_LOSS,
}

internal data class PolynomialError(
    val code: PolynomialErrorCode,
    val message: String,
)

internal sealed interface PolynomialOperationResult {
    data class Success(val polynomial: Polynomial) : PolynomialOperationResult
    data class Failure(val error: PolynomialError) : PolynomialOperationResult
}

internal sealed interface PolynomialEvaluationResult {
    data class Success(val value: Double) : PolynomialEvaluationResult
    data class Failure(val error: PolynomialError) : PolynomialEvaluationResult
}
