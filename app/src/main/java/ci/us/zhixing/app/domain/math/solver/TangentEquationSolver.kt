package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.TangentEquation
import ci.us.zhixing.app.domain.math.model.TangentEquationSolutionResult
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.max
import kotlin.math.round
import kotlin.math.tan

/**
 * Solves tan(a*x+b)=c over real x, with all angles in radians.
 *
 * For non-zero a, the solutions form one family with period pi/|a|.
 * For a=0, the constant angle is checked separately because tangent is
 * undefined at pi/2 + k*pi.
 */
internal object TangentEquationSolver {
    fun solve(equation: TangentEquation): TangentEquationSolutionResult {
        if (!equation.coefficient.isFinite() ||
            !equation.phase.isFinite() ||
            !equation.target.isFinite()
        ) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "正切方程的系数、相位和目标值必须是有限数",
            )
        }

        if (equation.coefficient == 0.0) {
            return solveConstantEquation(equation)
        }

        val period = PI / abs(equation.coefficient)
        if (!period.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "正切方程的周期超出 Double 可表示范围",
            )
        }
        if (period <= 0.0) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "正切方程的周期无法在 Double 中表示",
            )
        }

        val principalAngle = atan(equation.target)
        val numerator = principalAngle - equation.phase
        val rawBase = numerator / equation.coefficient
        return when (
            val normalized = normalizeFamily(rawBase, numerator, equation, period)
        ) {
            is FamilyNormalization.Success ->
                TangentEquationSolutionResult.PeriodicSolutions(normalized.family)
            is FamilyNormalization.Failure -> normalized.failure
        }
    }

    private fun solveConstantEquation(
        equation: TangentEquation,
    ): TangentEquationSolutionResult {
        val periodCount = equation.phase / PI
        if (!periodCount.isFinite() || abs(periodCount) > MAX_RELIABLE_PERIOD_COUNT) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "相位过大，无法可靠地判断正切方程的定义域",
            )
        }
        if (isCanonicalPole(equation.phase)) {
            return TangentEquationSolutionResult.NoSolutions
        }

        val actual = tan(equation.phase)
        if (!actual.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "正切方程的常数角求值超出 Double 范围",
            )
        }
        return if (normalizedDifference(actual, equation.target) <= ROOT_RESIDUAL_TOLERANCE) {
            TangentEquationSolutionResult.AllRealNumbers
        } else {
            TangentEquationSolutionResult.NoSolutions
        }
    }

    /**
     * Detects an exactly represented canonical pole without rejecting nearby
     * finite angles. A tolerance-based cosine check would incorrectly discard
     * valid values immediately next to pi/2 + k*pi.
     */
    private fun isCanonicalPole(angle: Double): Boolean {
        val nearestPoleIndex = round((angle - HALF_PI) / PI)
        if (!nearestPoleIndex.isFinite() ||
            abs(nearestPoleIndex) > MAX_RELIABLE_PERIOD_COUNT
        ) {
            return false
        }
        val representedPole = HALF_PI + nearestPoleIndex * PI
        return angle == representedPole
    }

    private fun normalizeFamily(
        rawBase: Double,
        numerator: Double,
        equation: TangentEquation,
        period: Double,
    ): FamilyNormalization {
        if (!rawBase.isFinite()) {
            return normalizationFailure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "正切方程的周期解基准值不是有限数",
            )
        }
        if (rawBase == 0.0 && numerator != 0.0) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "正切方程的非零解基准值下溢为零",
            )
        }

        val periodCount = rawBase / period
        if (!periodCount.isFinite() || abs(periodCount) > MAX_RELIABLE_PERIOD_COUNT) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "相位过大，无法可靠地归一化正切方程的周期解",
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
                "正切方程回代时角度超出 Double 范围",
            )
        }
        val actual = tan(evaluatedAngle)
        if (!actual.isFinite()) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "正切方程候选解回代时接近定义域极点，无法可靠验证",
            )
        }
        val residual = normalizedDifference(actual, equation.target)
        if (!residual.isFinite() || residual > ROOT_RESIDUAL_TOLERANCE) {
            return normalizationFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "正切方程周期解未通过原式回代检查",
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
    ) = TangentEquationSolutionResult.Failure(
        PolynomialError(code = code, message = message),
    )

    private sealed interface FamilyNormalization {
        data class Success(val family: PeriodicSolutionFamily) : FamilyNormalization
        data class Failure(val failure: TangentEquationSolutionResult.Failure) : FamilyNormalization
    }

    private const val PI = Math.PI
    private const val HALF_PI = Math.PI / 2.0
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
    private const val MAX_RELIABLE_PERIOD_COUNT = 4_503_599_627_370_496.0
}
