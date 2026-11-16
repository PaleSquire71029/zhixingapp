package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.LinearCombinationTrigonometricEquation
import ci.us.zhixing.app.domain.math.model.LinearCombinationTrigonometricEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineEquation
import ci.us.zhixing.app.domain.math.model.SineEquationSolutionResult
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.cos

/**
 * Solves A*sin(w*x+phase)+B*cos(w*x+phase)=target by the auxiliary-angle
 * identity R*sin(w*x+phase+delta), where R=hypot(A,B) and delta=atan2(B,A).
 *
 * The existing SineEquationSolver remains the sole periodic root engine.
 */
internal object LinearCombinationTrigonometricEquationSolver {
    fun solve(
        equation: LinearCombinationTrigonometricEquation,
    ): LinearCombinationTrigonometricEquationSolutionResult {
        if (!equation.sineCoefficient.isFinite() ||
            !equation.cosineCoefficient.isFinite() ||
            !equation.angularCoefficient.isFinite() ||
            !equation.phase.isFinite() ||
            !equation.target.isFinite()
        ) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "组合三角方程的系数、相位和目标值必须是有限数",
            )
        }

        val amplitude = hypot(equation.sineCoefficient, equation.cosineCoefficient)
        if (!amplitude.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "组合三角方程的振幅超出 Double 可表示范围",
            )
        }

        if (amplitude == 0.0) {
            return if (equation.target == 0.0) {
                LinearCombinationTrigonometricEquationSolutionResult.AllRealNumbers
            } else {
                LinearCombinationTrigonometricEquationSolutionResult.NoSolutions
            }
        }

        if (abs(equation.target) > amplitude) {
            return LinearCombinationTrigonometricEquationSolutionResult.NoSolutions
        }

        val normalizedTarget = equation.target / amplitude
        if (!normalizedTarget.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "组合三角方程的归一化目标值不是有限数",
            )
        }
        if (equation.target != 0.0 && normalizedTarget == 0.0) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "组合三角方程的非零目标值归一化后下溢为零",
            )
        }
        if (abs(equation.target) < amplitude && abs(normalizedTarget) == 1.0) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "组合三角方程的目标值过于接近振幅边界，无法可靠区分解族",
            )
        }

        val phaseShift = atan2(equation.cosineCoefficient, equation.sineCoefficient)
        val shiftedPhase = equation.phase + phaseShift
        if (!shiftedPhase.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "组合三角方程的辅助角相位超出 Double 可表示范围",
            )
        }
        if (phaseShift != 0.0 && shiftedPhase == equation.phase) {
            return failure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "原相位过大，辅助角平移在 Double 中丢失",
            )
        }

        return when (
            val solved = SineEquationSolver.solve(
                SineEquation(
                    coefficient = equation.angularCoefficient,
                    phase = shiftedPhase,
                    target = normalizedTarget,
                ),
            )
        ) {
            SineEquationSolutionResult.AllRealNumbers -> {
                if (originalResidual(equation, 0.0, amplitude) <= ROOT_RESIDUAL_TOLERANCE) {
                    LinearCombinationTrigonometricEquationSolutionResult.AllRealNumbers
                } else {
                    failure(
                        PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                        "组合三角方程的常数情形未通过原式检查",
                    )
                }
            }

            SineEquationSolutionResult.NoSolutions ->
                LinearCombinationTrigonometricEquationSolutionResult.NoSolutions

            is SineEquationSolutionResult.Failure ->
                LinearCombinationTrigonometricEquationSolutionResult.Failure(solved.error)

            is SineEquationSolutionResult.PeriodicSolutions -> {
                for (family in solved.families) {
                    val residual = originalResidual(equation, family.firstSolution, amplitude)
                    if (!residual.isFinite() || residual > ROOT_RESIDUAL_TOLERANCE) {
                        return failure(
                            PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                            "组合三角方程的周期解未通过原式回代检查",
                        )
                    }
                }
                LinearCombinationTrigonometricEquationSolutionResult.PeriodicSolutions(
                    families = solved.families.toList(),
                )
            }
        }
    }

    private fun originalResidual(
        equation: LinearCombinationTrigonometricEquation,
        x: Double,
        amplitude: Double,
    ): Double {
        val angle = equation.angularCoefficient * x + equation.phase
        if (!angle.isFinite()) return Double.POSITIVE_INFINITY

        val sineTerm = equation.sineCoefficient * sin(angle)
        val cosineTerm = equation.cosineCoefficient * cos(angle)
        val actual = Math.fma(equation.sineCoefficient, sin(angle), cosineTerm)
        if (!sineTerm.isFinite() || !cosineTerm.isFinite() || !actual.isFinite()) {
            return Double.POSITIVE_INFINITY
        }

        return abs(actual - equation.target) /
            max(1.0, max(amplitude, abs(equation.target)))
    }

    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = LinearCombinationTrigonometricEquationSolutionResult.Failure(
        PolynomialError(code = code, message = message),
    )

    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
}
