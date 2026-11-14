package ci.us.zhixing.app.domain.math.model

/**
 * A cosine equation of the form cos(coefficient * x + phase) = target.
 * Angles are measured in radians.
 */
internal data class CosineEquation(
    val coefficient: Double,
    val phase: Double,
    val target: Double,
)
