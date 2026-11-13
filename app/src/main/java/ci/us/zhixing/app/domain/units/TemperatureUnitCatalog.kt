package ci.us.zhixing.app.domain.units

/**
 * Legacy-compatible temperature definitions, expressed against kelvin.
 *
 * The offsets are applied before scaling so each absolute-zero input maps
 * exactly to 0 K: C + 273.15, (F + 459.67) * 5/9, and K unchanged.
 */
internal object TemperatureUnitCatalog {
    val units = AffineUnitCategoryUnitSet(
        category = UnitCategory.TEMPERATURE,
        defaultFromUnitId = "c",
        defaultToUnitId = "f",
        units = listOf(
            AffineUnitDefinition("c", "摄氏度 °C", 1.0, 273.15),
            AffineUnitDefinition("f", "华氏度 °F", 5.0 / 9.0, 459.67),
            AffineUnitDefinition("k", "开尔文 K", 1.0, 0.0),
        ),
    )

    fun find(unitId: String): AffineUnitDefinition? = units.findUnit(unitId)
}
