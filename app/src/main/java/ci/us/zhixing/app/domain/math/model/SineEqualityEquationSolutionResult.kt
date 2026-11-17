package ci.us.zhixing.app.domain.math.model

/** Result for sin(a*x+b)=sin(c*x+d) over the real numbers. */
internal sealed interface SineEqualityEquationSolutionResult {
    object AllRealNumbers : SineEqualityEquationSolutionResult

    object NoSolutions : SineEqualityEquationSolutionResult

    data class PeriodicSolutions(
        val families: List<PeriodicSolutionFamily>,
    ) : SineEqualityEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : SineEqualityEquationSolutionResult
}
