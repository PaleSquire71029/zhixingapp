package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineEquation
import ci.us.zhixing.app.domain.math.model.SineEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.SineEqualityEquation
import ci.us.zhixing.app.domain.math.model.SineEqualityEquationSolutionResult
import kotlin.math.abs

/**
 * Solves sin(a*x+b)=sin(c*x+d) using
 *
 * sin(U)=sin(V) iff U-V=2kπ or U+V=(2k+1)π.
 *
 * Both branches are reduced to sin(linearAngle)=0 and delegated to the
 * existing SineEquationSolver. The returned families are checked against
 * the original equality before they are exposed.
 */
internal object SineEqualityEquationSolver {
    fun solve(
        equation: SineEqualityEquation,
    ): SineEqualityEquationSolutionResult {
        if (!equation.leftCoefficient.isFinite() ||
            !equation.leftPhase.isFinite() ||
            !equation.rightCoefficient.isFinite() ||
            !equation.rightPhase.isFinite()
        ) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "正弦等式两侧的角系数和相位必须是有限数",
            )
        }

        // A branch may be numerically ambiguous while the other branch proves
        // an identity. Keep branch-local failures until both are classified.
        val differenceBranch = solveDifferenceBranch(equation)
        val sumBranch = solveSumBranch(equation)

        if (differenceBranch === BranchResult.AllRealNumbers ||
            sumBranch === BranchResult.AllRealNumbers
        ) {
            val residual = originalResidual(equation, 0.0)
                ?: return failure(
                    PolynomialErrorCode.NON_FINITE_RESULT,
                    "正弦等式恒等分支的原式回代产生非有限角度",
                )
            return if (residual <= ROOT_RESIDUAL_TOLERANCE) {
                SineEqualityEquationSolutionResult.AllRealNumbers
            } else {
                precisionFailure("正弦等式恒等分支未通过原式检查")
            }
        }

        val branchFailure = listOf(differenceBranch, sumBranch)
            .filterIsInstance<BranchResult.Failure>()
            .firstOrNull()
        if (branchFailure != null) {
            return SineEqualityEquationSolutionResult.Failure(branchFailure.error)
        }

        val families = buildList {
            (differenceBranch as? BranchResult.Periodic)?.let { addAll(it.families) }
            (sumBranch as? BranchResult.Periodic)?.let { addAll(it.families) }
        }.distinct().sortedWith(
            compareBy<PeriodicSolutionFamily> { it.firstSolution }
                .thenBy { it.period },
        )

        for (family in families) {
            val residual = originalResidual(equation, family.firstSolution)
                ?: return failure(
                    PolynomialErrorCode.NON_FINITE_RESULT,
                    "正弦等式周期解回代产生非有限角度",
                )
            if (residual > ROOT_RESIDUAL_TOLERANCE) {
                return precisionFailure("正弦等式周期解未通过原式回代检查")
            }
        }

        return if (families.isEmpty()) {
            SineEqualityEquationSolutionResult.NoSolutions
        } else {
            SineEqualityEquationSolutionResult.PeriodicSolutions(families)
        }
    }

    private fun solveDifferenceBranch(equation: SineEqualityEquation): BranchResult {
        val coefficient = halfDifference(
            equation.leftCoefficient,
            equation.rightCoefficient,
        ) ?: return BranchResult.Failure(
            PolynomialError(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "角系数之差的一半无法在 Double 中可靠表示",
            ),
        )
        val phase = halfDifference(
            equation.leftPhase,
            equation.rightPhase,
        ) ?: return BranchResult.Failure(
            PolynomialError(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "相位之差的一半无法在 Double 中可靠表示",
            ),
        )
        return solveZeroSineBranch(coefficient, phase)
    }

    private fun solveSumBranch(equation: SineEqualityEquation): BranchResult {
        val coefficient = halfSum(
            equation.leftCoefficient,
            equation.rightCoefficient,
        ) ?: return BranchResult.Failure(
            PolynomialError(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "角系数之和的一半无法在 Double 中可靠表示",
            ),
        )
        val halfPhaseSum = halfSum(
            equation.leftPhase,
            equation.rightPhase,
        ) ?: return BranchResult.Failure(
            PolynomialError(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "相位之和的一半无法在 Double 中可靠表示",
            ),
        )

        val phase = halfPhaseSum - HALF_PI
        if (!phase.isFinite()) {
            return BranchResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NON_FINITE_RESULT,
                    "正弦等式第二分支的相位超出 Double 可表示范围",
                ),
            )
        }
        if (halfPhaseSum != 0.0 && phase == halfPhaseSum) {
            return BranchResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                    "第二分支减去 π/2 时相位精度丢失",
                ),
            )
        }
        return solveZeroSineBranch(coefficient, phase)
    }

    private fun solveZeroSineBranch(
        coefficient: Double,
        phase: Double,
    ): BranchResult {
        if (coefficient == 0.0) return classifyConstantBranch(phase)

        // SineEquationSolver uses both 0 and π branches for target zero.
        // If π is swallowed by the phase magnitude, those two families can
        // collapse into one and silently omit half of the solutions.
        if (phase != 0.0 && PI - phase == -phase) {
            return BranchResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                    "相位过大，无法区分正弦零点的两个交错周期族",
                ),
            )
        }

        return when (
            val solved = SineEquationSolver.solve(
                SineEquation(
                    coefficient = coefficient,
                    phase = phase,
                    target = 0.0,
                ),
            )
        ) {
            SineEquationSolutionResult.AllRealNumbers -> BranchResult.AllRealNumbers
            SineEquationSolutionResult.NoSolutions -> BranchResult.NoSolutions
            is SineEquationSolutionResult.Failure -> BranchResult.Failure(solved.error)
            is SineEquationSolutionResult.PeriodicSolutions ->
                BranchResult.Periodic(solved.families)
        }
    }

    private fun classifyConstantBranch(phase: Double): BranchResult {
        val turns = phase / PI
        if (!turns.isFinite() || abs(turns) >= MAX_RELIABLE_INTEGER) {
            return BranchResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                    "常数相位过大，无法可靠判断其是否为 π 的整数倍",
                ),
            )
        }

        val distanceToInteger = abs(turns - Math.rint(turns))
        return when {
            distanceToInteger == 0.0 -> BranchResult.AllRealNumbers
            distanceToInteger <= CONSTANT_PHASE_TOLERANCE -> BranchResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                    "常数相位过于接近 π 的整数倍，无法可靠分类",
                ),
            )
            else -> BranchResult.NoSolutions
        }
    }

    private fun halfDifference(first: Double, second: Double): Double? {
        val difference = first - second
        val half = if (difference.isFinite()) {
            difference / 2.0
        } else {
            first / 2.0 - second / 2.0
        }
        return half.takeIf { it.isFinite() && (first == second || it != 0.0) }
    }

    private fun halfSum(first: Double, second: Double): Double? {
        val sum = first + second
        val half = if (sum.isFinite()) {
            sum / 2.0
        } else {
            first / 2.0 + second / 2.0
        }
        return half.takeIf { it.isFinite() && (sum == 0.0 || it != 0.0) }
    }

    /**
     * Returns null when evaluating either original angle exceeds Double.
     * Sine values are bounded, so their absolute difference is already a
     * normalized residual with scale max(1, |sin(left)|, |sin(right)|)=1.
     */
    private fun originalResidual(
        equation: SineEqualityEquation,
        x: Double,
    ): Double? {
        val leftAngle = Math.fma(equation.leftCoefficient, x, equation.leftPhase)
        val rightAngle = Math.fma(equation.rightCoefficient, x, equation.rightPhase)
        if (!leftAngle.isFinite() || !rightAngle.isFinite()) return null

        val leftValue = kotlin.math.sin(leftAngle)
        val rightValue = kotlin.math.sin(rightAngle)
        if (!leftValue.isFinite() || !rightValue.isFinite()) return null
        return abs(leftValue - rightValue)
    }

    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = SineEqualityEquationSolutionResult.Failure(
        PolynomialError(code = code, message = message),
    )

    private fun precisionFailure(message: String) =
        failure(PolynomialErrorCode.NUMERIC_PRECISION_LOSS, message)

    private sealed interface BranchResult {
        object AllRealNumbers : BranchResult
        object NoSolutions : BranchResult
        data class Periodic(val families: List<PeriodicSolutionFamily>) : BranchResult
        data class Failure(val error: PolynomialError) : BranchResult
    }

    private const val PI = Math.PI
    private const val HALF_PI = Math.PI / 2.0
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12
    private const val CONSTANT_PHASE_TOLERANCE = 1e-12
    private const val MAX_RELIABLE_INTEGER = 4_503_599_627_370_496.0
}
