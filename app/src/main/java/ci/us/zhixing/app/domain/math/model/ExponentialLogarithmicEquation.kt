package ci.us.zhixing.app.domain.math.model

/**
 * Supported exponential and logarithmic equation forms.
 *
 * All variable exponent / argument polynomials are limited to degree one in
 * this slice. Exponential bases must be positive; logarithm bases must be
 * positive and different from one.
 */
internal sealed interface ExponentialLogarithmicEquation {
    /** a ^ P(x) = c, with c > 0. */
    data class ExponentialEqualsConstant(
        val base: Double,
        val exponent: Polynomial,
        val value: Double,
    ) : ExponentialLogarithmicEquation

    /** a ^ P(x) = b ^ Q(x). */
    data class ExponentialExpressionsEqual(
        val leftBase: Double,
        val leftExponent: Polynomial,
        val rightBase: Double,
        val rightExponent: Polynomial,
    ) : ExponentialLogarithmicEquation

    /** log_a(P(x)) = c. */
    data class LogarithmEqualsConstant(
        val base: Double,
        val argument: Polynomial,
        val value: Double,
    ) : ExponentialLogarithmicEquation

    /** log_a(P(x)) = log_a(Q(x)); both arguments must be strictly positive. */
    data class SameBaseLogarithmsEqual(
        val base: Double,
        val leftArgument: Polynomial,
        val rightArgument: Polynomial,
    ) : ExponentialLogarithmicEquation
}
