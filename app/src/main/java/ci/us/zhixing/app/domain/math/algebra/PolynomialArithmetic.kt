package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.max
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.PolynomialRootsResult
import ci.us.zhixing.app.domain.math.model.PolynomialRealRoot

/** Pure polynomial operations. Coefficients are stored from constant term upward. */
internal object PolynomialArithmetic {
    fun add(
        left: Polynomial,
        right: Polynomial,
    ): PolynomialOperationResult {
        val result = MutableList(maxOf(left.coefficients.size, right.coefficients.size)) { 0.0 }
        left.coefficients.forEachIndexed { index, value ->
            result[index] += value
        }
        right.coefficients.forEachIndexed { index, value ->
            result[index] += value
        }
        return build(result)
    }

    fun subtract(
        left: Polynomial,
        right: Polynomial,
    ): PolynomialOperationResult {
        val result = MutableList(maxOf(left.coefficients.size, right.coefficients.size)) { 0.0 }
        left.coefficients.forEachIndexed { index, value ->
            result[index] += value
        }
        right.coefficients.forEachIndexed { index, value ->
            result[index] -= value
        }
        return build(result)
    }

    fun multiply(
        left: Polynomial,
        right: Polynomial,
    ): PolynomialOperationResult {
        val result = MutableList(
            left.coefficients.size + right.coefficients.size - 1,
        ) { 0.0 }

        left.coefficients.forEachIndexed { leftIndex, leftValue ->
            if (leftValue != 0.0) {
                right.coefficients.forEachIndexed { rightIndex, rightValue ->
                    result[leftIndex + rightIndex] += leftValue * rightValue
                }
            }
        }
        return build(result)
    }

    fun scale(
        polynomial: Polynomial,
        factor: Double,
    ): PolynomialOperationResult {
        if (!factor.isFinite()) {
            return failure(
                PolynomialErrorCode.NON_FINITE_INPUT,
                "缩放系数必须是有限数",
            )
        }
        return build(polynomial.coefficients.map { it * factor })
    }

    /** Horner evaluation; a non-finite x or intermediate value is an explicit error. */
    fun evaluate(
        polynomial: Polynomial,
        x: Double,
    ): PolynomialEvaluationResult {
        if (!x.isFinite()) {
            return PolynomialEvaluationResult.Failure(
                PolynomialError(
                    PolynomialErrorCode.NON_FINITE_INPUT,
                    "自变量必须是有限数",
                ),
            )
        }

        var value = 0.0
        for (index in polynomial.coefficients.lastIndex downTo 0) {
            value = value * x + polynomial.coefficients[index]
            if (!value.isFinite()) {
                return PolynomialEvaluationResult.Failure(
                    PolynomialError(
                        PolynomialErrorCode.NON_FINITE_RESULT,
                        "多项式计算结果不是有限数",
                    ),
                )
            }
        }
        return PolynomialEvaluationResult.Success(value)
    }


    fun solveRealRoots(polynomial: Polynomial): PolynomialRootsResult {
        if (polynomial.isZero()) {
            return PolynomialRootsResult.AllRealNumbers
        }
        if (polynomial.degree == 0) {
            return PolynomialRootsResult.NoRealRoots
        }
        return when (polynomial.degree) {
            1 -> solveLinear(polynomial)
            2 -> solveQuadratic(polynomial)
            else -> PolynomialRootsResult.UnsupportedDegree(polynomial.degree)
        }
    }

