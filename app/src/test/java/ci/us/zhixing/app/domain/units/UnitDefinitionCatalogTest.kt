package ci.us.zhixing.app.domain.units

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitDefinitionCatalogTest {
    @Test
    fun catalogContainsSixMigratedCategoriesInStableOrder() {
        assertEquals(
            listOf(
                UnitCategory.LENGTH,
                UnitCategory.AREA,
                UnitCategory.VOLUME,
                UnitCategory.MASS,
                UnitCategory.SPEED,
                UnitCategory.TIME,
            ),
            UnitDefinitionCatalog.supportedCategories,
        )
        assertEquals(6, UnitDefinitionCatalog.all.size)
    }

    @Test
    fun legacyUnitIdsAndCountsArePreservedForAllSixCategories() {
        assertEquals(
            listOf(
                "km", "m", "dm", "cm", "mm", "um", "nm", "nmi", "mi",
                "yd", "ft", "in", "li", "chi", "cun",
            ),
            UnitDefinitionCatalog.forCategory(UnitCategory.LENGTH)!!.units.map(UnitDefinition::id),
        )
        assertEquals(
            listOf(
                "km2", "ha", "mu", "m2", "dm2", "cm2", "mm2", "acre", "mi2", "ft2", "in2",
            ),
            UnitDefinitionCatalog.forCategory(UnitCategory.AREA)!!.units.map(UnitDefinition::id),
        )
        assertEquals(
            listOf(
                "m3", "dm3", "cm3", "mm3", "l", "ml", "galus", "galuk", "ft3", "in3",
            ),
            UnitDefinitionCatalog.forCategory(UnitCategory.VOLUME)!!.units.map(UnitDefinition::id),
        )
        assertEquals(
            listOf("t", "kg", "g", "mg", "jin", "liang", "lb", "oz", "st"),
            UnitDefinitionCatalog.forCategory(UnitCategory.MASS)!!.units.map(UnitDefinition::id),
        )
        assertEquals(
            listOf("ms", "kmh", "mph", "knot", "fts", "mach"),
            UnitDefinitionCatalog.forCategory(UnitCategory.SPEED)!!.units.map(UnitDefinition::id),
        )
        assertEquals(
            listOf("ns", "us", "ms", "s", "min", "h", "d", "wk", "yr"),
            UnitDefinitionCatalog.forCategory(UnitCategory.TIME)!!.units.map(UnitDefinition::id),
        )
        assertEquals(60, UnitDefinitionCatalog.all.sumOf { it.units.size })
    }

    @Test
    fun legacyLabelsAndDefaultSelectionsArePreserved() {
        val length = UnitDefinitionCatalog.forCategory(UnitCategory.LENGTH)!!
        val area = UnitDefinitionCatalog.forCategory(UnitCategory.AREA)!!
        val volume = UnitDefinitionCatalog.forCategory(UnitCategory.VOLUME)!!
        val mass = UnitDefinitionCatalog.forCategory(UnitCategory.MASS)!!
        val speed = UnitDefinitionCatalog.forCategory(UnitCategory.SPEED)!!
        val time = UnitDefinitionCatalog.forCategory(UnitCategory.TIME)!!

        assertEquals("m", length.defaultFromUnitId)
        assertEquals("ft", length.defaultToUnitId)
        assertEquals("米 m", length.findUnit("m")!!.label)
        assertEquals("微米 μm", length.findUnit("um")!!.label)
        assertEquals("市里", length.findUnit("li")!!.label)

        assertEquals("m2", area.defaultFromUnitId)
        assertEquals("ft2", area.defaultToUnitId)
        assertEquals("平方千米 km²", area.findUnit("km2")!!.label)
        assertEquals("亩", area.findUnit("mu")!!.label)

        assertEquals("l", volume.defaultFromUnitId)
        assertEquals("ml", volume.defaultToUnitId)
        assertEquals("升 L", volume.findUnit("l")!!.label)
        assertEquals("加仑（美）", volume.findUnit("galus")!!.label)

        assertEquals("kg", mass.defaultFromUnitId)
        assertEquals("lb", mass.defaultToUnitId)
        assertEquals("千克 kg", mass.findUnit("kg")!!.label)
        assertEquals("市斤", mass.findUnit("jin")!!.label)

        assertEquals("kmh", speed.defaultFromUnitId)
        assertEquals("ms", speed.defaultToUnitId)
        assertEquals("千米每小时 km/h", speed.findUnit("kmh")!!.label)
        assertEquals("马赫 Ma", speed.findUnit("mach")!!.label)

        assertEquals("h", time.defaultFromUnitId)
        assertEquals("min", time.defaultToUnitId)
        assertEquals("小时 h", time.findUnit("h")!!.label)
        assertEquals("年（365 天）", time.findUnit("yr")!!.label)
    }

    @Test
    fun everyLegacyScaleFactorMatchesItsCategoryBaseUnit() {
        val expected = mapOf(
            "length/km" to 1000.0,
            "length/m" to 1.0,
            "length/dm" to 0.1,
            "length/cm" to 0.01,
            "length/mm" to 0.001,
            "length/um" to 1e-6,
            "length/nm" to 1e-9,
            "length/nmi" to 1852.0,
            "length/mi" to 1609.344,
            "length/yd" to 0.9144,
            "length/ft" to 0.3048,
            "length/in" to 0.0254,
            "length/li" to 500.0,
            "length/chi" to 1.0 / 3.0,
            "length/cun" to 1.0 / 30.0,
            "area/km2" to 1e6,
            "area/ha" to 1e4,
            "area/mu" to 2000.0 / 3.0,
            "area/m2" to 1.0,
            "area/dm2" to 0.01,
            "area/cm2" to 1e-4,
            "area/mm2" to 1e-6,
            "area/acre" to 4046.8564224,
            "area/mi2" to 2589988.110336,
            "area/ft2" to 0.09290304,
            "area/in2" to 0.00064516,
            "volume/m3" to 1.0,
            "volume/dm3" to 0.001,
            "volume/cm3" to 1e-6,
            "volume/mm3" to 1e-9,
            "volume/l" to 0.001,
            "volume/ml" to 1e-6,
            "volume/galus" to 0.003785411784,
            "volume/galuk" to 0.00454609,
            "volume/ft3" to 0.028316846592,
            "volume/in3" to 0.000016387064,
            "mass/t" to 1000.0,
            "mass/kg" to 1.0,
            "mass/g" to 0.001,
            "mass/mg" to 1e-6,
            "mass/jin" to 0.5,
            "mass/liang" to 0.05,
            "mass/lb" to 0.45359237,
            "mass/oz" to 0.028349523125,
            "mass/st" to 6.35029318,
            "speed/ms" to 1.0,
            "speed/kmh" to 1.0 / 3.6,
            "speed/mph" to 0.44704,
            "speed/knot" to 0.5144444444444444,
            "speed/fts" to 0.3048,
            "speed/mach" to 340.29,
            "time/ns" to 1e-9,
            "time/us" to 1e-6,
            "time/ms" to 0.001,
            "time/s" to 1.0,
            "time/min" to 60.0,
            "time/h" to 3600.0,
            "time/d" to 86400.0,
            "time/wk" to 604800.0,
            "time/yr" to 31536000.0,
        )

        assertEquals(60, expected.size)
        UnitDefinitionCatalog.all.forEach { categorySet ->
            categorySet.units.forEach { unit ->
                val key = "${categorySet.category.id}/${unit.id}"
                assertEquals("Unexpected scale for $key", expected[key]!!, unit.scaleToBase, 0.0)
            }
        }
    }

    @Test
    fun definitionsHavePositiveFiniteScalesAndCorrectDimensions() {
        UnitDefinitionCatalog.all.forEach { categorySet ->
            assertTrue(categorySet.category.conversionKind == UnitConversionKind.LINEAR)
            assertTrue(categorySet.units.all {
                it.id.isNotBlank() && it.label.isNotBlank() &&
                    it.scaleToBase.isFinite() && it.scaleToBase > 0.0
            })
        }
        assertEquals(UnitDimension(lengthExponent = 1), UnitCategory.LENGTH.dimension)
        assertEquals(UnitDimension(lengthExponent = 2), UnitCategory.AREA.dimension)
        assertEquals(UnitDimension(lengthExponent = 3), UnitCategory.VOLUME.dimension)
        assertEquals(UnitDimension(massExponent = 1), UnitCategory.MASS.dimension)
        assertEquals(
            UnitDimension(lengthExponent = 1, timeExponent = -1),
            UnitCategory.SPEED.dimension,
        )
        assertEquals(UnitDimension(timeExponent = 1), UnitCategory.TIME.dimension)
    }

    @Test
    fun lookupIsScopedToTheRequestedCategory() {
        assertEquals(
            0.3048,
            UnitDefinitionCatalog.find(UnitCategory.LENGTH, "ft")!!.scaleToBase,
            0.0,
        )
        assertEquals(
            0.45359237,
            UnitDefinitionCatalog.find(UnitCategory.MASS, "lb")!!.scaleToBase,
            0.0,
        )
        assertNull(UnitDefinitionCatalog.find(UnitCategory.AREA, "ft"))
        assertNull(UnitDefinitionCatalog.find(UnitCategory.LENGTH, "unknown"))
        assertNull(UnitDefinitionCatalog.find(UnitCategory.MASS, "km"))
        assertNull(UnitDefinitionCatalog.forCategory(UnitCategory.TEMPERATURE))
        assertNull(UnitDefinitionCatalog.forCategory(UnitCategory.DATA_STORAGE))
        assertNull(UnitDefinitionCatalog.forCategory(UnitCategory.ANGLE))
    }

    @Test
    fun categoryAndUnitListsAreImmutableAndDefaultsExist() {
        UnitDefinitionCatalog.all.forEach { categorySet ->
            assertTrue(categorySet.findUnit(categorySet.defaultFromUnitId) != null)
            assertTrue(categorySet.findUnit(categorySet.defaultToUnitId) != null)
            assertEquals(
                categorySet.units.size,
                categorySet.units.map(UnitDefinition::id).toSet().size,
            )
        }

        @Suppress("UNCHECKED_CAST")
        val mutableCategories = UnitDefinitionCatalog.all as MutableList<UnitCategoryUnitSet>
        @Suppress("UNCHECKED_CAST")
        val mutableUnits = UnitDefinitionCatalog.all.first().units as MutableList<UnitDefinition>

        assertMutationRejected { mutableCategories.add(UnitDefinitionCatalog.all.first()) }
        assertMutationRejected { mutableUnits.add(mutableUnits.first()) }
        assertEquals(6, UnitDefinitionCatalog.all.size)
        assertEquals(15, UnitDefinitionCatalog.all.first().units.size)
        assertEquals(9, UnitDefinitionCatalog.all.last().units.size)
    }

    @Test
    fun invalidUnitDefinitionsAreRejected() {
        assertIllegalArgument { UnitDefinition("", "米", 1.0) }
        assertIllegalArgument { UnitDefinition("m", " ", 1.0) }
        assertIllegalArgument { UnitDefinition("bad", "无效", 0.0) }
        assertIllegalArgument { UnitDefinition("bad", "无效", -1.0) }
        assertIllegalArgument { UnitDefinition("bad", "无效", Double.NaN) }
        assertIllegalArgument { UnitDefinition("bad", "无效", Double.POSITIVE_INFINITY) }
    }

    @Test
    fun scaleOnlySetsRejectAffineCategoriesAndInvalidDefaults() {
        val unit = UnitDefinition("c", "摄氏度 °C", 1.0)
        assertIllegalArgument {
            UnitCategoryUnitSet(UnitCategory.TEMPERATURE, "c", "c", listOf(unit))
        }
        assertIllegalArgument {
            UnitCategoryUnitSet(
                UnitCategory.LENGTH,
                "missing",
                "m",
                listOf(UnitDefinition("m", "米 m", 1.0)),
            )
        }
        assertIllegalArgument {
            UnitCategoryUnitSet(
                UnitCategory.LENGTH,
                "m",
                "m",
                listOf(
                    UnitDefinition("m", "米 m", 1.0),
                    UnitDefinition("m", "重复米", 1.0),
                ),
            )
        }
    }

    private fun assertMutationRejected(mutation: () -> Unit) {
        var rejected = false
        try {
            mutation()
        } catch (_: UnsupportedOperationException) {
            rejected = true
        }
        assertTrue("Expected the catalog to reject mutation", rejected)
    }

    private fun assertIllegalArgument(action: () -> Unit) {
        var rejected = false
        try {
            action()
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue("Expected invalid metadata to be rejected", rejected)
    }
}
