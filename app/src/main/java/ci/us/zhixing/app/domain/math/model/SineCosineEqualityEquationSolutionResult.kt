package ci.us.zhixing.app.domain.math.model

/** Result for sin(a*x+b)=cos(c*x+d) over the real numbers. */
internal sealed interface SineCosineEqualityEquationSolutionResult {
    object AllRealNumbers : SineCosineEqualityEquationSolutionResult

    object NoSolutions : SineCosineEqualityEquationSolutionResult

    data class PeriodicSolutions(
        val families: List<PeriodicSolutionFamily>,
    ) : SineCosineEqualityEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : SineCosineEqualityEquationSolutionResult
}
