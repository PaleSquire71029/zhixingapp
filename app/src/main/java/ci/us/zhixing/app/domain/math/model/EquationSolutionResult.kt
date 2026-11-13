package ci.us.zhixing.app.domain.math.model

/** A finite equation solution and the normalized residual of the reduced equation. */
internal data class EquationSolution(
    val value: Double,
    val normalizedResidual: Double,
)

/**
 * Result of solving a polynomial equation over the real numbers.
 *
 * Rational-equation domain restrictions are intentionally handled by a later
 * solver slice; this result describes the polynomial equation only.
 */
internal sealed interface EquationSolutionResult {
    object AllRealNumbers : EquationSolutionResult
    object NoSolutions : EquationSolutionResult
    data class FiniteSolutions(
        val solutions: List<EquationSolution>,
    ) : EquationSolutionResult
    data class UnsupportedDegree(
        val degree: Int,
    ) : EquationSolutionResult
    data class Failure(
        val error: PolynomialError,
    ) : EquationSolutionResult
}
