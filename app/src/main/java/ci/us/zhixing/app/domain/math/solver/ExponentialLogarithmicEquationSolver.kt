package ci.us.zhixing.app.domain.math.solver

import ci.us.zhixing.app.domain.math.algebra.PolynomialArithmetic
import ci.us.zhixing.app.domain.math.model.EquationSolution
import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicEquation
import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicEquationSolutionResult
import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicError
import ci.us.zhixing.app.domain.math.model.ExponentialLogarithmicErrorCode
import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

/**
 * Solves bounded exponential / logarithmic equation forms using natural-log
 * transformations. The transformed linear equation only supplies candidates;
 * logarithm domains and the original logarithmic equation are rechecked.
 */
internal object ExponentialLogarithmicEquationSolver {
    fun solve(
        equation: ExponentialLogarithmicEquation,
    ): ExponentialLogarithmicEquationSolutionResult = when (equation) {
        is ExponentialLogarithmicEquation.ExponentialEqualsConstant ->
            solveExponentialEqualsConstant(equation)

        is ExponentialLogarithmicEquation.ExponentialExpressionsEqual ->
            solveExponentialExpressionsEqual(equation)

        is ExponentialLogarithmicEquation.LogarithmEqualsConstant ->
            solveLogarithmEqualsConstant(equation)

        is ExponentialLogarithmicEquation.SameBaseLogarithmsEqual ->
            solveSameBaseLogarithmsEqual(equation)
    }

    private fun solveExponentialEqualsConstant(
        equation: ExponentialLogarithmicEquation.ExponentialEqualsConstant,
    ): ExponentialLogarithmicEquationSolutionResult {
        if (equation.exponent.degree > MAX_VARIABLE_DEGREE) {
            return unsupported(equation.exponent.degree, 0)
        }
        if (!equation.base.isFinite() || !equation.value.isFinite()) {
            return failure(
                ExponentialLogarithmicErrorCode.NON_FINITE_INPUT,
                "指数方程的底数和右侧必须是有限数",
            )
        }
        if (equation.base <= 0.0) {
            return failure(
                ExponentialLogarithmicErrorCode.INVALID_BASE,
                "实数指数方程的底数必须大于零",
            )
        }
        if (equation.value <= 0.0) {
            return failure(
                ExponentialLogarithmicErrorCode.INVALID_LOGARITHM_VALUE,
                "指数式的右侧必须大于零",
            )
        }
        if (equation.base == 1.0) {
            return if (equation.value == 1.0) {
                allRealNumbers()
            } else {
                ExponentialLogarithmicEquationSolutionResult.NoSolutions
            }
        }
        if (equation.exponent.degree == 0) {
            val computed = Math.pow(equation.base, equation.exponent.coefficients[0])
            return if (computed.isFinite() && computed == equation.value) {
                allRealNumbers()
            } else {
                ExponentialLogarithmicEquationSolutionResult.NoSolutions
            }
        }

        val logBase = ln(equation.base)
        val logValue = ln(equation.value)
        val coefficients = DoubleArray(2)
        for (index in 0..1) {
            val sourceCoefficient = coefficientAt(equation.exponent, index)
            val scaledCoefficient = logBase * sourceCoefficient
            if (logBase != 0.0 && sourceCoefficient != 0.0 && scaledCoefficient == 0.0) {
                return failure(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "指数方程变换时系数下溢，无法可靠求解",
                )
            }
            coefficients[index] = scaledCoefficient
        }
        coefficients[0] -= logValue
        return solveReducedLinear(
            coefficients = coefficients,
            onAllRealNumbers = {
                failure(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "指数方程化简为近似恒等式，无法可靠确认对所有实数成立",
                )
            },
            verify = { x -> verifyExponentialEqualsConstant(equation, x) },
        )
    }

