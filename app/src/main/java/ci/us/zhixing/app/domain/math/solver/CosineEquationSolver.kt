package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.CosineEquation
import ci.us.zhixing.app.domain.math.model.CosineEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.max

/**
 * Solves cos(a*x+b)=c over real x, with all angles in radians.
 *
 * For non-zero a, the solution is represented by one or two periodic
 * families. For a=0, the equation is constant and is classified directly.
 */
internal object CosineEquationSolver {
    fun solve(equation: CosineEquation): CosineEquationSolutionResult {
        if (!equation.coefficient.isFinite() ||
            !equation.phase.isFinite() ||
            !equation.target.isFinite()
        ) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "余弦方程的系数、相位和目标值必须是有限数",
            )
        }

        if (equation.target < -1.0 || equation.target > 1.0) {
            return CosineEquationSolutionResult.NoSolutions
        }

        if (equation.coefficient == 0.0) {
            val actual = kotlin.math.cos(equation.phase)
            return if (normalizedDifference(actual, equation.target) <= ROOT_RESIDUAL_TOLERANCE) {
                CosineEquationSolutionResult.AllRealNumbers
            } else {
                CosineEquationSolutionResult.NoSolutions
            }
        }

        val period = TWO_PI / abs(equation.coefficient)
        if (!period.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "余弦方程的周期超出 Double 可表示范围",
            )
        }
        if (period <= 0.0) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "余弦方程的周期无法在 Double 中表示",
            )
        }

        val principalAngle = acos(equation.target)
        val firstNumerator = principalAngle - equation.phase
        val firstRaw = firstNumerator / equation.coefficient
        val first = when (
            val normalized = normalizeFamily(firstRaw, firstNumerator, equation, period)
        ) {
            is FamilyNormalization.Success -> normalized.family
            is FamilyNormalization.Failure -> return normalized.failure
        }

        val families = mutableListOf(first)
        if (abs(equation.target) != 1.0) {
            val secondNumerator = -principalAngle - equation.phase
            val secondRaw = secondNumerator / equation.coefficient
            val second = when (
                val normalized = normalizeFamily(secondRaw, secondNumerator, equation, period)
            ) {
                is FamilyNormalization.Success -> normalized.family
                is FamilyNormalization.Failure -> return normalized.failure
            }
            if (abs(second.firstSolution - first.firstSolution) >
                FAMILY_MERGE_TOLERANCE * period
            ) {
                families += second
            }
        }

        return CosineEquationSolutionResult.PeriodicSolutions(
            families.sortedBy { it.firstSolution },
        )
    }

    private fun normalizeFamily(
        rawBase: Double,
        numerator: Double,
        equation: CosineEquation,
        period: Double,
    ): FamilyNormalization {
        if (!rawBase.isFinite()) {
            return normalizationFailure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "余弦方程的周期解基准值不是有限数",
            )
        }
        if (rawBase == 0.0 && numerator != 0.0) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "余弦方程的非零解基准值下溢为零",
            )
        }

        val periodCount = rawBase / period
        if (!periodCount.isFinite() || abs(periodCount) > MAX_RELIABLE_PERIOD_COUNT) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "相位过大，无法可靠地归一化余弦方程的周期解",
            )
        }

        var normalized = rawBase % period
        if (normalized < 0.0) normalized += period
        if (normalized >= period) normalized = 0.0
        if (normalized == 0.0) normalized = 0.0

        val evaluatedAngle = equation.coefficient * normalized + equation.phase
        if (!evaluatedAngle.isFinite()) {
            return normalizationFailure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "余弦方程回代时角度超出 Double 范围",
            )
        }
        val residual = normalizedDifference(
            kotlin.math.cos(evaluatedAngle),
            equation.target,
        )
        if (!residual.isFinite() || residual > ROOT_RESIDUAL_TOLERANCE) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "余弦方程周期解未通过原式回代检查",
            )
        }

        return FamilyNormalization.Success(
            PeriodicSolutionFamily(
                firstSolution = normalized,
                period = period,
            ),
        )
    }

    private fun normalizedDifference(actual: Double, expected: Double): Double =
        abs(actual - expected) / max(1.0, max(abs(actual), abs(expected)))

    private fun normalizationFailure(
        code: PolynomialErrorCode,
        message: String,
    ) = FamilyNormalization.Failure(failure(code, message))

    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = CosineEquationSolutionResult.Failure(
        PolynomialError(code = code, message = message),
    )

    private sealed interface FamilyNormalization {
        data class Success(val family: PeriodicSolutionFamily) : FamilyNormalization
        data class Failure(val failure: CosineEquationSolutionResult.Failure) : FamilyNormalization
    }

    private const val PI = Math.PI
    private const val TWO_PI = 2.0 * Math.PI
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
    private const val FAMILY_MERGE_TOLERANCE = 1e-12
    private const val MAX_RELIABLE_PERIOD_COUNT = 4_503_599_627_370_496.0
}
