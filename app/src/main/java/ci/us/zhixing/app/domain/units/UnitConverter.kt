package ci.us.zhixing.app.domain.units

import java.math.BigDecimal
import java.math.MathContext

/** Unified typed result for both linear and affine unit conversion. */
internal sealed interface UnitConversionResult {
    data class Success(val value: Double) : UnitConversionResult

    data class Failure(
        val error: UnitConversionError,
    ) : UnitConversionResult
}

/** Stable error model for category lookup, dimension checks, and numeric safety. */
internal sealed interface UnitConversionError {
    data object NonFiniteInput : UnitConversionError
    data class UnknownSourceCategory(val categoryId: String) : UnitConversionError
    data class UnknownTargetCategory(val categoryId: String) : UnitConversionError
    data class IncompatibleDimensions(
        val sourceCategoryId: String,
        val targetCategoryId: String,
    ) : UnitConversionError
    data class CategoryMismatch(
        val sourceCategoryId: String,
        val targetCategoryId: String,
    ) : UnitConversionError
    data class UnsupportedCategory(val categoryId: String) : UnitConversionError
    data class UnknownSourceUnit(
        val categoryId: String,
        val unitId: String,
    ) : UnitConversionError
    data class UnknownTargetUnit(
        val categoryId: String,
        val unitId: String,
    ) : UnitConversionError
    data object BelowAbsoluteZero : UnitConversionError
    data object NonFiniteIntermediateValue : UnitConversionError
    data object NonFiniteResult : UnitConversionError
    data object PrecisionUnderflow : UnitConversionError
}

/**
 * Converts values only within the same unit category.
 *
 * Linear categories use BigDecimal intermediates so an oversized temporary
 * base-unit value does not incorrectly fail when the final converted value
 * is representable. Temperature delegates to its affine Kelvin-based model.
 */
internal object UnitConverter {
    private val conversionMathContext = MathContext.DECIMAL128

    fun convert(
        value: Double,
        sourceCategoryId: String,
        sourceUnitId: String,
        targetCategoryId: String,
        targetUnitId: String,
    ): UnitConversionResult {
        if (!value.isFinite()) {
            return failure(UnitConversionError.NonFiniteInput)
        }

        val sourceCategory = UnitCategoryCatalog.findById(sourceCategoryId)
            ?: return failure(UnitConversionError.UnknownSourceCategory(sourceCategoryId))
        val targetCategory = UnitCategoryCatalog.findById(targetCategoryId)
            ?: return failure(UnitConversionError.UnknownTargetCategory(targetCategoryId))

        if (!UnitCategoryCatalog.areDimensionallyCompatible(sourceCategory, targetCategory)) {
            return failure(
                UnitConversionError.IncompatibleDimensions(
                    sourceCategoryId = sourceCategoryId,
                    targetCategoryId = targetCategoryId,
                ),
            )
        }
        if (sourceCategory != targetCategory) {
            return failure(
                UnitConversionError.CategoryMismatch(
                    sourceCategoryId = sourceCategoryId,
                    targetCategoryId = targetCategoryId,
                ),
            )
        }

        return when (sourceCategory.conversionKind) {
            UnitConversionKind.LINEAR -> convertLinear(
                value = value,
                category = sourceCategory,
                sourceUnitId = sourceUnitId,
                targetUnitId = targetUnitId,
            )
            UnitConversionKind.AFFINE -> convertAffine(
                value = value,
                category = sourceCategory,
                sourceUnitId = sourceUnitId,
                targetUnitId = targetUnitId,
            )
        }
    }

    private fun convertLinear(
        value: Double,
        category: UnitCategory,
        sourceUnitId: String,
        targetUnitId: String,
    ): UnitConversionResult {
        val unitSet = UnitDefinitionCatalog.forCategory(category)
            ?: return failure(UnitConversionError.UnsupportedCategory(category.id))

        val source = unitSet.findUnit(sourceUnitId)
            ?: return failure(UnitConversionError.UnknownSourceUnit(category.id, sourceUnitId))
        val target = unitSet.findUnit(targetUnitId)
            ?: return failure(UnitConversionError.UnknownTargetUnit(category.id, targetUnitId))

        val exactDecimalResult = BigDecimal.valueOf(value)
            .multiply(BigDecimal.valueOf(source.scaleToBase), conversionMathContext)
            .divide(BigDecimal.valueOf(target.scaleToBase), conversionMathContext)

        val result = exactDecimalResult.toDouble()
        if (!result.isFinite()) {
            return failure(UnitConversionError.NonFiniteResult)
        }
        if (result == 0.0 && exactDecimalResult.signum() != 0) {
            return failure(UnitConversionError.PrecisionUnderflow)
        }

        return UnitConversionResult.Success(result)
    }

    private fun convertAffine(
        value: Double,
        category: UnitCategory,
        sourceUnitId: String,
        targetUnitId: String,
    ): UnitConversionResult {
        if (category != UnitCategory.TEMPERATURE) {
            return failure(UnitConversionError.UnsupportedCategory(category.id))
        }

        return when (
            val result = TemperatureConverter.convert(
                value = value,
                fromUnitId = sourceUnitId,
                toUnitId = targetUnitId,
            )
        ) {
            is TemperatureConversionResult.Success ->
                UnitConversionResult.Success(result.value)
            is TemperatureConversionResult.Failure -> {
                val error = when (val temperatureError = result.error) {
                    TemperatureConversionError.NonFiniteInput ->
                        UnitConversionError.NonFiniteInput
                    TemperatureConversionError.BelowAbsoluteZero ->
                        UnitConversionError.BelowAbsoluteZero
                    TemperatureConversionError.NonFiniteIntermediateValue ->
                        UnitConversionError.NonFiniteIntermediateValue
                    TemperatureConversionError.NonFiniteResult ->
                        UnitConversionError.NonFiniteResult
                    is TemperatureConversionError.UnknownSourceUnit ->
                        UnitConversionError.UnknownSourceUnit(category.id, temperatureError.unitId)
                    is TemperatureConversionError.UnknownTargetUnit ->
                        UnitConversionError.UnknownTargetUnit(category.id, temperatureError.unitId)
                }
                failure(error)
            }
        }
    }

    private fun failure(error: UnitConversionError) =
        UnitConversionResult.Failure(error)
}
