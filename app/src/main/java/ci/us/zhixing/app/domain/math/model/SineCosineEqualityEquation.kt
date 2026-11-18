package ci.us.zhixing.app.domain.math.model

/**
 * An equality between a sine and a cosine with linear angles:
 *
 * sin(sineCoefficient * x + sinePhase)
 *     = cos(cosineCoefficient * x + cosinePhase).
 *
 * All angles are measured in radians.
 */
internal data class SineCosineEqualityEquation(
    val sineCoefficient: Double,
    val sinePhase: Double,
    val cosineCoefficient: Double,
    val cosinePhase: Double,
)
