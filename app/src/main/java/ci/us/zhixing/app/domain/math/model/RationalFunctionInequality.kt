package ci.us.zhixing.app.domain.math.model

/** A rational-function inequality of the form N(x)/D(x) comparison 0. */
internal data class RationalFunctionInequality(
    val expression: RationalFunction,
    val comparison: PolynomialComparison,
)