    private fun solveExponentialExpressionsEqual(
        equation: ExponentialLogarithmicEquation.ExponentialExpressionsEqual,
    ): ExponentialLogarithmicEquationSolutionResult {
        if (equation.leftExponent.degree > MAX_VARIABLE_DEGREE ||
            equation.rightExponent.degree > MAX_VARIABLE_DEGREE
        ) {
            return unsupported(
                equation.leftExponent.degree,
                equation.rightExponent.degree,
            )
        }
        if (!equation.leftBase.isFinite() || !equation.rightBase.isFinite()) {
            return failure(
                ExponentialLogarithmicErrorCode.NON_FINITE_INPUT,
                "指数方程的底数必须是有限数",
            )
        }
        if (equation.leftBase <= 0.0 || equation.rightBase <= 0.0) {
            return failure(
                ExponentialLogarithmicErrorCode.INVALID_BASE,
                "实数指数方程的底数必须大于零",
            )
        }

        val leftLogBase = ln(equation.leftBase)
        val rightLogBase = ln(equation.rightBase)
        val coefficients = DoubleArray(2)
        for (index in 0..1) {
            val leftCoefficient = coefficientAt(equation.leftExponent, index)
            val rightCoefficient = coefficientAt(equation.rightExponent, index)
            val scaledLeft = leftLogBase * leftCoefficient
            val scaledRight = rightLogBase * rightCoefficient
            if ((leftLogBase != 0.0 && leftCoefficient != 0.0 && scaledLeft == 0.0) ||
                (rightLogBase != 0.0 && rightCoefficient != 0.0 && scaledRight == 0.0)
            ) {
                return failure(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "指数方程变换时系数下溢，无法可靠求解",
                )
            }
            coefficients[index] = scaledLeft - scaledRight
        }

        return solveReducedLinear(
            coefficients = coefficients,
            onAllRealNumbers = {
                if (isProvableExponentialIdentity(equation)) {
                    allRealNumbers()
                } else {
                    failure(
                        ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                        "两个指数式的对数系数过于接近，无法可靠区分恒等式与近似恒等式",
                    )
                }
            },
            verify = { x -> verifyExponentialExpressionsEqual(equation, x) },
        )
    }

    private fun solveLogarithmEqualsConstant(
        equation: ExponentialLogarithmicEquation.LogarithmEqualsConstant,
    ): ExponentialLogarithmicEquationSolutionResult {
        if (equation.argument.degree > MAX_VARIABLE_DEGREE) {
            return unsupported(equation.argument.degree, 0)
        }
        if (!equation.base.isFinite() || !equation.value.isFinite()) {
            return failure(
                ExponentialLogarithmicErrorCode.NON_FINITE_INPUT,
                "对数方程的底数和等式右侧必须是有限数",
            )
        }
        if (equation.base <= 0.0 || equation.base == 1.0) {
            return failure(
                ExponentialLogarithmicErrorCode.INVALID_BASE,
                "对数底数必须大于零且不等于一",
            )
        }

        if (equation.argument.degree == 0) {
            val argument = equation.argument.coefficients[0]
            if (argument <= 0.0) return ExponentialLogarithmicEquationSolutionResult.NoSolutions
            val actualLog = ln(argument)
            val expectedLog = equation.value * ln(equation.base)
            if (!expectedLog.isFinite()) {
                return failure(
                    ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                    "对数方程的目标对数值超出可计算范围",
                )
            }
            val scale = maxOf(1.0, abs(actualLog), abs(expectedLog))
            val residual = abs(actualLog / scale - expectedLog / scale)
            return if (residual.isFinite() && residual <= LOG_RESIDUAL_TOLERANCE) {
                allRealNumbers()
            } else {
                ExponentialLogarithmicEquationSolutionResult.NoSolutions
            }
        }

        val targetLog = equation.value * ln(equation.base)
        if (!targetLog.isFinite()) {
            return failure(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "对数方程的目标真数超出可计算范围",
            )
        }
        val target = exp(targetLog)
        if (!target.isFinite() || target == 0.0) {
            return failure(
                ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                "对数方程的目标真数超出 Double 正数表示范围",
            )
        }

        if (targetLog != 0.0 && target == 1.0) {
            return failure(
                ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                "对数方程的目标真数过于接近 1，Double 无法可靠区分",
            )
        }

        val coefficients = doubleArrayOf(
            equation.argument.coefficients[0] - target,
            coefficientAt(equation.argument, 1),
        )
        return solveReducedLinear(
            coefficients = coefficients,
            onAllRealNumbers = {
                allRealNumbers()
            },
            verify = { x -> verifyLogarithmEqualsConstant(equation, targetLog, x) },
        )
    }

