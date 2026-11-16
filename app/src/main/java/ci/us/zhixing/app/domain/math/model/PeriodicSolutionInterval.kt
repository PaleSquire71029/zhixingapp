package ci.us.zhixing.app.domain.math.model

/**
 * A finite interval used to materialize periodic solution families.
 *
 * Bounds are expressed in the same unit as the trigonometric equation
 * variable (radians for the current sine/cosine/tangent solvers).
 */
internal data class PeriodicSolutionInterval(
    val lowerBound: Double,
    val upperBound: Double,
    val includeLowerBound: Boolean = true,
    val includeUpperBound: Boolean = true,
)

internal enum class PeriodicSolutionIntervalErrorCode {
    NON_FINITE_INPUT,
    INVALID_INTERVAL,
    INVALID_FAMILY,
    INVALID_MAX_SOLUTIONS,
    TOO_MANY_SOLUTIONS,
    NON_FINITE_RESULT,
    NUMERIC_PRECISION_LOSS,
}

internal data class PeriodicSolutionIntervalError(
    val code: PeriodicSolutionIntervalErrorCode,
    val message: String,
)

internal sealed interface PeriodicSolutionIntervalResult {
    data class FiniteSolutions(
        val values: List<Double>,
    ) : PeriodicSolutionIntervalResult

    object NoSolutions : PeriodicSolutionIntervalResult

    data class Failure(
        val error: PeriodicSolutionIntervalError,
    ) : PeriodicSolutionIntervalResult
}
