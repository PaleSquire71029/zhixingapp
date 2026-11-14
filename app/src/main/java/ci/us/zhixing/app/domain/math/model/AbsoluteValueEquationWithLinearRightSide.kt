package ci.us.zhixing.app.domain.math.model

/**
 * Absolute-value equation |P(x)| = Q(x).
 * The current solver accepts deg(P) <= 2 and deg(Q) <= 1.
 */
internal data class AbsoluteValueEquationWithLinearRightSide(
    val expression: Polynomial,
    val right: Polynomial,
)
