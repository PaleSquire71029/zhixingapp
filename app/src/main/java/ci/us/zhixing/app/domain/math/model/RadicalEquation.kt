package ci.us.zhixing.app.domain.math.model

/**
 * A square-root equation of the form sqrt(radicand(x)) = right(x).
 *
 * The current solver slice supports radicands through degree two and right
 * expressions through degree one.
 */
internal data class RadicalEquation(
    val radicand: Polynomial,
    val right: Polynomial,
)
