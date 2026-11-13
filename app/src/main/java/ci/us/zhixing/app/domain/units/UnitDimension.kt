package ci.us.zhixing.app.domain.units

/**
 * A physical-dimension vector.
 *
 * Exponents are ordered by base dimensions rather than by display category:
 * length (L), mass (M), time (T), thermodynamic temperature (Θ), data amount
 * (D), and angle (A). Keeping angle and data as explicit dimensions prevents
 * accidental conversion to an ordinary scalar or to a physical quantity.
 */
internal data class UnitDimension(
    val lengthExponent: Int = 0,
    val massExponent: Int = 0,
    val timeExponent: Int = 0,
    val temperatureExponent: Int = 0,
    val dataExponent: Int = 0,
    val angleExponent: Int = 0,
)
