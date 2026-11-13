package ci.us.zhixing.app.domain.math.model

/**
 * An inverse-proportion function f(x) = k / x.
 *
 * Its real domain excludes x = 0, regardless of whether k is zero.
 */
internal data class InverseProportionFunction(
    val constant: Double,
)