    private fun solveLinear(polynomial: Polynomial): PolynomialRootsResult {
        val intercept = polynomial.coefficients[0]
        val slope = polynomial.coefficients[1]
        val rawRoot = -intercept / slope
        if (!rawRoot.isFinite()) {
            return rootsFailure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "一次多项式的实根超出 Double 可表示范围",
            )
        }
        if (rawRoot == 0.0 && intercept != 0.0) {
            return rootsFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "一次多项式的非零实根在 Double 中下溢为零",
            )
        }

        val root = if (rawRoot == 0.0) 0.0 else rawRoot
        val linearTerm = slope * root
        if (!linearTerm.isFinite()) {
            return rootsFailure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "一次多项式根的残差检查产生非有限值",
            )
        }

        val residualScale = max(abs(intercept), abs(linearTerm))
        val normalizedResidual = if (residualScale == 0.0) {
            0.0
        } else {
            abs(intercept / residualScale + linearTerm / residualScale)
        }
        if (!normalizedResidual.isFinite() || normalizedResidual > ROOT_RESIDUAL_TOLERANCE) {
            return rootsFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "一次多项式根未通过归一化残差检查",
            )
        }

        return PolynomialRootsResult.FiniteRoots(
            listOf(PolynomialRealRoot(root, normalizedResidual)),
        )
    }

    /**
     * Uses an exact binary-rational discriminant sign and a scaled, stable
     * q-formula. This avoids both b*b overflow and cancellation in the small
     * root of b*b - 4*a*c.
     */
    private fun solveQuadratic(polynomial: Polynomial): PolynomialRootsResult {
        val constant = polynomial.coefficients[0]
        val linear = polynomial.coefficients[1]
        val quadratic = polynomial.coefficients[2]

        val exactA = ExactDyadic.fromDouble(quadratic)
        val exactB = ExactDyadic.fromDouble(linear)
        val exactC = ExactDyadic.fromDouble(constant)
        val discriminant = exactB * exactB -
            ExactDyadic.fromDouble(4.0) * exactA * exactC

        return when (discriminant.signum()) {
            -1 -> PolynomialRootsResult.NoRealRoots
            0 -> {
                val denominator = ScaledDouble.fromDyadic(exactA) *
                    ScaledDouble.fromDouble(2.0)
                val root = ScaledDouble.fromDyadic(exactB).negate() / denominator
                finiteRoots(polynomial, listOf(root))
            }
            else -> {
                val squareRoot = ScaledDouble.fromDyadic(discriminant).sqrt()
                // q = -0.5 * (b + copySign(sqrt(discriminant), b)).
                // The terms have the same sign, avoiding catastrophic cancellation.
                val signedSquareRoot = if (linear < 0.0) squareRoot.negate() else squareRoot
                val q = (ScaledDouble.fromDyadic(exactB) + signedSquareRoot) *
                    ScaledDouble.fromDouble(-0.5)
                if (q.isZero) {
                    return rootsFailure(
                        PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                        "二次多项式的稳定求根中间值下溢为零",
                    )
                }

                val firstRoot = q / ScaledDouble.fromDyadic(exactA)
                val secondRoot = ScaledDouble.fromDyadic(exactC) / q
                finiteRoots(polynomial, listOf(firstRoot, secondRoot), requireDistinct = true)
            }
        }
    }

    private fun finiteRoots(
        polynomial: Polynomial,
        scaledRoots: List<ScaledDouble>,
        requireDistinct: Boolean = false,
    ): PolynomialRootsResult {
        val roots = mutableListOf<PolynomialRealRoot>()
        for (scaledRoot in scaledRoots) {
            val value = scaledRoot.toDouble()
            if (!value.isFinite()) {
                return rootsFailure(
                    PolynomialErrorCode.NON_FINITE_RESULT,
                    "多项式实根超出 Double 可表示范围",
                )
            }
            if (value == 0.0 && !scaledRoot.isZero) {
                return rootsFailure(
                    PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                    "非零多项式实根在 Double 中下溢为零",
                )
            }

            val normalizedResidual = normalizedResidualAt(polynomial, value)
            if (!normalizedResidual.isFinite() || normalizedResidual > ROOT_RESIDUAL_TOLERANCE) {
                return rootsFailure(
                    PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                    "多项式实根未通过归一化残差检查",
                )
            }
            roots += PolynomialRealRoot(value, normalizedResidual)
        }

        if (requireDistinct && roots.map { it.value }.distinct().size != roots.size) {
            return rootsFailure(
                PolynomialErrorCode.NUMERIC_PRECISION_LOSS,
                "两个不同实根无法在 Double 中可靠区分",
            )
        }
        return PolynomialRootsResult.FiniteRoots(roots.sortedBy { it.value })
    }

    /**
     * Computes |p(x)| / (|a*x²| + |b*x| + |c|) using exact dyadic
     * arithmetic for the already-rounded Double root. This keeps the
     * backward-error check finite even when direct polynomial evaluation
     * would overflow or underflow.
     */
    private fun normalizedResidualAt(polynomial: Polynomial, x: Double): Double {
        val coefficients = polynomial.coefficients
        val exactX = ExactDyadic.fromDouble(x)
        val quadraticTerm = ExactDyadic.fromDouble(coefficients[2]) * exactX * exactX
        val linearTerm = ExactDyadic.fromDouble(coefficients[1]) * exactX
        val constantTerm = ExactDyadic.fromDouble(coefficients[0])
        val residual = (quadraticTerm + linearTerm + constantTerm).abs()
        val scale = quadraticTerm.abs() + linearTerm.abs() + constantTerm.abs()
        return if (scale.isZero) 0.0 else residual.ratioTo(scale)
    }

    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12

    private fun rootsFailure(
        code: PolynomialErrorCode,
        message: String,
    ) = PolynomialRootsResult.Failure(PolynomialError(code, message))

    /**
     * A binary-scaled finite value: mantissa * 2^exponent, with |mantissa|
     * normalized to [0.5, 1). Intermediate exponents may exceed Double's
     * range until the final root conversion.
     */
    private data class ScaledDouble(
        val mantissa: Double,
        val exponent: Int,
    ) {
        val isZero: Boolean
            get() = mantissa == 0.0

        fun negate(): ScaledDouble = copy(mantissa = -mantissa)

        operator fun plus(other: ScaledDouble): ScaledDouble {
            if (isZero) return other
            if (other.isZero) return this
            val (larger, smaller) = if (exponent >= other.exponent) {
                this to other
            } else {
                other to this
            }
            val difference = smaller.exponent - larger.exponent
            val aligned = if (difference < -1074) {
                0.0
            } else {
                Math.scalb(smaller.mantissa, difference)
            }
            return normalized(larger.mantissa + aligned, larger.exponent)
        }

        operator fun times(other: ScaledDouble): ScaledDouble =
            normalized(mantissa * other.mantissa, exponent + other.exponent)

        operator fun div(other: ScaledDouble): ScaledDouble {
            require(!other.isZero) { "Cannot divide by a zero scaled value" }
            return normalized(mantissa / other.mantissa, exponent - other.exponent)
        }

        fun sqrt(): ScaledDouble {
            require(mantissa > 0.0) { "Square root requires a positive scaled value" }
            val oddExponent = exponent % 2 != 0
            val adjustedMantissa = if (oddExponent) mantissa * 2.0 else mantissa
            val adjustedExponent = if (oddExponent) exponent - 1 else exponent
            return normalized(kotlin.math.sqrt(adjustedMantissa), adjustedExponent / 2)
        }

        fun toDouble(): Double = Math.scalb(mantissa, exponent)

        companion object {
            private const val TWO_TO_53 = 9_007_199_254_740_992.0
            val ZERO = ScaledDouble(0.0, 0)

            fun fromDouble(value: Double): ScaledDouble =
                fromDyadic(ExactDyadic.fromDouble(value))

            fun fromDyadic(value: ExactDyadic): ScaledDouble {
                if (value.isZero) return ZERO
                val absolute = value.significand.abs()
                val bitLength = absolute.bitLength()
                val shift = max(0, bitLength - 53)
                var top = if (shift == 0) {
                    absolute.shiftLeft(53 - bitLength)
                } else {
                    absolute.shiftRight(shift)
                }
                var scaledExponent = value.exponent + bitLength

                if (shift > 0) {
                    val remainder = absolute.subtract(top.shiftLeft(shift))
                    val halfway = BigInteger.ONE.shiftLeft(shift - 1)
                    if (remainder >= halfway) top = top.add(BigInteger.ONE)
                    if (top.bitLength() > 53) {
                        top = top.shiftRight(1)
                        scaledExponent += 1
                    }
                }

                val unsignedMantissa = top.toDouble() / TWO_TO_53
                val signedMantissa = if (value.signum() < 0) {
                    -unsignedMantissa
                } else {
                    unsignedMantissa
                }
                return ScaledDouble(signedMantissa, scaledExponent)
            }

            private fun normalized(value: Double, exponent: Int): ScaledDouble {
                if (value == 0.0) return ZERO
                val adjustment = Math.getExponent(kotlin.math.abs(value)) + 1
                return ScaledDouble(
                    mantissa = Math.scalb(value, -adjustment),
                    exponent = exponent + adjustment,
                )
            }
        }
    }
    /**
     * Tests whether the polynomial is exactly zero at [x] when every finite
     * Double is interpreted as its exact IEEE-754 binary value.
     *
     * This intentionally does not use an epsilon: a tiny non-zero value is
     * still non-zero, and cancellation in Double Horner evaluation cannot
     * turn a non-zero restriction into a false domain exclusion.
     * Callers must validate [x] and run [evaluate] first so non-finite Horner
     * intermediates retain the existing typed-error behavior.
     */
    fun isExactlyZeroAt(polynomial: Polynomial, x: Double): Boolean {
        require(x.isFinite()) { "x must be finite before exact polynomial evaluation" }
        val exactX = ExactDyadic.fromDouble(x)
        var exactValue = ExactDyadic.ZERO
        for (index in polynomial.coefficients.lastIndex downTo 0) {
            exactValue = exactValue * exactX + ExactDyadic.fromDouble(polynomial.coefficients[index])
        }
        return exactValue.isZero
    }

    private const val ROOT_RESIDUAL_TOLERANCE = 1e-12

    private fun rootsFailure(
        code: PolynomialErrorCode,
        message: String,
    ) = PolynomialRootsResult.Failure(PolynomialError(code, message))

    private fun build(coefficients: List<Double>): PolynomialOperationResult {
        if (coefficients.any { !it.isFinite() }) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "多项式运算结果不是有限数",
            )
        }
        return PolynomialOperationResult.Success(Polynomial.from(coefficients))
    }

    /** Exact finite binary rational: significand * 2^exponent. */
    private data class ExactDyadic private constructor(
        val significand: BigInteger,
        val exponent: Int,
    ) {
        val isZero: Boolean
            get() = significand.signum() == 0

        fun signum(): Int = significand.signum()

        fun abs(): ExactDyadic = if (signum() < 0) -this else this

        operator fun unaryMinus(): ExactDyadic =
            ExactDyadic(significand.negate(), exponent)

        operator fun plus(other: ExactDyadic): ExactDyadic {
            if (isZero) return other
            if (other.isZero) return this
            val commonExponent = minOf(exponent, other.exponent)
            val left = significand.shiftLeft(exponent - commonExponent)
            val right = other.significand.shiftLeft(other.exponent - commonExponent)
            return normalized(left.add(right), commonExponent)
        }

        operator fun minus(other: ExactDyadic): ExactDyadic = this + (-other)

        operator fun times(other: ExactDyadic): ExactDyadic =
            normalized(significand.multiply(other.significand), exponent + other.exponent)

        fun ratioTo(denominator: ExactDyadic): Double {
            require(!denominator.isZero) { "A dyadic ratio cannot have a zero denominator" }
            if (isZero) return 0.0
            val numeratorMagnitude = significand.abs()
            val denominatorMagnitude = denominator.significand.abs()
            val numeratorShift = max(0, numeratorMagnitude.bitLength() - 53)
            val denominatorShift = max(0, denominatorMagnitude.bitLength() - 53)
            val numeratorTop = numeratorMagnitude.shiftRight(numeratorShift).toDouble()
            val denominatorTop = denominatorMagnitude.shiftRight(denominatorShift).toDouble()
            val mantissaRatio = numeratorTop / denominatorTop
            val exponentDifference = exponent - denominator.exponent +
                numeratorShift - denominatorShift
            return Math.scalb(mantissaRatio, exponentDifference)
        }

        companion object {
            val ZERO = ExactDyadic(BigInteger.ZERO, 0)

            fun fromDouble(value: Double): ExactDyadic {
                require(value.isFinite())
                val bits = java.lang.Double.doubleToRawLongBits(value)
                val exponentBits = ((bits ushr 52) and 0x7ffL).toInt()
                val fractionBits = bits and 0x000f_ffff_ffff_ffffL
                val significandBits = if (exponentBits == 0) {
                    fractionBits
                } else {
                    fractionBits or (1L shl 52)
                }
                if (significandBits == 0L) return ZERO

                val sign = if ((bits ushr 63) == 0L) BigInteger.ONE else BigInteger.ONE.negate()
                val significand = BigInteger.valueOf(significandBits).multiply(sign)
                val exponent = if (exponentBits == 0) {
                    -1074
                } else {
                    exponentBits - 1023 - 52
                }
                return normalized(significand, exponent)
            }

            private fun normalized(value: BigInteger, exponent: Int): ExactDyadic {
                if (value.signum() == 0) return ZERO
                val trailingZeroBits = value.abs().lowestSetBit
                return ExactDyadic(value.shiftRight(trailingZeroBits), exponent + trailingZeroBits)
            }
        }
    }
    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = PolynomialOperationResult.Failure(
        PolynomialError(code, message),
    )
}
