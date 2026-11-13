package ci.us.zhixing.app.domain.math.model

/** The comparison applied to a polynomial expression and zero. */
internal enum class PolynomialComparison {
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
}

/** A real polynomial inequality of the form P(x) comparison 0. */
internal data class PolynomialInequality(
    val expression: Polynomial,
    val comparison: PolynomialComparison,
)
