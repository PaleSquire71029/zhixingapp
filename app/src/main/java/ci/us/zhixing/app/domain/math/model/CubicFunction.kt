package ci.us.zhixing.app.domain.math.model

/** A real cubic polynomial function: f(x) = ax^3 + bx^2 + cx + d. */
internal data class CubicFunction(
    val cubicCoefficient: Double,
    val quadraticCoefficient: Double,
    val linearCoefficient: Double,
    val intercept: Double,
)
