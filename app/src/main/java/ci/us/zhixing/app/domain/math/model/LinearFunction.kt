package ci.us.zhixing.app.domain.math.model

/** A real linear function in slope-intercept form: f(x) = slope * x + intercept. */
internal data class LinearFunction(
    val slope: Double,
    val intercept: Double,
)
