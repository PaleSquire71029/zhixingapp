package ci.us.zhixing.app.domain.math.model

/** Result of solving a real cube-root equation over the real numbers. */
internal sealed interface CubeRootEquationSolutionResult {
    /** Every real x is a solution. */
    object AllRealNumbers : CubeRootEquationSolutionResult

    object NoSolutions : CubeRootEquationSolutionResult

    data class FiniteSolutions(
        val solutions: List<EquationSolution>,
    ) : CubeRootEquationSolutionResult

    data class UnsupportedStructure(
        val radicandDegree: Int,
        val rightDegree: Int,
    ) : CubeRootEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : CubeRootEquationSolutionResult
}
