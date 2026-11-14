package ci.us.zhixing.app.domain.math.model

/**
 * A sine equation of the form sin(coefficient * x + phase) = target.
 * Angles are measured in radians.
 */
internal data class SineEquation(
    val coefficient: Double,
    val phase: Double,
    val target: Double,
)
