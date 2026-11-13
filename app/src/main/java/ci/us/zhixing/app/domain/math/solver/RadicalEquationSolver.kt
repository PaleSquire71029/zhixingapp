package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.RadicalEquation
import ci.us.zhixing.app.domain.math.model.RadicalEquationSolutionResult
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Solves sqrt(P(x)) = Q(x) for deg(P) <= 2 and deg(Q) <= 1.
 *
 * Squaring is used only to generate candidates. Every finite candidate is
 * checked against P(x) >= 0, Q(x) >= 0, and the original unsquared equation.
 * If P-Q² is identically zero, the result carries the exact non-negative
 * domain conditions instead of incorrectly claiming every real number works.
 */
internal object RadicalEquationSolver {
    fun solve(equation: RadicalEquation): RadicalEquationSolutionResult {
        if (equation.radicand.degree > MAX_RADICAND_DEGREE ||
            equation.right.degree > MAX_RIGHT_DEGREE
        ) {
            return RadicalEquationSolutionResult.UnsupportedStructure(
                radicandDegree = equation.radicand.degree,
                rightDegree = equation.right.degree,
            )
        }

        val squaredRight = when (
            val squared = PolynomialArithmetic.multiply(equation.right, equation.right)
        ) {
            is PolynomialOperationResult.Success -> squared.polynomial
            is PolynomialOperationResult.Failure ->
                return RadicalEquationSolutionResult.Failure(squared.error)
        }

        val candidatePolynomial = when (
            val reduced = PolynomialArithmetic.subtract(equation.radicand, squaredRight)
        ) {
            is PolynomialOperationResult.Success -> reduced.polynomial
            is PolynomialOperationResult.Failure ->
                return RadicalEquationSolutionResult.Failure(reduced.error)
        }

        return when (val roots = PolynomialArithmetic.solveRealRoots(candidatePolynomial)) {
            PolynomialRootsResult.AllRealNumbers -> allSolutionsOnDomain(equation)
            PolynomialRootsResult.NoRealRoots -> RadicalEquationSolutionResult.NoSolutions
            is PolynomialRootsResult.UnsupportedDegree ->
                RadicalEquationSolutionResult.UnsupportedStructure(
                    radicandDegree = equation.radicand.degree,
                    rightDegree = equation.right.degree,
                )
            is PolynomialRootsResult.Failure ->
                RadicalEquationSolutionResult.Failure(roots.error)
            is PolynomialRootsResult.FiniteRoots -> validateCandidates(equation, roots)
        }
    }

    private fun allSolutionsOnDomain(equation: RadicalEquation): RadicalEquationSolutionResult {
        val constraints = mutableListOf<Polynomial>()
        for (condition in listOf(equation.radicand, equation.right)) {
            if (condition.degree == 0) {
                if (PolynomialArithmetic.signAt(condition, 0.0) < 0) {
                    return RadicalEquationSolutionResult.NoSolutions
                }
            } else if (!condition.isZero()) {
                constraints += condition
            }
        }
        return RadicalEquationSolutionResult.AllRealNumbersOnDomain(constraints.distinct())
    }

    private fun validateCandidates(
        equation: RadicalEquation,
        roots: PolynomialRootsResult.FiniteRoots,
    ): RadicalEquationSolutionResult {
        val accepted = mutableListOf<EquationSolution>()
        for (root in roots.roots) {
            val radicandSign = PolynomialArithmetic.signAt(equation.radicand, root.value)
            val rightSign = PolynomialArithmetic.signAt(equation.right, root.value)

            // These are the domain conditions lost by squaring.
            if (radicandSign < 0 || rightSign < 0) continue

            val radicandValue = when (
                val evaluation = PolynomialArithmetic.evaluate(equation.radicand, root.value)
            ) {
                is PolynomialEvaluationResult.Success -> evaluation.value
                is PolynomialEvaluationResult.Failure ->
                    return RadicalEquationSolutionResult.Failure(evaluation.error)
            }
            val rightValue = when (
                val evaluation = PolynomialArithmetic.evaluate(equation.right, root.value)
            ) {
                is PolynomialEvaluationResult.Success -> evaluation.value
                is PolynomialEvaluationResult.Failure ->
                    return RadicalEquationSolutionResult.Failure(evaluation.error)
            }

            if (!agreesWithExactSign(radicandSign, radicandValue) ||
                !agreesWithExactSign(rightSign, rightValue)
            ) {
                return precisionFailure(
                    "根式方程定义域检查发生数值舍入，无法可靠判断符号",
                )
            }

            val radicalValue = sqrt(radicandValue)
            val scale = maxOf(abs(radicalValue), abs(rightValue))
            val normalizedResidual = if (scale == 0.0) {
                0.0
            } else {
                abs(radicalValue / scale - rightValue / scale)
            }
            if (!normalizedResidual.isFinite() ||
                normalizedResidual > ROOT_RESIDUAL_TOLERANCE
            ) {
                return precisionFailure(
                    "平方后得到的候选根未通过原根式方程残差检查",
                )
            }

            accepted += EquationSolution(
                value = root.value,
                normalizedResidual = normalizedResidual,
            )
        }

        return if (accepted.isEmpty()) {
            RadicalEquationSolutionResult.NoSolutions
        } else {
            RadicalEquationSolutionResult.FiniteSolutions(
                accepted.distinctBy { it.value }.sortedBy { it.value },
            )
        }
    }

    private fun agreesWithExactSign(sign: Int, value: Double): Boolean = when {
        sign < 0 -> value < 0.0
        sign == 0 -> value == 0.0
        else -> value > 0.0
    }

    private fun precisionFailure(message: String) =
        RadicalEquationSolutionResult.Failure(
            ci.us.zhixing.app.domain.math.model.PolynomialError(
                ci.us.zhixing.app.domain.math.model.PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                message,
            ),
        )

    private const val MAX_RADICAND_DEGREE = 2
    private const val MAX_RIGHT_DEGREE = 1
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
}
