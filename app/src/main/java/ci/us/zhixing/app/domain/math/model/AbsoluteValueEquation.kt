package ci.us.zhixing.app.domain.math.model

/**
 * A single absolute-value equation of the form |expression(x)| = target.
 *
 * The supported slice accepts a constant target and constant, linear, or
 * quadratic inner polynomials. Higher-degree inner expressions remain explicit
 * unsupported cases until a separate solver extension is implemented.
 */
internal data class AbsoluteValueEquation(
    val expression: Polynomial,
    val target: Double,
)
