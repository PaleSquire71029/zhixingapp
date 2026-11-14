package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.AbsoluteValueEquationWithLinearRightSide
import ci.us.zhixing.app.domain.math.model.AbsoluteValueLinearRightEquationResult
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import kotlin.math.abs

/**
 * Solves |P(x)| = Q(x), where deg(P) <= 2 and deg(Q) <= 1.
 *
 * The two algebraic branches are P-Q=0 and P+Q=0. Both must also satisfy
 * Q(x)>=0; identity branches therefore return their non-negative domain
 * condition instead of incorrectly claiming all real numbers.
 */
internal object AbsoluteValueLinearRightEquationSolver {
    fun solve(
        equation: AbsoluteValueEquationWithLinearRightSide,
    ): AbsoluteValueLinearRightEquationResult {
        if (equation.expression.degree > MAX_EXPRESSION_DEGREE ||
            equation.right.degree > MAX_RIGHT_DEGREE
        ) {
            return AbsoluteValueLinearRightEquationResult.UnsupportedStructure(
                expressionDegree = equation.expression.degree,
                rightDegree = equation.right.degree,
            )
        }

        val positiveBranch = when (
            val reduced = PolynomialArithmetic.subtract(equation.expression, equation.right)
        ) {
            is PolynomialOperationResult.Success -> reduced.polynomial
            is PolynomialOperationResult.Failure ->
                return AbsoluteValueLinearRightEquationResult.Failure(reduced.error)
        }

        val candidates = mutableListOf<EquationSolution>()
        when (val roots = PolynomialArithmetic.solveRealRoots(positiveBranch)) {
            PolynomialRootsResult.AllRealNumbers ->
                return allSolutionsOnNonNegativeRight(equation.right)
            PolynomialRootsResult.NoRealRoots -> Unit
            is PolynomialRootsResult.UnsupportedDegree ->
                return unsupported(equation)
            is PolynomialRootsResult.Failure ->
                return AbsoluteValueLinearRightEquationResult.Failure(roots.error)
            is PolynomialRootsResult.FiniteRoots -> when (
                val validation = collectValidSolutions(equation, roots)
            ) {
                is CandidateCollection.Success -> candidates += validation.solutions
                is CandidateCollection.Failure ->
                    return AbsoluteValueLinearRightEquationResult.Failure(validation.error)
            }
        }

        val negativeBranch = when (
            val reduced = PolynomialArithmetic.add(equation.expression, equation.right)
        ) {
            is PolynomialOperationResult.Success -> reduced.polynomial
            is PolynomialOperationResult.Failure ->
                return AbsoluteValueLinearRightEquationResult.Failure(reduced.error)
        }
        when (val roots = PolynomialArithmetic.solveRealRoots(negativeBranch)) {
            PolynomialRootsResult.AllRealNumbers ->
                return allSolutionsOnNonNegativeRight(equation.right)
            PolynomialRootsResult.NoRealRoots -> Unit
            is PolynomialRootsResult.UnsupportedDegree ->
                return unsupported(equation)
            is PolynomialRootsResult.Failure ->
                return AbsoluteValueLinearRightEquationResult.Failure(roots.error)
            is PolynomialRootsResult.FiniteRoots -> when (
                val validation = collectValidSolutions(equation, roots)
            ) {
                is CandidateCollection.Success -> candidates += validation.solutions
                is CandidateCollection.Failure ->
                    return AbsoluteValueLinearRightEquationResult.Failure(validation.error)
            }
        }

        val unique = candidates.distinctBy { it.value }.sortedBy { it.value }
        return if (unique.isEmpty()) {
            AbsoluteValueLinearRightEquationResult.NoSolutions
        } else {
            AbsoluteValueLinearRightEquationResult.FiniteSolutions(unique)
        }
    }

    private fun unsupported(
        equation: AbsoluteValueEquationWithLinearRightSide,
    ) = AbsoluteValueLinearRightEquationResult.UnsupportedStructure(
        expressionDegree = equation.expression.degree,
        rightDegree = equation.right.degree,
    )

    private fun allSolutionsOnNonNegativeRight(
        right: Polynomial,
    ): AbsoluteValueLinearRightEquationResult {
        if (right.isZero()) {
            return AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain(emptyList())
        }
        if (right.degree == 0) {
            return if (right.coefficients[0] < 0.0) {
                AbsoluteValueLinearRightEquationResult.NoSolutions
            } else {
                AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain(emptyList())
            }
        }
        return AbsoluteValueLinearRightEquationResult.AllRealNumbersOnDomain(listOf(right))
    }

    private fun collectValidSolutions(
        equation: AbsoluteValueEquationWithLinearRightSide,
        roots: PolynomialRootsResult.FiniteRoots,
    ): CandidateCollection {
        val accepted = mutableListOf<EquationSolution>()
        for (root in roots.roots) {
            val rightSign = PolynomialArithmetic.signAt(equation.right, root.value)
            val rightValue = when (
                val evaluated = PolynomialArithmetic.evaluate(equation.right, root.value)
            ) {
                is PolynomialEvaluationResult.Success -> evaluated.value
                is PolynomialEvaluationResult.Failure ->
                    return CandidateCollection.Failure(evaluated.error)
            }
            if (!agreesWithExactSign(rightSign, rightValue)) {
                return CandidateCollection.Failure(
                    PolynomialError(
                        PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                        "绝对值方程右侧的非负性检查发生数值舍入",
                    ),
                )
            }
            if (rightSign < 0) continue

            val expressionValue = when (
                val evaluated = PolynomialArithmetic.evaluate(equation.expression, root.value)
            ) {
                is PolynomialEvaluationResult.Success -> evaluated.value
                is PolynomialEvaluationResult.Failure ->
                    return CandidateCollection.Failure(evaluated.error)
            }
            val absoluteValue = abs(expressionValue)
            val scale = maxOf(absoluteValue, abs(rightValue))
            val residual = if (scale == 0.0) {
                0.0
            } else {
                abs(absoluteValue / scale - rightValue / scale)
            }
            if (!residual.isFinite() || residual > ROOT_RESIDUAL_TOLERANCE) {
                return CandidateCollection.Failure(
                    PolynomialError(
                        PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                        "候选根未通过原绝对值方程回代检查",
                    ),
                )
            }
            accepted += EquationSolution(root.value, residual)
        }
        return CandidateCollection.Success(accepted)
    }

    private fun agreesWithExactSign(sign: Int, value: Double): Boolean = when {
        sign < 0 -> value < 0.0
        sign == 0 -> value == 0.0
        else -> value > 0.0
    }

    private sealed interface CandidateCollection {
        data class Success(val solutions: List<EquationSolution>) : CandidateCollection
        data class Failure(val error: PolynomialError) : CandidateCollection
    }

    private const val MAX_EXPRESSION_DEGREE = 2
    private const val MAX_RIGHT_DEGREE = 1
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
}
