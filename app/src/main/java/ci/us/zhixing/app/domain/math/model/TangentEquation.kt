package ci.us.zhixing.app.domain.math.model

/**
 * A tangent equation of the form tan(coefficient * x + phase) = target.
 * Angles are measured in radians.
 */
internal data class TangentEquation(
    val coefficient: Double,
    val phase: Double,
    val target: Double,
)
