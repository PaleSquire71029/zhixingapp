package ci.us.zhixing.app.domain.units

import java.util.Collections

/**
 * A linear unit's scale relative to its category's base unit.
 *
 * The base units in the current catalog are metre, square metre, and cubic
 * metre. Affine units such as temperatures use a separate representation.
 */
internal data class UnitDefinition(
    val id: String,
    val label: String,
    val scaleToBase: Double,
) {
    init {
        require(id.isNotBlank()) { "Unit id must not be blank" }
        require(label.isNotBlank()) { "Unit label must not be blank" }
        require(scaleToBase.isFinite() && scaleToBase > 0.0) {
            "A linear unit scale must be finite and positive"
        }
    }
}

/** Immutable unit definitions and legacy defaults for one linear category. */
internal class UnitCategoryUnitSet(
    val category: UnitCategory,
    val defaultFromUnitId: String,
    val defaultToUnitId: String,
    units: List<UnitDefinition>,
) {
    val units: List<UnitDefinition> =
        Collections.unmodifiableList(units.toList())

    private val byId: Map<String, UnitDefinition> =
        this.units.associateBy(UnitDefinition::id)

    init {
        require(category.conversionKind == UnitConversionKind.LINEAR) {
            "Scale-only unit sets require a LINEAR category"
        }
        require(this.units.isNotEmpty()) { "A unit category must not be empty" }
        require(byId.size == this.units.size) { "Unit ids must be unique within a category" }
        require(defaultFromUnitId in byId) {
            "Default source unit must exist in the category"
        }
        require(defaultToUnitId in byId) {
            "Default target unit must exist in the category"
        }
    }

    fun findUnit(id: String): UnitDefinition? = byId[id]
}
