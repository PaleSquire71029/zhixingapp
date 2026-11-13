package ci.us.zhixing.app.domain.math.model

/**
 * An equation whose two sides have already been converted to polynomials.
 * The solver moves [right] to the left and solves left(x) - right(x) = 0.
 */
internal data class PolynomialEquation(
    val left: Polynomial,
    val right: Polynomial,
)
