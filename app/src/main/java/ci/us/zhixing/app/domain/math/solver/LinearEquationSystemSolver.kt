package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.LinearEquationSystem
import ci.us.zhixing.app.domain.math.model.LinearSystemSolutionResult
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import java.math.BigDecimal
import java.math.MathContext

/**
 * Solves two equations of the form a*x + b*y = c.
 *
 * BigDecimal is used for determinant / rank classification and Cramer's-rule
 * arithmetic so finite Double inputs do not overflow in intermediate products.
 * The final solution is still checked against Double range and the original
 * equations' normalized residual before it is returned.
 */
internal object LinearEquationSystemSolver {
    fun solve(system: LinearEquationSystem): LinearSystemSolutionResult {
        val inputValues = listOf(
            system.first.xCoefficient,
            system.first.yCoefficient,
            system.first.constant,
            system.second.xCoefficient,
            system.second.yCoefficient,
            system.second.constant,
        )
        if (inputValues.any { !it.isFinite() }) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "方程组的系数和常数项必须是有限数",
            )
        }

        val a = BigDecimal.valueOf(system.first.xCoefficient)
        val b = BigDecimal.valueOf(system.first.yCoefficient)
        val c = BigDecimal.valueOf(system.first.constant)
        val d = BigDecimal.valueOf(system.second.xCoefficient)
        val e = BigDecimal.valueOf(system.second.yCoefficient)
        val f = BigDecimal.valueOf(system.second.constant)

        val determinant = a.multiply(e).subtract(b.multiply(d))
        if (determinant.signum() == 0) {
            return classifySingularSystem(a, b, c, d, e, f)
        }

        val xNumerator = c.multiply(e).subtract(b.multiply(f))
        val yNumerator = a.multiply(f).subtract(c.multiply(d))
        val exactX = xNumerator.divide(determinant, DIVISION_CONTEXT)
        val exactY = yNumerator.divide(determinant, DIVISION_CONTEXT)
        val x = exactX.toDouble()
        val y = exactY.toDouble()

        if (!x.isFinite() || !y.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "方程组的唯一解超出 Double 可表示范围",
            )
        }
        if ((x == 0.0 && exactX.signum() != 0) ||
            (y == 0.0 && exactY.signum() != 0)
        ) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "方程组的非零解在 Double 中下溢为零",
            )
        }

        val decimalX = BigDecimal.valueOf(x)
        val decimalY = BigDecimal.valueOf(y)
        val firstResidual = normalizedResidual(a, b, c, decimalX, decimalY)
        val secondResidual = normalizedResidual(d, e, f, decimalX, decimalY)
        val residual = if (firstResidual >= secondResidual) firstResidual else secondResidual
        val normalizedResidual = residual.toDouble()
        if (!normalizedResidual.isFinite() || normalizedResidual > MAX_NORMALIZED_RESIDUAL) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "方程组的唯一解未通过原方程残差检查",
            )
        }

        return LinearSystemSolutionResult.UniqueSolution(
            x = x,
            y = y,
            normalizedResidual = normalizedResidual,
        )
    }

    private fun classifySingularSystem(
        a: BigDecimal,
        b: BigDecimal,
        c: BigDecimal,
        d: BigDecimal,
        e: BigDecimal,
        f: BigDecimal,
    ): LinearSystemSolutionResult {
        val firstRowIsZero = a.signum() == 0 && b.signum() == 0
        val secondRowIsZero = d.signum() == 0 && e.signum() == 0

        if (firstRowIsZero && secondRowIsZero) {
            return if (c.signum() == 0 && f.signum() == 0) {
                LinearSystemSolutionResult.AllRealPairs
            } else {
                LinearSystemSolutionResult.NoSolutions
            }
        }

        // With a zero coefficient determinant, consistency requires both
        // augmented 2x2 minors to vanish as well.
        val augmentedXMinor = a.multiply(f).subtract(d.multiply(c))
        val augmentedYMinor = b.multiply(f).subtract(e.multiply(c))
        return if (augmentedXMinor.signum() == 0 && augmentedYMinor.signum() == 0) {
            LinearSystemSolutionResult.AllRealPairs
        } else {
            LinearSystemSolutionResult.NoSolutions
        }
    }

    private fun normalizedResidual(
        a: BigDecimal,
        b: BigDecimal,
        c: BigDecimal,
        x: BigDecimal,
        y: BigDecimal,
    ): BigDecimal {
        val xTerm = a.multiply(x)
        val yTerm = b.multiply(y)
        val residual = xTerm.add(yTerm).subtract(c).abs()
        val scale = xTerm.abs().add(yTerm.abs()).add(c.abs())
        return if (scale.signum() == 0) {
            BigDecimal.ZERO
        } else {
            residual.divide(scale, DIVISION_CONTEXT)
        }
    }

    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = LinearSystemSolutionResult.Failure(
        PolynomialError(code = code, message = message),
    )

    private val DIVISION_CONTEXT = MathContext.DECIMAL128
    private const val MAX_NORMALIZED_RESIDUAL = 1e-12
}
