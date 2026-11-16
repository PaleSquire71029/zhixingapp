package ci.us.zhixing.app.domain.math.model

/**
 * A linear combination of sine and cosine with a shared linear angle:
 *
 * A*sin(angularCoefficient*x + phase)
 *     + B*cos(angularCoefficient*x + phase) = target.
 *
 * All angles are measured in radians.
 */
internal data class LinearCombinationTrigonometricEquation(
    val sineCoefficient: Double,
    val cosineCoefficient: Double,
    val angularCoefficient: Double,
    val phase: Double,
    val target: Double,
)
