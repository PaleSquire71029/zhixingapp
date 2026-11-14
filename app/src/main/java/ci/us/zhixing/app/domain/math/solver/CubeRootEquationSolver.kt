package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.CubeRootEquation
import ci.us.zhixing.app.domain.math.model.CubeRootEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import kotlin.math.abs

/**
 * Solves the real cube-root equation cubert(P(x)) = Q(x) for deg(P) <= 3
 * and deg(Q) <= 1.
 *
 * The real cube function is one-to-one on the whole real line, so this
 * equation is equivalent to P(x) = Q(x)^3. The polynomial roots are still
 * checked against the original cube-root equation to guard numeric error.
 * Unlike square roots, no non-negative domain restriction is introduced.
 */
internal object CubeRootEquationSolver {
    fun solve(equation: CubeRootEquation): CubeRootEquationSolutionResult {
        if (equation.radicand.degree > MAX_RADICAND_DEGREE ||
            equation.right.degree > MAX_RIGHT_DEGREE
        ) {
            return CubeRootEquationSolutionResult.UnsupportedStructure(
                radicandDegree = equation.radicand.degree,
                rightDegree = equation.right.degree,
            )
        }

        val squaredRight = when (
            val squared = PolynomialArithmetic.multiply(equation.right, equation.right)
        ) {
            is PolynomialOperationResult.Success -> squared.polynomial
            is PolynomialOperationResult.Failure ->
                return CubeRootEquationSolutionResult.Failure(squared.error)
        }
        val cubedRight = when (
            val cubed = PolynomialArithmetic.multiply(squaredRight, equation.right)
        ) {
            is PolynomialOperationResult.Success -> cubed.polynomial
            is PolynomialOperationResult.Failure ->
                return CubeRootEquationSolutionResult.Failure(cubed.error)
        }
        val candidatePolynomial = when (
            val reduced = PolynomialArithmetic.subtract(equation.radicand, cubedRight)
        ) {
            is PolynomialOperationResult.Success -> reduced.polynomial
            is PolynomialOperationResult.Failure ->
                return CubeRootEquationSolutionResult.Failure(reduced.error)
        }

        return when (val roots = PolynomialArithmetic.solveRealRoots(candidatePolynomial)) {
            PolynomialRootsResult.AllRealNumbers ->
                CubeRootEquationSolutionResult.AllRealNumbers
            PolynomialRootsResult.NoRealRoots ->
                CubeRootEquationSolutionResult.NoSolutions
            is PolynomialRootsResult.UnsupportedDegree ->
                CubeRootEquationSolutionResult.UnsupportedStructure(
                    radicandDegree = equation.radicand.degree,
                    rightDegree = equation.right.degree,
                )
            is PolynomialRootsResult.Failure ->
                CubeRootEquationSolutionResult.Failure(roots.error)
            is PolynomialRootsResult.FiniteRoots -> validateOriginalEquation(equation, roots)
        }
    }

    private fun validateOriginalEquation(
        equation: CubeRootEquation,
        roots: PolynomialRootsResult.FiniteRoots,
    ): CubeRootEquationSolutionResult {
        val solutions = mutableListOf<EquationSolution>()
        for (root in roots.roots) {
            val radicandValue = when (
                val evaluated = PolynomialArithmetic.evaluate(equation.radicand, root.value)
            ) {
                is PolynomialEvaluationResult.Success -> evaluated.value
                is PolynomialEvaluationResult.Failure ->
                    return CubeRootEquationSolutionResult.Failure(evaluated.error)
            }
            val rightValue = when (
                val evaluated = PolynomialArithmetic.evaluate(equation.right, root.value)
            ) {
                is PolynomialEvaluationResult.Success -> evaluated.value
                is PolynomialEvaluationResult.Failure ->
                    return CubeRootEquationSolutionResult.Failure(evaluated.error)
            }

            val cubeRootValue = Math.cbrt(radicandValue)
            val scale = maxOf(abs(cubeRootValue), abs(rightValue))
            val normalizedResidual = if (scale == 0.0) {
                0.0
            } else {
                abs(cubeRootValue / scale - rightValue / scale)
            }
            if (!cubeRootValue.isFinite() ||
                !normalizedResidual.isFinite() ||
                normalizedResidual > ROOT_RESIDUAL_TOLERANCE
            ) {
                return CubeRootEquationSolutionResult.Failure(
                    PolynomialError(
                        code = PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                        message = "候选根未通过原立方根方程回代检查",
                    ),
                )
            }

            solutions += EquationSolution(
                value = root.value,
                normalizedResidual = normalizedResidual,
            )
        }

        return if (solutions.isEmpty()) {
            CubeRootEquationSolutionResult.NoSolutions
        } else {
            CubeRootEquationSolutionResult.FiniteSolutions(
                solutions.distinctBy { it.value }.sortedBy { it.value },
            )
        }
    }

    private const val MAX_RADICAND_DEGREE = 3
    private const val MAX_RIGHT_DEGREE = 1
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
}
