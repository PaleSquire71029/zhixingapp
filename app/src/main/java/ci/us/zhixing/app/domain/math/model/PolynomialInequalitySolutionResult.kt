package ci.us.zhixing.app.domain.math.model

/**
 * One connected subset of the real line.
 *
 * A null lower / upper bound represents negative / positive infinity. Infinite
 * endpoints are always open. Finite singleton intervals are represented with
 * equal, inclusive lower and upper bounds.
 */
internal data class RealInterval(
    val lowerBound: Double?,
    val lowerInclusive: Boolean,
    val upperBound: Double?,
    val upperInclusive: Boolean,
)

internal sealed interface PolynomialInequalitySolutionResult {
    object AllRealNumbers : PolynomialInequalitySolutionResult
    object NoSolutions : PolynomialInequalitySolutionResult

    data class Intervals(
        val intervals: List<RealInterval>,
    ) : PolynomialInequalitySolutionResult

    data class UnsupportedDegree(
        val degree: Int,
    ) : PolynomialInequalitySolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : PolynomialInequalitySolutionResult
}
