package ci.us.zhixing.app.domain.math.model

/** A real quadratic function y = ax^2 + bx + c. */
internal data class QuadraticFunction(
    val quadraticCoefficient: Double,
    val linearCoefficient: Double,
    val intercept: Double,
)
