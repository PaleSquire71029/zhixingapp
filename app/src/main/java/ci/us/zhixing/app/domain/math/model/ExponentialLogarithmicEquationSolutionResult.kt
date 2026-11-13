package ci.us.zhixing.app.domain.math.model

internal enum class ExponentialLogarithmicErrorCode {
    NON_FINITE_INPUT,
    INVALID_BASE,
    INVALID_LOGARITHM_VALUE,
    NON_FINITE_RESULT,
    NUMERIC_PRECISION_LOSS,
}

internal data class ExponentialLogarithmicError(
    val code: ExponentialLogarithmicErrorCode,
    val message: String,
)

/** Result for the supported exponential / logarithmic equation forms. */
internal sealed interface ExponentialLogarithmicEquationSolutionResult {
    /**
     * All real x satisfying every listed strictly-positive polynomial
     * condition. An empty list means all real numbers.
     */
    data class AllRealNumbersOnDomain(
        val positiveConditions: List<Polynomial>,
    ) : ExponentialLogarithmicEquationSolutionResult

    object NoSolutions : ExponentialLogarithmicEquationSolutionResult

    data class FiniteSolutions(
        val solutions: List<EquationSolution>,
    ) : ExponentialLogarithmicEquationSolutionResult

    data class UnsupportedStructure(
        val leftDegree: Int,
        val rightDegree: Int,
    ) : ExponentialLogarithmicEquationSolutionResult

    data class Failure(
        val error: ExponentialLogarithmicError,
    ) : ExponentialLogarithmicEquationSolutionResult
}
