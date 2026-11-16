package ci.us.zhixing.app.domain.math.model

/** Result for A*sin(w*x+phase)+B*cos(w*x+phase)=target over real x. */
internal sealed interface LinearCombinationTrigonometricEquationSolutionResult {
    object AllRealNumbers : LinearCombinationTrigonometricEquationSolutionResult

    object NoSolutions : LinearCombinationTrigonometricEquationSolutionResult

    data class PeriodicSolutions(
        val families: List<PeriodicSolutionFamily>,
    ) : LinearCombinationTrigonometricEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : LinearCombinationTrigonometricEquationSolutionResult
}
