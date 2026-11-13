package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.EquationSolutionResult
import ci.us.zhixing.app.domain.math.model.PolynomialEquation
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult

/**
 * Solves polynomial equations through degree three.
 *
 * Both sides are represented independently. We reduce L(x) = R(x) to
 * L(x) - R(x) = 0, then reuse the polynomial root engine. Rational equations
 * use a separate solver so original denominator restrictions remain explicit.
 */
internal object PolynomialEquationSolver {
    fun solve(equation: PolynomialEquation): EquationSolutionResult {
        val reduced = when (
            val subtraction = PolynomialArithmetic.subtract(equation.left, equation.right)
        ) {
            is PolynomialOperationResult.Success -> subtraction.polynomial
            is PolynomialOperationResult.Failure -> {
                return EquationSolutionResult.Failure(subtraction.error)
            }
        }

        // Keep the equation layer aligned with the tested polynomial root
        // engine; unsupported higher degrees remain explicit rather than guessed.
        if (reduced.degree > MAX_SUPPORTED_DEGREE) {
            return EquationSolutionResult.UnsupportedDegree(reduced.degree)
        }

        return when (val roots = PolynomialArithmetic.solveRealRoots(reduced)) {
            PolynomialRootsResult.AllRealNumbers -> EquationSolutionResult.AllRealNumbers
            PolynomialRootsResult.NoRealRoots -> EquationSolutionResult.NoSolutions
            is PolynomialRootsResult.FiniteRoots -> EquationSolutionResult.FiniteSolutions(
                roots.roots.map { root ->
                    EquationSolution(
                        value = root.value,
                        normalizedResidual = root.normalizedResidual,
                    )
                },
            )
            is PolynomialRootsResult.UnsupportedDegree -> {
                EquationSolutionResult.UnsupportedDegree(roots.degree)
            }
            is PolynomialRootsResult.Failure -> EquationSolutionResult.Failure(roots.error)
        }
    }

    private const val MAX_SUPPORTED_DEGREE = 3
}
