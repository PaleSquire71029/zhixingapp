package ci.us.zhixing.app.domain.math.model

/** An equation whose sides are rational functions with preserved source domains. */
internal data class RationalEquation(
    val left: RationalFunction,
    val right: RationalFunction,
)
