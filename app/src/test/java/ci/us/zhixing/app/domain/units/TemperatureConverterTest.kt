package ci.us.zhixing.app.domain.units

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemperatureConverterTest {
    @Test
    fun temperatureCatalogKeepsLegacyIdsLabelsAndDefaults() {
        assertEquals(UnitCategory.TEMPERATURE, TemperatureUnitCatalog.units.category)
        assertEquals(UnitConversionKind.AFFINE, TemperatureUnitCatalog.units.category.conversionKind)
        assertEquals("c", TemperatureUnitCatalog.units.defaultFromUnitId)
        assertEquals("f", TemperatureUnitCatalog.units.defaultToUnitId)
        assertEquals(listOf("c", "f", "k"), TemperatureUnitCatalog.units.units.map { it.id })
        assertEquals("摄氏度 °C", TemperatureUnitCatalog.find("c")!!.label)
        assertEquals("华氏度 °F", TemperatureUnitCatalog.find("f")!!.label)
        assertEquals("开尔文 K", TemperatureUnitCatalog.find("k")!!.label)
        assertEquals(3, TemperatureUnitCatalog.units.units.size)
    }

    @Test
    fun celsiusFahrenheitAndKelvinConvertUsingAffineOffsets() {
        assertSuccess(TemperatureConverter.convert(0.0, "c", "f"), 32.0)
        assertSuccess(TemperatureConverter.convert(100.0, "c", "f"), 212.0)
        assertSuccess(TemperatureConverter.convert(32.0, "f", "c"), 0.0)
        assertSuccess(TemperatureConverter.convert(212.0, "f", "c"), 100.0)
        assertSuccess(TemperatureConverter.convert(0.0, "c", "k"), 273.15)
        assertSuccess(TemperatureConverter.convert(273.15, "k", "c"), 0.0)
        assertSuccess(TemperatureConverter.convert(0.0, "k", "f"), -459.67)
        assertSuccess(TemperatureConverter.convert(300.0, "k", "k"), 300.0)
    }

    @Test
    fun negativeTemperaturesAboveAbsoluteZeroAreSupported() {
        assertSuccess(TemperatureConverter.convert(-40.0, "c", "f"), -40.0)
        assertSuccess(TemperatureConverter.convert(-20.0, "c", "k"), 253.15)
        assertSuccess(TemperatureConverter.convert(-4.0, "f", "c"), -20.0)
    }

    @Test
    fun allThreeAbsoluteZeroRepresentationsMapToZeroKelvin() {
        assertSuccess(TemperatureConverter.convert(-273.15, "c", "k"), 0.0)
        assertSuccess(TemperatureConverter.convert(-459.67, "f", "k"), 0.0)
        assertSuccess(TemperatureConverter.convert(0.0, "k", "k"), 0.0)
        assertSuccess(TemperatureConverter.convert(0.0, "k", "c"), -273.15)
        assertSuccess(TemperatureConverter.convert(0.0, "k", "f"), -459.67)
    }

    @Test
    fun valuesImmediatelyBelowAbsoluteZeroAreRejected() {
        assertFailure(
            TemperatureConverter.convert(Math.nextDown(-273.15), "c", "k"),
            TemperatureConversionError.BelowAbsoluteZero,
        )
        assertFailure(
            TemperatureConverter.convert(Math.nextDown(-459.67), "f", "k"),
            TemperatureConversionError.BelowAbsoluteZero,
        )
        assertFailure(
            TemperatureConverter.convert(Math.nextDown(0.0), "k", "c"),
            TemperatureConversionError.BelowAbsoluteZero,
        )
    }

    @Test
    fun nonFiniteInputsAreRejected() {
        assertFailure(
            TemperatureConverter.convert(Double.NaN, "c", "f"),
            TemperatureConversionError.NonFiniteInput,
        )
        assertFailure(
            TemperatureConverter.convert(Double.POSITIVE_INFINITY, "c", "f"),
            TemperatureConversionError.NonFiniteInput,
        )
        assertFailure(
            TemperatureConverter.convert(Double.NEGATIVE_INFINITY, "f", "c"),
            TemperatureConversionError.NonFiniteInput,
        )
    }

    @Test
    fun conversionOverflowReturnsTypedFailure() {
        assertFailure(
            TemperatureConverter.convert(Double.MAX_VALUE, "k", "f"),
            TemperatureConversionError.NonFiniteResult,
        )
    }

    @Test
    fun unknownSourceAndTargetUnitsReturnTypedFailures() {
        assertFailure(
            TemperatureConverter.convert(20.0, "unknown", "c"),
            TemperatureConversionError.UnknownSourceUnit("unknown"),
        )
        assertFailure(
            TemperatureConverter.convert(20.0, "c", "unknown"),
            TemperatureConversionError.UnknownTargetUnit("unknown"),
        )
    }

    @Test
    fun temperatureCatalogIsImmutableAndAffineSetsRejectLinearCategories() {
        @Suppress("UNCHECKED_CAST")
        val mutableUnits =
            TemperatureUnitCatalog.units.units as MutableList<AffineUnitDefinition>
        var rejected = false
        try {
            mutableUnits.add(mutableUnits.first())
        } catch (_: UnsupportedOperationException) {
            rejected = true
        }
        assertTrue(rejected)
        assertEquals(3, TemperatureUnitCatalog.units.units.size)

        assertIllegalArgument {
            AffineUnitCategoryUnitSet(
                category = UnitCategory.LENGTH,
                defaultFromUnitId = "m",
                defaultToUnitId = "m",
                units = listOf(AffineUnitDefinition("m", "米", 1.0, 0.0)),
            )
        }
        assertIllegalArgument { AffineUnitDefinition("", "°C", 1.0, 0.0) }
        assertIllegalArgument { AffineUnitDefinition("c", " ", 1.0, 0.0) }
        assertIllegalArgument { AffineUnitDefinition("c", "°C", 0.0, 0.0) }
        assertIllegalArgument { AffineUnitDefinition("c", "°C", 1.0, Double.NaN) }
    }

    private fun assertSuccess(
        result: TemperatureConversionResult,
        expected: Double,
        delta: Double = 1e-10,
    ) {
        assertTrue("Expected success but got $result", result is TemperatureConversionResult.Success)
        assertEquals(expected, (result as TemperatureConversionResult.Success).value, delta)
    }

    private fun assertFailure(
        result: TemperatureConversionResult,
        expected: TemperatureConversionError,
    ) {
        assertTrue("Expected failure but got $result", result is TemperatureConversionResult.Failure)
        assertEquals(expected, (result as TemperatureConversionResult.Failure).error)
    }

    private fun assertIllegalArgument(action: () -> Unit) {
        var rejected = false
        try {
            action()
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue("Expected invalid affine metadata to be rejected", rejected)
    }
}
