package ci.us.zhixing.app.domain.math.model

/**
 * An equality of two sine functions with linear angles:
 *
 * sin(leftCoefficient * x + leftPhase)
 *     = sin(rightCoefficient * x + rightPhase).
 *
 * All angles are measured in radians.
 */
internal data class SineEqualityEquation(
    val leftCoefficient: Double,
    val leftPhase: Double,
    val rightCoefficient: Double,
    val rightPhase: Double,
)
