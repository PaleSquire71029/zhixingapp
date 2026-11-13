package ci.us.zhixing.app.domain.units

import java.util.Collections

/**
 * Legacy-compatible linear unit definitions migrated from calculator.html.
 *
 * Factors are relative to each category's base unit. This catalog stores
 * metadata only; conversion arithmetic and input parsing are separate steps.
 */
internal object UnitDefinitionCatalog {
    val all: List<UnitCategoryUnitSet> = Collections.unmodifiableList(
        listOf(
            UnitCategoryUnitSet(
                category = UnitCategory.LENGTH,
                defaultFromUnitId = "m",
                defaultToUnitId = "ft",
                units = listOf(
                    UnitDefinition("km", "千米 km", 1000.0),
                    UnitDefinition("m", "米 m", 1.0),
                    UnitDefinition("dm", "分米 dm", 0.1),
                    UnitDefinition("cm", "厘米 cm", 0.01),
                    UnitDefinition("mm", "毫米 mm", 0.001),
                    UnitDefinition("um", "微米 μm", 1e-6),
                    UnitDefinition("nm", "纳米 nm", 1e-9),
                    UnitDefinition("nmi", "海里 nmi", 1852.0),
                    UnitDefinition("mi", "英里 mi", 1609.344),
                    UnitDefinition("yd", "码 yd", 0.9144),
                    UnitDefinition("ft", "英尺 ft", 0.3048),
                    UnitDefinition("in", "英寸 in", 0.0254),
                    UnitDefinition("li", "市里", 500.0),
                    UnitDefinition("chi", "市尺", 1.0 / 3.0),
                    UnitDefinition("cun", "市寸", 1.0 / 30.0),
                ),
            ),
            UnitCategoryUnitSet(
                category = UnitCategory.AREA,
                defaultFromUnitId = "m2",
                defaultToUnitId = "ft2",
                units = listOf(
                    UnitDefinition("km2", "平方千米 km²", 1e6),
                    UnitDefinition("ha", "公顷 ha", 1e4),
                    UnitDefinition("mu", "亩", 2000.0 / 3.0),
                    UnitDefinition("m2", "平方米 m²", 1.0),
                    UnitDefinition("dm2", "平方分米 dm²", 0.01),
                    UnitDefinition("cm2", "平方厘米 cm²", 1e-4),
                    UnitDefinition("mm2", "平方毫米 mm²", 1e-6),
                    UnitDefinition("acre", "英亩 acre", 4046.8564224),
                    UnitDefinition("mi2", "平方英里 mi²", 2589988.110336),
                    UnitDefinition("ft2", "平方英尺 ft²", 0.09290304),
                    UnitDefinition("in2", "平方英寸 in²", 0.00064516),
                ),
            ),
            UnitCategoryUnitSet(
                category = UnitCategory.VOLUME,
                defaultFromUnitId = "l",
                defaultToUnitId = "ml",
                units = listOf(
                    UnitDefinition("m3", "立方米 m³", 1.0),
                    UnitDefinition("dm3", "立方分米 dm³", 0.001),
                    UnitDefinition("cm3", "立方厘米 cm³", 1e-6),
                    UnitDefinition("mm3", "立方毫米 mm³", 1e-9),
                    UnitDefinition("l", "升 L", 0.001),
                    UnitDefinition("ml", "毫升 mL", 1e-6),
                    UnitDefinition("galus", "加仑（美）", 0.003785411784),
                    UnitDefinition("galuk", "加仑（英）", 0.00454609),
                    UnitDefinition("ft3", "立方英尺 ft³", 0.028316846592),
                    UnitDefinition("in3", "立方英寸 in³", 0.000016387064),
                ),
            ),
            UnitCategoryUnitSet(
                category = UnitCategory.MASS,
                defaultFromUnitId = "kg",
                defaultToUnitId = "lb",
                units = listOf(
                    UnitDefinition("t", "吨 t", 1000.0),
                    UnitDefinition("kg", "千克 kg", 1.0),
                    UnitDefinition("g", "克 g", 0.001),
                    UnitDefinition("mg", "毫克 mg", 1e-6),
                    UnitDefinition("jin", "市斤", 0.5),
                    UnitDefinition("liang", "市两", 0.05),
                    UnitDefinition("lb", "磅 lb", 0.45359237),
                    UnitDefinition("oz", "盎司 oz", 0.028349523125),
                    UnitDefinition("st", "英石 st", 6.35029318),
                ),
            ),
            UnitCategoryUnitSet(
                category = UnitCategory.SPEED,
                defaultFromUnitId = "kmh",
                defaultToUnitId = "ms",
                units = listOf(
                    UnitDefinition("ms", "米每秒 m/s", 1.0),
                    UnitDefinition("kmh", "千米每小时 km/h", 1.0 / 3.6),
                    UnitDefinition("mph", "英里每小时 mph", 0.44704),
                    UnitDefinition("knot", "节 kn", 0.5144444444444444),
                    UnitDefinition("fts", "英尺每秒 ft/s", 0.3048),
                    UnitDefinition("mach", "马赫 Ma", 340.29),
                ),
            ),
            UnitCategoryUnitSet(
                category = UnitCategory.TIME,
                defaultFromUnitId = "h",
                defaultToUnitId = "min",
                units = listOf(
                    UnitDefinition("ns", "纳秒 ns", 1e-9),
                    UnitDefinition("us", "微秒 μs", 1e-6),
                    UnitDefinition("ms", "毫秒 ms", 0.001),
                    UnitDefinition("s", "秒 s", 1.0),
                    UnitDefinition("min", "分 min", 60.0),
                    UnitDefinition("h", "小时 h", 3600.0),
                    UnitDefinition("d", "天 d", 86400.0),
                    UnitDefinition("wk", "周 wk", 604800.0),
                    UnitDefinition("yr", "年（365 天）", 31536000.0),
                ),
            ),
        ),
    )

    private val byCategory: Map<UnitCategory, UnitCategoryUnitSet> =
        all.associateBy(UnitCategoryUnitSet::category)

    /** Categories whose concrete linear units have been migrated so far. */
    val supportedCategories: List<UnitCategory> =
        Collections.unmodifiableList(all.map(UnitCategoryUnitSet::category))

    fun forCategory(category: UnitCategory): UnitCategoryUnitSet? =
        byCategory[category]

    fun find(category: UnitCategory, unitId: String): UnitDefinition? =
        byCategory[category]?.findUnit(unitId)
}
