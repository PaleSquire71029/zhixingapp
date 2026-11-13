package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.algebra.RationalFunctionArithmetic
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.RationalEquation
import ci.us.zhixing.app.domain.math.model.RationalEquationError
import ci.us.zhixing.app.domain.math.model.RationalEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.RationalFunctionOperationResult

/**
 * Solves rational equations without losing source-expression domain holes.
 *
 * The difference of the two rational functions carries both sides' original
 * denominator restrictions. Its numerator supplies candidate roots; every
 * candidate is then rejected when any original restriction is exactly zero.
 */
internal object RationalEquationSolver {
    fun solve(equation: RationalEquation): RationalEquationSolutionResult {
        val difference = when (
            val subtraction = RationalFunctionArithmetic.subtract(equation.left, equation.right)
        ) {
            is RationalFunctionOperationResult.Success -> subtraction.value
            is RationalFunctionOperationResult.Failure -> {
                return RationalEquationSolutionResult.Failure(
                    RationalEquationError.RationalFunctionOperation(subtraction.error),
                )
            }
        }

        if (difference.domainRestrictions.any { it.isZero() }) {
            return RationalEquationSolutionResult.Failure(
                RationalEquationError.RationalFunctionOperation(
                    ci.us.zhixing.app.domain.math.model.RationalFunctionError(
                        ci.us.zhixing.app.domain.math.model.RationalFunctionErrorCode.EMPTY_DOMAIN,
                        "原式定义域为空",
                    ),
                ),
            )
        }

        return when (val roots = PolynomialArithmetic.solveRealRoots(difference.numerator)) {
            PolynomialRootsResult.AllRealNumbers ->
                RationalEquationSolutionResult.AllRealNumbersInDomain(
                    difference.domainRestrictions,
                )

            PolynomialRootsResult.NoRealRoots -> RationalEquationSolutionResult.NoSolutions

            is PolynomialRootsResult.FiniteRoots -> {
                val validRoots = roots.roots.filter { root ->
                    difference.domainRestrictions.none { restriction ->
                        PolynomialArithmetic.isExactlyZeroAt(restriction, root.value)
                    }
                }
                if (validRoots.isEmpty()) {
                    RationalEquationSolutionResult.NoSolutions
                } else {
                    RationalEquationSolutionResult.FiniteSolutions(
                        validRoots.map { root ->
                            EquationSolution(
                                value = root.value,
                                normalizedResidual = root.normalizedResidual,
                            )
                        },
                    )
                }
            }

            is PolynomialRootsResult.UnsupportedDegree ->
                RationalEquationSolutionResult.UnsupportedDegree(roots.degree)

            is PolynomialRootsResult.Failure ->
                RationalEquationSolutionResult.Failure(
                    RationalEquationError.PolynomialOperation(roots.error),
                )
        }
    }
}
