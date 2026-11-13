package ci.us.zhixing.app.domain.math.model

/**
 * A single absolute-value equation of the form |expression(x)| = target.
 *
 * This initial slice intentionally accepts a constant target and supports
 * constant or linear inner polynomials only.
 */
internal data class AbsoluteValueEquation(
    val expression: Polynomial,
    val target: Double,
)
