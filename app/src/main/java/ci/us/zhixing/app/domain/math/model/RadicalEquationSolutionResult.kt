package ci.us.zhixing.app.domain.math.model

/** Result of solving a single square-root equation over the real numbers. */
internal sealed interface RadicalEquationSolutionResult {
    /**
     * Every real x satisfying all listed non-negative polynomial conditions
     * is a solution. An empty list means every real number is a solution.
     */
    data class AllRealNumbersOnDomain(
        val nonNegativeConditions: List<Polynomial>,
    ) : RadicalEquationSolutionResult

    object NoSolutions : RadicalEquationSolutionResult

    data class FiniteSolutions(
        val solutions: List<EquationSolution>,
    ) : RadicalEquationSolutionResult

    data class UnsupportedStructure(
        val radicandDegree: Int,
        val rightDegree: Int,
    ) : RadicalEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : RadicalEquationSolutionResult
}
