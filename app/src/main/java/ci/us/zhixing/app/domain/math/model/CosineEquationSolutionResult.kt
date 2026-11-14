package ci.us.zhixing.app.domain.math.model

/** Result of solving cos(a*x+b)=c over the real numbers. */
internal sealed interface CosineEquationSolutionResult {
    object AllRealNumbers : CosineEquationSolutionResult

    object NoSolutions : CosineEquationSolutionResult

    data class PeriodicSolutions(
        val families: List<PeriodicSolutionFamily>,
    ) : CosineEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : CosineEquationSolutionResult
}
