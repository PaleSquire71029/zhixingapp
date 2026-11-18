package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.SineCosineEqualityEquation
import ci.us.zhixing.app.domain.math.model.SineCosineEqualityEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.SineEqualityEquation
import ci.us.zhixing.app.domain.math.model.SineEqualityEquationSolutionResult
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Solves sin(a*x+b)=cos(c*x+d) by using
 *
 * cos(V) = sin(π/2 - V)
 *
 * and delegating the resulting sine equality to SineEqualityEquationSolver.
 * Every returned identity or periodic family is checked against the original
 * sine-cosine equation before it is exposed.
 */
internal object SineCosineEqualityEquationSolver {
    fun solve(
        equation: SineCosineEqualityEquation,
    ): SineCosineEqualityEquationSolutionResult {
        if (!equation.sineCoefficient.isFinite() ||
            !equation.sinePhase.isFinite() ||
            !equation.cosineCoefficient.isFinite() ||
            !equation.cosinePhase.isFinite()
        ) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "正弦—余弦等式的角系数和相位必须是有限数",
            )
        }

        val transformedCosinePhase = HALF_PI - equation.cosinePhase
        if (!transformedCosinePhase.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "余弦相位转换后超出 Double 可表示范围",
            )
        }

        // Reconstruct the applied π/2 shift. For large finite phases the
        // subtraction may round away part (or all) of π/2 without overflowing.
        val reconstructedShift = transformedCosinePhase + equation.cosinePhase
        if (!reconstructedShift.isFinite() ||
            abs(reconstructedShift - HALF_PI) > PHASE_SHIFT_TOLERANCE
        ) {
            return precisionFailure("余弦相位过大，无法可靠保留 π/2 转换")
        }

        val transformed = SineEqualityEquation(
            leftCoefficient = equation.sineCoefficient,
            leftPhase = equation.sinePhase,
            rightCoefficient = -equation.cosineCoefficient,
            rightPhase = transformedCosinePhase,
        )

        return when (val solved = SineEqualityEquationSolver.solve(transformed)) {
            SineEqualityEquationSolutionResult.AllRealNumbers ->
                validateIdentity(equation)

            SineEqualityEquationSolutionResult.NoSolutions ->
                SineCosineEqualityEquationSolutionResult.NoSolutions

            is SineEqualityEquationSolutionResult.PeriodicSolutions ->
                validateFamilies(equation, solved.families)

            is SineEqualityEquationSolutionResult.Failure ->
                SineCosineEqualityEquationSolutionResult.Failure(solved.error)
        }
    }

    private fun validateIdentity(
        equation: SineCosineEqualityEquation,
    ): SineCosineEqualityEquationSolutionResult {
        // Check multiple finite points as a guard against a phase conversion
        // that is technically representable but fails the original equation.
        for (x in IDENTITY_CHECK_POINTS) {
            val residual = originalResidual(equation, x)
                ?: return failure(
                    PolynomialErrorCode.NON_FINITE_RESULT,
                    "恒等式回代时角度超出 Double 可表示范围",
                )
            if (residual > ROOT_RESIDUAL_TOLERANCE) {
                return precisionFailure("正弦—余弦恒等分支未通过原式回代检查")
            }
        }
        return SineCosineEqualityEquationSolutionResult.AllRealNumbers
    }

    private fun validateFamilies(
        equation: SineCosineEqualityEquation,
        families: List<ci.us.zhixing.app.domain.math.model.PeriodicSolutionFamily>,
    ): SineCosineEqualityEquationSolutionResult {
        for (family in families) {
            val residual = originalResidual(equation, family.firstSolution)
                ?: return failure(
                    PolynomialErrorCode.NON_FINITE_RESULT,
                    "正弦—余弦周期解回代时角度超出 Double 可表示范围",
                )
            if (residual > ROOT_RESIDUAL_TOLERANCE) {
                return precisionFailure("正弦—余弦周期解未通过原式回代检查")
            }
        }
        return SineCosineEqualityEquationSolutionResult.PeriodicSolutions(families)
    }

    /**
     * Returns null when either original linear angle is not finite.
     * Sine and cosine are bounded, so their absolute difference is a
     * normalized residual with a natural scale of one.
     */
    private fun originalResidual(
        equation: SineCosineEqualityEquation,
        x: Double,
    ): Double? {
        val sineAngle = Math.fma(equation.sineCoefficient, x, equation.sinePhase)
        val cosineAngle = Math.fma(equation.cosineCoefficient, x, equation.cosinePhase)
        if (!sineAngle.isFinite() || !cosineAngle.isFinite()) return null

        val sineValue = sin(sineAngle)
        val cosineValue = cos(cosineAngle)
        if (!sineValue.isFinite() || !cosineValue.isFinite()) return null
        return abs(sineValue - cosineValue)
    }

    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = SineCosineEqualityEquationSolutionResult.Failure(
        PolynomialError(code = code, message = message),
    )

    private fun precisionFailure(message: String) =
        failure(PolynomialErrorCode.NUMERIC_PRECISION_LOSS, message)

    private const val HALF_PI = Math.PI / 2.0
    private const val PHASE_SHIFT_TOLERANCE = 1e-12
    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12

    private val IDENTITY_CHECK_POINTS = doubleArrayOf(-1.0, 0.0, 1.0)
}
