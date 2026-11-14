package ci.us.zhixing.app.domain.math.model

/**
 * A periodic real solution family represented by
 * x = firstSolution + k * period, k in Z.
 */
internal data class PeriodicSolutionFamily(
    val firstSolution: Double,
    val period: Double,
)

/** Result of solving sin(a*x+b)=c over the real numbers. */
internal sealed interface SineEquationSolutionResult {
    object AllRealNumbers : SineEquationSolutionResult

    object NoSolutions : SineEquationSolutionResult

    data class PeriodicSolutions(
        val families: List<PeriodicSolutionFamily>,
    ) : SineEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : SineEquationSolutionResult
}
