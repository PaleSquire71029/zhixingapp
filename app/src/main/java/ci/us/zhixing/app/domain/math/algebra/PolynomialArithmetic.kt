package ci.us.zhixing.app.domain.math.algebra

import ci.us.zhixing.app.domain.math.model.Polynomial
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

    private fun build(coefficients: List<Double>): PolynomialOperationResult {
        if (coefficients.any { !it.isFinite() }) {
            return failure(
                PolynomialErrorCode.NON_FINITE_RESULT,
                "多项式运算结果不是有限数",
            )
        }
        return PolynomialOperationResult.Success(Polynomial.from(coefficients))
    }

    private fun failure(
        code: PolynomialErrorCode,
        message: String,
    ) = PolynomialOperationResult.Failure(
        PolynomialError(code, message),
    )
}
