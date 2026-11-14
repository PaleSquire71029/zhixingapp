package ci.us.zhixing.app.domain.math.model

/** Solution set of a two-variable linear equation system. */
internal sealed interface LinearSystemSolutionResult {
    /** Every ordered pair (x, y) of real numbers satisfies the system. */
    object AllRealPairs : LinearSystemSolutionResult

    object NoSolutions : LinearSystemSolutionResult

    data class UniqueSolution(
        val x: Double,
        val y: Double,
        /** Maximum normalized backward error across the two original equations. */
        val normalizedResidual: Double,
    ) : LinearSystemSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : LinearSystemSolutionResult
}
