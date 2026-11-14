package ci.us.zhixing.app.domain.math.model

/** A real cube-root equation of the form cubert(P(x)) = Q(x). */
internal data class CubeRootEquation(
    val radicand: Polynomial,
    val right: Polynomial,
)
