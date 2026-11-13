package ci.us.zhixing.app.domain.math.model

/** A rational equation's solution set, including the original expression domain. */
internal sealed interface RationalEquationSolutionResult {
    /**
     * The equation is an identity at every point in the original domain.
     * Restrictions are kept as exact polynomial predicates p(x) != 0, rather
     * than approximating or prematurely enumerating every excluded point.
     */
    data class AllRealNumbersInDomain(
        val domainRestrictions: List<Polynomial>,
    ) : RationalEquationSolutionResult

    object NoSolutions : RationalEquationSolutionResult

    data class FiniteSolutions(
        val solutions: List<EquationSolution>,
    ) : RationalEquationSolutionResult

    data class UnsupportedDegree(
        val degree: Int,
    ) : RationalEquationSolutionResult

    data class Failure(
        val error: RationalEquationError,
    ) : RationalEquationSolutionResult
}

/** Keeps polynomial and rational-function failures distinguishable. */
internal sealed interface RationalEquationError {
    data class PolynomialOperation(
        val error: PolynomialError,
    ) : RationalEquationError

    data class RationalFunctionOperation(
        val error: RationalFunctionError,
    ) : RationalEquationError
}
