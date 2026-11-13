package ci.us.zhixing.app.ui.units

import ci.us.zhixing.app.domain.units.TemperatureUnitCatalog
import ci.us.zhixing.app.domain.units.UnitCategory
import ci.us.zhixing.app.domain.units.UnitCategoryCatalog
import ci.us.zhixing.app.domain.units.UnitConversionError
import ci.us.zhixing.app.domain.units.UnitConversionResult
import ci.us.zhixing.app.domain.units.UnitConverter
import ci.us.zhixing.app.domain.units.UnitDefinitionCatalog
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

internal data class UnitOption(
    val id: String,
    val label: String,
)

internal data class UnitConversionRow(
    val unit: UnitOption,
    val result: UnitConversionResult,
)

internal sealed interface UnitConversionPresentation {
    data object EmptyInput : UnitConversionPresentation
    data object InvalidInput : UnitConversionPresentation
    data class Failure(val error: UnitConversionError) : UnitConversionPresentation
    data class Converted(
        val value: Double,
        val rows: List<UnitConversionRow>,
    ) : UnitConversionPresentation
}

/**
 * Pure presentation adapter for the native unit conversion screen.
 *
 * It owns no conversion formulas: every visible value, including the
 * same-category overview, is produced by the single UnitConverter domain API.
 */
internal object UnitConversionPresenter {
    fun categories(): List<UnitCategory> = UnitCategoryCatalog.all

    fun units(categoryId: String): List<UnitOption> {
        val category = UnitCategoryCatalog.findById(categoryId) ?: return emptyList()
        return if (category == UnitCategory.TEMPERATURE) {
            TemperatureUnitCatalog.units.units.map { UnitOption(it.id, it.label) }
        } else {
            UnitDefinitionCatalog.forCategory(category)
                ?.units
                ?.map { UnitOption(it.id, it.label) }
                .orEmpty()
        }
    }

    fun defaults(categoryId: String): Pair<String, String>? {
        val category = UnitCategoryCatalog.findById(categoryId) ?: return null
        return if (category == UnitCategory.TEMPERATURE) {
            TemperatureUnitCatalog.units.defaultFromUnitId to
                TemperatureUnitCatalog.units.defaultToUnitId
        } else {
            UnitDefinitionCatalog.forCategory(category)?.let {
                it.defaultFromUnitId to it.defaultToUnitId
            }
        }
    }

    fun present(
        inputText: String,
        categoryId: String,
        sourceUnitId: String,
        targetUnitId: String,
    ): UnitConversionPresentation {
        if (inputText.isBlank()) return UnitConversionPresentation.EmptyInput
        val value = inputText.trim().toDoubleOrNull()
            ?: return UnitConversionPresentation.InvalidInput

        val primary = UnitConverter.convert(
            value = value,
            sourceCategoryId = categoryId,
            sourceUnitId = sourceUnitId,
            targetCategoryId = categoryId,
            targetUnitId = targetUnitId,
        )
        if (primary is UnitConversionResult.Failure) {
            return UnitConversionPresentation.Failure(primary.error)
        }

        val rows = units(categoryId).map { unit ->
            UnitConversionRow(
                unit = unit,
                result = UnitConverter.convert(
                    value = value,
                    sourceCategoryId = categoryId,
                    sourceUnitId = sourceUnitId,
                    targetCategoryId = categoryId,
                    targetUnitId = unit.id,
                ),
            )
        }
        return UnitConversionPresentation.Converted(
            value = (primary as UnitConversionResult.Success).value,
            rows = rows,
        )
    }
}

/** Stable, locale-independent number text for the conversion result area. */
internal object UnitValueFormatter {
    private val normalContext = MathContext(10, RoundingMode.HALF_UP)
    private val scientificContext = MathContext(7, RoundingMode.HALF_UP)

    fun format(value: Double): String {
        if (!value.isFinite()) return "未定义"
        if (value == 0.0) return "0"

        val magnitude = abs(value)
        val rounded = if (magnitude >= 1e15 || magnitude < 1e-6) {
            BigDecimal.valueOf(value)
                .round(scientificContext)
                .stripTrailingZeros()
                .toString()
                .replace('E', 'e')
        } else {
            BigDecimal.valueOf(value)
                .round(normalContext)
                .stripTrailingZeros()
                .toPlainString()
        }
        return rounded
    }
}