    private fun solveSameBaseLogarithmsEqual(
        equation: ExponentialLogarithmicEquation.SameBaseLogarithmsEqual,
    ): ExponentialLogarithmicEquationSolutionResult {
        if (equation.leftArgument.degree > MAX_VARIABLE_DEGREE ||
            equation.rightArgument.degree > MAX_VARIABLE_DEGREE
        ) {
            return unsupported(
                equation.leftArgument.degree,
                equation.rightArgument.degree,
            )
        }
        if (!equation.base.isFinite()) {
            return failure(
                ExponentialLogarithmicErrorCode.NON_FINITE_INPUT,
                "对数底数必须是有限数",
            )
        }
        if (equation.base <= 0.0 || equation.base == 1.0) {
            return failure(
                ExponentialLogarithmicErrorCode.INVALID_BASE,
                "对数底数必须大于零且不等于一",
            )
        }

        val coefficients = DoubleArray(2) { index ->
            coefficientAt(equation.leftArgument, index) -
                coefficientAt(equation.rightArgument, index)
        }
        return solveReducedLinear(
            coefficients = coefficients,
            onAllRealNumbers = {
                allRealNumbersInPositiveDomain(
                    equation.leftArgument,
                    equation.rightArgument,
                )
            },
            verify = { x -> verifySameBaseLogarithmsEqual(equation, x) },
            filter = { x -> bothArgumentsPositive(equation.leftArgument, equation.rightArgument, x) },
        )
    }

    private fun solveReducedLinear(
        coefficients: DoubleArray,
        onAllRealNumbers: () -> ExponentialLogarithmicEquationSolutionResult,
        verify: (Double) -> VerificationResult,
        filter: ((Double) -> Boolean)? = null,
    ): ExponentialLogarithmicEquationSolutionResult {
        val reduced = when (val normalized = normalizedLinearPolynomial(coefficients)) {
            is NormalizedPolynomialResult.Success -> normalized.polynomial
            is NormalizedPolynomialResult.Failure ->
                return ExponentialLogarithmicEquationSolutionResult.Failure(normalized.error)
        }

        return when (val roots = PolynomialArithmetic.solveRealRoots(reduced)) {
            PolynomialRootsResult.AllRealNumbers -> onAllRealNumbers()
            PolynomialRootsResult.NoRealRoots -> ExponentialLogarithmicEquationSolutionResult.NoSolutions
            is PolynomialRootsResult.UnsupportedDegree -> unsupported(roots.degree, 0)
            is PolynomialRootsResult.Failure -> failure(
                when (roots.error.code) {
                    ci.us.zhixing.app.domain.math.model.PolynomialErrorCode.NON_FINITE_INPUT ->
                        ExponentialLogarithmicErrorCode.NON_FINITE_INPUT
                    ci.us.zhixing.app.domain.math.model.PolynomialErrorCode.NON_FINITE_RESULT ->
                        ExponentialLogarithmicErrorCode.NON_FINITE_RESULT
                    ci.us.zhixing.app.domain.math.model.PolynomialErrorCode.NUMERIC_PRECISION_LOSS ->
                        ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS
                },
                roots.error.message,
            )
            is PolynomialRootsResult.FiniteRoots -> {
                val solutions = mutableListOf<EquationSolution>()
                for (root in roots.roots) {
                    if (filter != null && !filter(root.value)) continue
                    when (val checked = verify(root.value)) {
                        is VerificationResult.Success -> solutions += EquationSolution(
                            value = root.value,
                            normalizedResidual = checked.normalizedResidual,
                        )
                        is VerificationResult.Failure ->
                            return ExponentialLogarithmicEquationSolutionResult.Failure(checked.error)
                        VerificationResult.Reject -> Unit
                    }
                }
                if (solutions.isEmpty()) {
                    ExponentialLogarithmicEquationSolutionResult.NoSolutions
                } else {
                    ExponentialLogarithmicEquationSolutionResult.FiniteSolutions(
                        solutions.distinctBy { it.value }.sortedBy { it.value },
                    )
                }
            }
        }
    }

