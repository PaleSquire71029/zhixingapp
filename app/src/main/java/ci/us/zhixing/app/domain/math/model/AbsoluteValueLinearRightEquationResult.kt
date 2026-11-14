package ci.us.zhixing.app.domain.math.model

/** Solution set for |P(x)| = Q(x), including the non-negative right-side domain. */
internal sealed interface AbsoluteValueLinearRightEquationResult {
    /**
     * Every real x satisfying each listed polynomial condition >= 0 is a solution.
     * An empty list means every real number is a solution.
     */
    data class AllRealNumbersOnDomain(
        val nonNegativeConditions: List<Polynomial>,
    ) : AbsoluteValueLinearRightEquationResult

    object NoSolutions : AbsoluteValueLinearRightEquationResult

    data class FiniteSolutions(
        val solutions: List<EquationSolution>,
    ) : AbsoluteValueLinearRightEquationResult

    data class UnsupportedStructure(
        val expressionDegree: Int,
        val rightDegree: Int,
    ) : AbsoluteValueLinearRightEquationResult

    data class Failure(
        val error: PolynomialError,
    ) : AbsoluteValueLinearRightEquationResult
}
