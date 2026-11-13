package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.AbsoluteValueEquation
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.EquationSolutionResult
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult

/**
 * Solves |a*x + b| = c by solving a*x+b-c=0 and a*x+b+c=0.
 *
 * Constant inner expressions are classified directly. Negative targets have
 * no solutions. This slice does not claim to solve variable right-hand sides
 * or nested / multiple absolute values.
 */
internal object AbsoluteValueEquationSolver {
    fun solve(equation: AbsoluteValueEquation): EquationSolutionResult {
        if (!equation.target.isFinite()) {
            return EquationSolutionResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NON_FINITE_INPUT,
                    "绝对值方程右侧必须是有限数",
                ),
            )
        }
        if (equation.target < 0.0) return EquationSolutionResult.NoSolutions
        if (equation.expression.degree > MAX_INNER_DEGREE) {
            return EquationSolutionResult.UnsupportedDegree(equation.expression.degree)
        }

        if (equation.expression.degree == 0) {
            return if (kotlin.math.abs(equation.expression.coefficients[0]) == equation.target) {
                EquationSolutionResult.AllRealNumbers
            } else {
                EquationSolutionResult.NoSolutions
            }
        }

        val positiveBranch = when (
            val reduced = PolynomialArithmetic.subtract(
                equation.expression,
                ci.us.zhixing.app.domain.math.model.Polynomial.constant(equation.target),
            )
        ) {
            is PolynomialOperationResult.Success -> reduced.polynomial
            is PolynomialOperationResult.Failure -> return EquationSolutionResult.Failure(reduced.error)
        }
        val positiveRoots = when (val roots = PolynomialArithmetic.solveRealRoots(positiveBranch)) {
            is PolynomialRootsResult.FiniteRoots -> roots.roots
            PolynomialRootsResult.AllRealNumbers -> return EquationSolutionResult.AllRealNumbers
            PolynomialRootsResult.NoRealRoots -> emptyList()
            is PolynomialRootsResult.UnsupportedDegree -> {
                return EquationSolutionResult.UnsupportedDegree(roots.degree)
            }
            is PolynomialRootsResult.Failure -> return EquationSolutionResult.Failure(roots.error)
        }

        if (equation.target == 0.0) {
            return EquationSolutionResult.FiniteSolutions(
                positiveRoots.map { EquationSolution(it.value, it.normalizedResidual) },
            )
        }

        val negativeBranch = when (
            val reduced = PolynomialArithmetic.add(
                equation.expression,
                ci.us.zhixing.app.domain.math.model.Polynomial.constant(equation.target),
            )
        ) {
            is PolynomialOperationResult.Success -> reduced.polynomial
            is PolynomialOperationResult.Failure -> return EquationSolutionResult.Failure(reduced.error)
        }
        val negativeRoots = when (val roots = PolynomialArithmetic.solveRealRoots(negativeBranch)) {
            is PolynomialRootsResult.FiniteRoots -> roots.roots
            PolynomialRootsResult.AllRealNumbers -> return EquationSolutionResult.AllRealNumbers
            PolynomialRootsResult.NoRealRoots -> emptyList()
            is PolynomialRootsResult.UnsupportedDegree -> {
                return EquationSolutionResult.UnsupportedDegree(roots.degree)
            }
            is PolynomialRootsResult.Failure -> return EquationSolutionResult.Failure(roots.error)
        }

        val solutions = (positiveRoots + negativeRoots)
            .distinctBy { it.value }
            .sortedBy { it.value }
            .map { EquationSolution(it.value, it.normalizedResidual) }

        return if (solutions.isEmpty()) {
            EquationSolutionResult.NoSolutions
        } else {
            EquationSolutionResult.FiniteSolutions(solutions)
        }
    }

    private const val MAX_INNER_DEGREE = 1
}
