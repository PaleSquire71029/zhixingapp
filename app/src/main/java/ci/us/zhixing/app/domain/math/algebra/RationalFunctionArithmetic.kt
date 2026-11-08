package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
import ci.us.zhixing.app.domain.math.model.PolynomialError
import ci.us.zhixing.app.domain.math.model.PolynomialErrorCode
import ci.us.zhixing.app.domain.math.model.PolynomialOperationResult
import ci.us.zhixing.app.domain.math.model.RationalFunction
import ci.us.zhixing.app.domain.math.model.RationalFunctionError
import ci.us.zhixing.app.domain.math.model.RationalFunctionErrorCode
import ci.us.zhixing.app.domain.math.model.RationalFunctionOperationResult

/**
 * Rational-function operations that preserve all restrictions from the
 * original expression tree. No common-factor cancellation is performed here.
 */
internal object RationalFunctionArithmetic {
    const val MAX_EXPONENT = 64

    fun add(
        left: RationalFunction,
        right: RationalFunction,
    ): RationalFunctionOperationResult = combine(
        numerator = crossCombine(left, right, subtract = false),
        denominator = PolynomialArithmetic.multiply(left.denominator, right.denominator),
        restrictions = left.domainRestrictions + right.domainRestrictions,
    )

    fun subtract(
        left: RationalFunction,
        right: RationalFunction,
    ): RationalFunctionOperationResult = combine(
        numerator = crossCombine(left, right, subtract = true),
        denominator = PolynomialArithmetic.multiply(left.denominator, right.denominator),
        restrictions = left.domainRestrictions + right.domainRestrictions,
    )

    fun multiply(
        left: RationalFunction,
        right: RationalFunction,
    ): RationalFunctionOperationResult = combine(
        numerator = PolynomialArithmetic.multiply(left.numerator, right.numerator),
        denominator = PolynomialArithmetic.multiply(left.denominator, right.denominator),
        restrictions = left.domainRestrictions + right.domainRestrictions,
    )

    fun divide(
        left: RationalFunction,
        right: RationalFunction,
    ): RationalFunctionOperationResult {
        if (right.numerator.isZero()) {
            return failure(
                RationalFunctionErrorCode.ZERO_DIVISOR_FUNCTION,
                "不能除以恒为零的有理函数",
            )
        }

        // Dividing by n/d introduces n(x) != 0 in addition to the divisor's
        // own original restrictions and denominator exclusions.
        return combine(
            numerator = PolynomialArithmetic.multiply(left.numerator, right.denominator),
            denominator = PolynomialArithmetic.multiply(left.denominator, right.numerator),
            restrictions = left.domainRestrictions +
                right.domainRestrictions +
                right.numerator,
        )
    }

    /**
     * Raises a rational function to a non-negative integer power.
     *
     * The original restrictions survive even for exponent 0, so
     * (1/x)^0 is represented as 1/1 while still excluding x = 0.
     */
    fun power(
        value: RationalFunction,
        exponent: Int,
    ): RationalFunctionOperationResult {
        if (exponent !in 0..MAX_EXPONENT) {
            return failure(
                RationalFunctionErrorCode.INVALID_EXPONENT,
                "有理式指数必须是 0 到 $MAX_EXPONENT 之间的整数",
            )
        }

        return combine(
            numerator = polynomialPower(value.numerator, exponent),
            denominator = polynomialPower(value.denominator, exponent),
            restrictions = value.domainRestrictions,
        )
    }

    private fun crossCombine(
        left: RationalFunction,
        right: RationalFunction,
        subtract: Boolean,
    ): PolynomialOperationResult {
        val leftProduct = PolynomialArithmetic.multiply(left.numerator, right.denominator)
        if (leftProduct is PolynomialOperationResult.Failure) return leftProduct

        val rightProduct = PolynomialArithmetic.multiply(right.numerator, left.denominator)
        if (rightProduct is PolynomialOperationResult.Failure) return rightProduct

        val leftPolynomial = (leftProduct as PolynomialOperationResult.Success).polynomial
        val rightPolynomial = (rightProduct as PolynomialOperationResult.Success).polynomial
        return if (subtract) {
            PolynomialArithmetic.subtract(leftPolynomial, rightPolynomial)
        } else {
            PolynomialArithmetic.add(leftPolynomial, rightPolynomial)
        }
    }

    private fun polynomialPower(
        polynomial: Polynomial,
        exponent: Int,
    ): PolynomialOperationResult {
        var result = Polynomial.constant(1.0)
        var factor = polynomial
        var remaining = exponent

        // Exponentiation by squaring bounds the number of polynomial
        // multiplications and propagates overflow as a typed failure.
        while (remaining > 0) {
            if ((remaining and 1) == 1) {
                when (val multiplied = PolynomialArithmetic.multiply(result, factor)) {
                    is PolynomialOperationResult.Success -> result = multiplied.polynomial
                    is PolynomialOperationResult.Failure -> return multiplied
                }
            }

            remaining = remaining ushr 1
            if (remaining > 0) {
                when (val squared = PolynomialArithmetic.multiply(factor, factor)) {
                    is PolynomialOperationResult.Success -> factor = squared.polynomial
                    is PolynomialOperationResult.Failure -> return squared
                }
            }
        }
        return PolynomialOperationResult.Success(result)
    }

    private fun combine(
        numerator: PolynomialOperationResult,
        denominator: PolynomialOperationResult,
        restrictions: List<Polynomial>,
    ): RationalFunctionOperationResult {
        val n = when (numerator) {
            is PolynomialOperationResult.Success -> numerator.polynomial
            is PolynomialOperationResult.Failure -> return mapFailure(numerator.error)
        }
        val d = when (denominator) {
            is PolynomialOperationResult.Success -> denominator.polynomial
            is PolynomialOperationResult.Failure -> return mapFailure(denominator.error)
        }
        return RationalFunction.create(
            numerator = n,
            denominator = d,
            domainRestrictions = restrictions,
        )
    }

    private fun mapFailure(error: PolynomialError): RationalFunctionOperationResult.Failure {
        val code = when (error.code) {
            PolynomialErrorCode.NON_FINITE_INPUT -> RationalFunctionErrorCode.NON_FINITE_INPUT
            PolynomialErrorCode.NON_FINITE_RESULT -> RationalFunctionErrorCode.NON_FINITE_RESULT
        }
        return RationalFunctionOperationResult.Failure(RationalFunctionError(code, error.message))
    }

    private fun failure(
        code: RationalFunctionErrorCode,
        message: String,
    ) = RationalFunctionOperationResult.Failure(
        RationalFunctionError(code, message),
    )
}
