package ci.us.zhixing.app.domain.units

import java.util.Collections

/**
 * An affine unit mapped to a category base using:
 *
 * baseValue = (value + offsetBeforeScale) * scaleToBase
 *
 * For temperature the base unit is kelvin. Keeping offset and scale separate
 * prevents affine units from being mistaken for scale-only UnitDefinition.
 */
internal data class AffineUnitDefinition(
    val id: String,
    val label: String,
    val scaleToBase: Double,
    val offsetBeforeScale: Double,
) {
    init {
        require(id.isNotBlank()) { "Affine unit id must not be blank" }
        require(label.isNotBlank()) { "Affine unit label must not be blank" }
        require(scaleToBase.isFinite() && scaleToBase > 0.0) {
            "An affine unit scale must be finite and positive"
        }
        require(offsetBeforeScale.isFinite()) {
            "An affine unit offset must be finite"
        }
    }

    fun toBase(value: Double): Double = (value + offsetBeforeScale) * scaleToBase

    fun fromBase(baseValue: Double): Double =
        baseValue / scaleToBase - offsetBeforeScale
}

/** Immutable metadata for a category that requires affine conversion. */
internal class AffineUnitCategoryUnitSet(
    val category: UnitCategory,
    val defaultFromUnitId: String,
    val defaultToUnitId: String,
    units: List<AffineUnitDefinition>,
) {
    val units: List<AffineUnitDefinition> =
        Collections.unmodifiableList(units.toList())

    private val byId: Map<String, AffineUnitDefinition> =
        this.units.associateBy(AffineUnitDefinition::id)

    init {
        require(category.conversionKind == UnitConversionKind.AFFINE) {
            "Affine unit sets require an AFFINE category"
        }
        require(this.units.isNotEmpty()) { "An affine category must not be empty" }
        require(byId.size == this.units.size) {
            "Affine unit ids must be unique within a category"
        }
        require(defaultFromUnitId in byId) {
            "Default source unit must exist in the category"
        }
        require(defaultToUnitId in byId) {
            "Default target unit must exist in the category"
        }
    }

    fun findUnit(id: String): AffineUnitDefinition? = byId[id]
}
