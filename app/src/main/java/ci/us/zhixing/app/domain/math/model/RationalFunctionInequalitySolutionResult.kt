package ci.us.zhixing.app.domain.math.model

/** Solution set for a rational-function inequality over the real numbers. */
internal sealed interface RationalFunctionInequalitySolutionResult {
    object AllRealNumbers : RationalFunctionInequalitySolutionResult
    object NoSolutions : RationalFunctionInequalitySolutionResult

    /** Finite solution components; excluded domain points remain open endpoints. */
    data class Intervals(
        val intervals: List<RealInterval>,
    ) : RationalFunctionInequalitySolutionResult

    data class UnsupportedDegree(
        val degree: Int,
    ) : RationalFunctionInequalitySolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : RationalFunctionInequalitySolutionResult
}