    private fun normalizedLinearPolynomial(
        coefficients: DoubleArray,
    ): NormalizedPolynomialResult {
        if (coefficients.any { !it.isFinite() }) {
            return NormalizedPolynomialResult.Failure(
                error(
                    ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                    "指数 / 对数方程变形产生非有限系数",
                ),
            )
        }
        val scale = coefficients.maxOf { abs(it) }
        if (scale == 0.0) {
            return NormalizedPolynomialResult.Success(Polynomial.constant(0.0))
        }
        val normalized = coefficients.map { it / scale }
        if (normalized.any { it != 0.0 && abs(it) < Polynomial.ZERO_TOLERANCE }) {
            return NormalizedPolynomialResult.Failure(
                error(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "指数 / 对数方程化简后的线性系数相差过大，无法可靠求根",
                ),
            )
        }
        return NormalizedPolynomialResult.Success(Polynomial.from(normalized))
    }

    private fun verifyExponentialEqualsConstant(
        equation: ExponentialLogarithmicEquation.ExponentialEqualsConstant,
        x: Double,
    ): VerificationResult {
        val exponent = evaluate(equation.exponent, x) ?: return VerificationResult.Failure(
            error(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "指数表达式求值超出有限范围",
            ),
        )
        return compareFiniteLogs(
            ln(equation.base) * exponent,
            ln(equation.value),
        )
    }

    private fun verifyExponentialExpressionsEqual(
        equation: ExponentialLogarithmicEquation.ExponentialExpressionsEqual,
        x: Double,
    ): VerificationResult {
        val leftExponent = evaluate(equation.leftExponent, x) ?: return VerificationResult.Failure(
            error(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "左侧指数表达式求值超出有限范围",
            ),
        )
        val rightExponent = evaluate(equation.rightExponent, x) ?: return VerificationResult.Failure(
            error(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "右侧指数表达式求值超出有限范围",
            ),
        )
        return compareFiniteLogs(
            ln(equation.leftBase) * leftExponent,
            ln(equation.rightBase) * rightExponent,
        )
    }

    private fun verifyLogarithmEqualsConstant(
        equation: ExponentialLogarithmicEquation.LogarithmEqualsConstant,
        targetLog: Double,
        x: Double,
    ): VerificationResult {
        val sign = PolynomialArithmetic.signAt(equation.argument, x)
        if (sign <= 0) return VerificationResult.Reject
        val argument = evaluate(equation.argument, x) ?: return VerificationResult.Failure(
            error(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "对数真数求值超出有限范围",
            ),
        )
        if (argument <= 0.0) {
            return VerificationResult.Failure(
                error(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "对数真数的精确符号与浮点求值不一致",
                ),
            )
        }
        return compareFiniteLogs(ln(argument), targetLog)
    }

    private fun verifySameBaseLogarithmsEqual(
        equation: ExponentialLogarithmicEquation.SameBaseLogarithmsEqual,
        x: Double,
    ): VerificationResult {
        val left = evaluate(equation.leftArgument, x) ?: return VerificationResult.Failure(
            error(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "左侧对数真数求值超出有限范围",
            ),
        )
        val right = evaluate(equation.rightArgument, x) ?: return VerificationResult.Failure(
            error(
                ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                "右侧对数真数求值超出有限范围",
            ),
        )
        if (left <= 0.0 || right <= 0.0) {
            return VerificationResult.Failure(
                error(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "对数真数的精确符号与浮点求值不一致",
                ),
            )
        }
        return compareFiniteLogs(ln(left), ln(right))
    }

