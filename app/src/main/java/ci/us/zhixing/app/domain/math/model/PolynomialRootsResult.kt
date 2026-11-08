package ci.us.zhixing.app.domain.math.model

/** One real root together with its dimensionless backward-error measure. */
internal data class PolynomialRealRoot(
    val value: Double,
    val normalizedResidual: Double,
)

/**
 * Distinguishes infinitely many roots, no real roots, finite real roots,
 * and degrees that are not handled by the current solver slice.
 */
internal sealed interface PolynomialRootsResult {
    object AllRealNumbers : PolynomialRootsResult
    object NoRealRoots : PolynomialRootsResult
    data class FiniteRoots(val roots: List<PolynomialRealRoot>) : PolynomialRootsResult
    data class UnsupportedDegree(val degree: Int) : PolynomialRootsResult
    data class Failure(val error: PolynomialError) : PolynomialRootsResult
}
