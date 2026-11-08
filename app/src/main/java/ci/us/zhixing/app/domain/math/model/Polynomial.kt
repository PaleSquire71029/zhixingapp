package ci.us.zhixing.app.domain.math.model

/**
 * A real polynomial whose coefficients are stored in ascending degree order:
 * [c0, c1, c2] represents c0 + c1*x + c2*x^2.
 *
 * Instances are normalized and immutable. The zero polynomial is represented
 * by one coefficient, never by an empty list.
 */
internal class Polynomial private constructor(
    coefficients: List<Double>,
) {
    val coefficients: List<Double> = coefficients.toList()

    /** Matches the legacy engine: the zero polynomial has represented degree 0. */
    val degree: Int
        get() = coefficients.size - 1

    fun isZero(): Boolean =
        coefficients.size == 1 && kotlin.math.abs(coefficients[0]) < ZERO_TOLERANCE

    fun isConstant(): Boolean = degree == 0

    companion object {
        const val ZERO_TOLERANCE = 1e-12

        fun of(vararg coefficients: Double): Polynomial = from(coefficients.toList())

        fun from(coefficients: List<Double>): Polynomial {
            require(coefficients.all { it.isFinite() }) {
                "Polynomial coefficients must be finite"
            }

            val normalized = coefficients.toMutableList()
            while (
                normalized.size > 1 &&
                kotlin.math.abs(normalized.last()) < ZERO_TOLERANCE
            ) {
                normalized.removeAt(normalized.lastIndex)
            }
            if (normalized.isEmpty()) normalized += 0.0
            return Polynomial(normalized)
        }

        fun constant(value: Double): Polynomial = of(value)

        fun variable(): Polynomial = of(0.0, 1.0)
    }

    override fun equals(other: Any?): Boolean =
        other is Polynomial && coefficients == other.coefficients

    override fun hashCode(): Int = coefficients.hashCode()

    override fun toString(): String = "Polynomial"
}
