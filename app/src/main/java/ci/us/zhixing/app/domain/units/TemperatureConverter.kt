package ci.us.zhixing.app.domain.units

/** A typed outcome from converting between two temperature units. */
internal sealed interface TemperatureConversionResult {
    data class Success(val value: Double) : TemperatureConversionResult

    data class Failure(
        val error: TemperatureConversionError,
    ) : TemperatureConversionResult
}

/** Expected input, unit lookup, and numeric failures from temperature conversion. */
internal sealed interface TemperatureConversionError {
    data object NonFiniteInput : TemperatureConversionError
    data object BelowAbsoluteZero : TemperatureConversionError
    data object NonFiniteResult : TemperatureConversionError
    data class UnknownSourceUnit(val unitId: String) : TemperatureConversionError
    data class UnknownTargetUnit(val unitId: String) : TemperatureConversionError
}

/**
 * Temperature conversion through kelvin, with absolute-zero and finite-value
 * checks. This converter deliberately does not accept linear unit categories.
 */
internal object TemperatureConverter {
    fun convert(
        value: Double,
        fromUnitId: String,
        toUnitId: String,
    ): TemperatureConversionResult {
        if (!value.isFinite()) {
            return TemperatureConversionResult.Failure(
                TemperatureConversionError.NonFiniteInput,
            )
        }

        val source = TemperatureUnitCatalog.find(fromUnitId)
            ?: return TemperatureConversionResult.Failure(
                TemperatureConversionError.UnknownSourceUnit(fromUnitId),
            )
        val target = TemperatureUnitCatalog.find(toUnitId)
            ?: return TemperatureConversionResult.Failure(
                TemperatureConversionError.UnknownTargetUnit(toUnitId),
            )

        val kelvin = source.toBase(value)
        if (!kelvin.isFinite()) {
            return TemperatureConversionResult.Failure(
                TemperatureConversionError.NonFiniteResult,
            )
        }
        if (kelvin < 0.0) {
            return TemperatureConversionResult.Failure(
                TemperatureConversionError.BelowAbsoluteZero,
            )
        }

        val converted = target.fromBase(kelvin)
        if (!converted.isFinite()) {
            return TemperatureConversionResult.Failure(
                TemperatureConversionError.NonFiniteResult,
            )
        }

        return TemperatureConversionResult.Success(converted)
    }
}
