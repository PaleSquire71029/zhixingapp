package ci.us.zhixing.app.domain.math.model

/** Result of solving tan(a*x+b)=c over the real numbers. */
internal sealed interface TangentEquationSolutionResult {
    object AllRealNumbers : TangentEquationSolutionResult

    object NoSolutions : TangentEquationSolutionResult

    data class PeriodicSolutions(
        val family: PeriodicSolutionFamily,
    ) : TangentEquationSolutionResult

    data class Failure(
        val error: PolynomialError,
    ) : TangentEquationSolutionResult
}