    private fun compareFiniteLogs(
        leftLog: Double,
        rightLog: Double,
    ): VerificationResult {
        if (!leftLog.isFinite() || !rightLog.isFinite()) {
            return VerificationResult.Failure(
                error(
                    ExponentialLogarithmicErrorCode.NON_FINITE_RESULT,
                    "指数 / 对数回代产生非有限对数值",
                ),
            )
        }
        val scale = maxOf(1.0, abs(leftLog), abs(rightLog))
        val residual = abs(leftLog / scale - rightLog / scale)
        return if (residual.isFinite() && residual <= LOG_RESIDUAL_TOLERANCE) {
            VerificationResult.Success(residual)
        } else {
            VerificationResult.Failure(
                error(
                    ExponentialLogarithmicErrorCode.NUMERIC_PRECISION_LOSS,
                    "指数 / 对数方程的候选解未通过原式回代残差检查",
                ),
            )
        }
    }

    private fun allRealNumbersInPositiveDomain(
        left: Polynomial,
        right: Polynomial,
    ): ExponentialLogarithmicEquationSolutionResult {
        val conditions = mutableListOf<Polynomial>()
        for (polynomial in listOf(left, right)) {
            if (polynomial.degree == 0) {
                if (polynomial.coefficients[0] <= 0.0) {
                    return ExponentialLogarithmicEquationSolutionResult.NoSolutions
                }
            } else {
                conditions += polynomial
            }
        }
        return ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(
            conditions.distinct(),
        )
    }

    private fun bothArgumentsPositive(
        left: Polynomial,
        right: Polynomial,
        x: Double,
    ): Boolean =
        PolynomialArithmetic.signAt(left, x) > 0 &&
            PolynomialArithmetic.signAt(right, x) > 0

    private fun isProvableExponentialIdentity(
        equation: ExponentialLogarithmicEquation.ExponentialExpressionsEqual,
    ): Boolean =
        equation.leftBase == 1.0 && equation.rightBase == 1.0 ||
            equation.leftBase == equation.rightBase &&
            equation.leftExponent == equation.rightExponent ||
            equation.leftExponent.isZero() && equation.rightExponent.isZero() ||
            equation.leftBase == 1.0 && equation.rightExponent.isZero() ||
            equation.rightBase == 1.0 && equation.leftExponent.isZero()

    private fun evaluate(polynomial: Polynomial, x: Double): Double? =
        when (val result = PolynomialArithmetic.evaluate(polynomial, x)) {
            is PolynomialEvaluationResult.Success -> result.value
            is PolynomialEvaluationResult.Failure -> null
        }

    private fun coefficientAt(polynomial: Polynomial, index: Int): Double =
        polynomial.coefficients.getOrElse(index) { 0.0 }

    private fun allRealNumbers() =
        ExponentialLogarithmicEquationSolutionResult.AllRealNumbersOnDomain(emptyList())

    private fun unsupported(leftDegree: Int, rightDegree: Int) =
        ExponentialLogarithmicEquationSolutionResult.UnsupportedStructure(
            leftDegree = leftDegree,
            rightDegree = rightDegree,
        )

    private fun failure(
        code: ExponentialLogarithmicErrorCode,
        message: String,
    ) = ExponentialLogarithmicEquationSolutionResult.Failure(error(code, message))

    private fun error(
        code: ExponentialLogarithmicErrorCode,
        message: String,
    ) = ExponentialLogarithmicError(code, message)

    private sealed interface VerificationResult {
        data class Success(val normalizedResidual: Double) : VerificationResult
        data class Failure(val error: ExponentialLogarithmicError) : VerificationResult
        object Reject : VerificationResult
    }

    private sealed interface NormalizedPolynomialResult {
        data class Success(val polynomial: Polynomial) : NormalizedPolynomialResult
        data class Failure(val error: ExponentialLogarithmicError) : NormalizedPolynomialResult
    }

    private const val MAX_VARIABLE_DEGREE = 1
    private const val LOG_RESIDUAL_TOLERANCE = 1e-12
}
