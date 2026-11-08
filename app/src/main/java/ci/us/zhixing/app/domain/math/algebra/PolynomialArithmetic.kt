package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
import java.math.BigInteger
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.PolynomialEvaluationResult
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult

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

        operator fun plus(other: ExactDyadic): ExactDyadic {
            if (isZero) return other
            if (other.isZero) return this
            val commonExponent = minOf(exponent, other.exponent)
            val left = significand.shiftLeft(exponent - commonExponent)
            val right = other.significand.shiftLeft(other.exponent - commonExponent)
            return normalized(left.add(right), commonExponent)
        }

        operator fun times(other: ExactDyadic): ExactDyadic =
            normalized(significand.multiply(other.significand), exponent + other.exponent)

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
