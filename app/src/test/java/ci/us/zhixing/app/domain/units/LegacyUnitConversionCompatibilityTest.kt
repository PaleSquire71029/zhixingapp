package ci.us.zhixing.app.domain.units

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * C8.7 compatibility checks against the conversion rules in calculator.html.
 *
 * The catalog's exact legacy factors are independently frozen by
 * UnitDefinitionCatalogTest; these tests verify the complete conversion path.
 */
class LegacyUnitConversionCompatibilityTest {
    @Test
    fun everyLegacyLinearUnitPairMatchesTheOldBaseUnitAlgorithm() {
        val sampleValues = listOf(-123.456, -1.0, 0.0, 1.0, 123.456)
        var pairCount = 0
        var conversionCount = 0

        UnitDefinitionCatalog.all.forEach { categorySet ->
            categorySet.units.forEach { source ->
                categorySet.units.forEach { target ->
                    pairCount += 1
                    sampleValues.forEach { value ->
                        // Legacy calculator.html: b.fromBase(a.toBase(v)).
                        val expected = value * source.scaleToBase / target.scaleToBase
                        val result = UnitConverter.convert(
                            value = value,
                            sourceCategoryId = categorySet.category.id,
                            sourceUnitId = source.id,
                            targetCategoryId = categorySet.category.id,
                            targetUnitId = target.id,
                        )

                        assertTrue(
                            "Expected success for ${categorySet.category.id}/${source.id} " +
                                "-> ${target.id} at $value, got $result",
                            result is UnitConversionResult.Success,
                        )
                        val actual = (result as UnitConversionResult.Success).value
                        assertClose(
                            message = "${categorySet.category.id}/${source.id} -> ${target.id}",
                            expected = expected,
                            actual = actual,
                        )
                        conversionCount += 1
                    }
                }
            }
        }

        // 15² + 11² + 10² + 9² + 6² + 9² + 10² + 6² = 780.
        assertEquals(780, pairCount)
        assertEquals(3900, conversionCount)
    }

    @Test
    fun everyLegacyTemperaturePairMatchesTheOldCelsiusBaseAlgorithm() {
        val valuesBySourceUnit = mapOf(
            "c" to listOf(-273.15, -40.0, 0.0, 25.0, 100.0),
            "f" to listOf(-459.67, -40.0, 32.0, 77.0, 212.0),
            "k" to listOf(0.0, 233.15, 273.15, 298.15, 373.15),
        )
        var conversionCount = 0

        valuesBySourceUnit.forEach { (sourceId, values) ->
            TemperatureUnitCatalog.units.units.forEach { target ->
                values.forEach { value ->
                    // Legacy calculator.html used Celsius as its temperature base.
                    val celsius = legacyToCelsius(value, sourceId)
                    val expected = legacyFromCelsius(celsius, target.id)
                    val result = UnitConverter.convert(
                        value = value,
                        sourceCategoryId = "temp",
                        sourceUnitId = sourceId,
                        targetCategoryId = "temp",
                        targetUnitId = target.id,
                    )

                    assertTrue(
                        "Expected success for temp/$sourceId -> ${target.id} at $value, got $result",
                        result is UnitConversionResult.Success,
                    )
                    assertClose(
                        message = "temp/$sourceId -> ${target.id}",
                        expected = expected,
                        actual = (result as UnitConversionResult.Success).value,
                    )
                    conversionCount += 1
                }
            }
        }

        assertEquals(45, conversionCount)
    }

    @Test
    fun legacyCategoryOrderAndDefaultUnitSelectionsRemainStable() {
        val expected = listOf(
            "length" to ("m" to "ft"),
            "area" to ("m2" to "ft2"),
            "volume" to ("l" to "ml"),
            "mass" to ("kg" to "lb"),
            "temp" to ("c" to "f"),
            "speed" to ("kmh" to "ms"),
            "time" to ("h" to "min"),
            "data" to ("mb" to "gb"),
            "angle" to ("deg" to "rad"),
        )

        assertEquals(expected.map { it.first }, UnitCategoryCatalog.all.map(UnitCategory::id))
        expected.forEach { (categoryId, defaults) ->
            if (categoryId == "temp") {
                assertEquals(defaults.first, TemperatureUnitCatalog.units.defaultFromUnitId)
                assertEquals(defaults.second, TemperatureUnitCatalog.units.defaultToUnitId)
            } else {
                val category = UnitCategoryCatalog.findById(categoryId)!!
                val set = UnitDefinitionCatalog.forCategory(category)!!
                assertEquals(defaults.first, set.defaultFromUnitId)
                assertEquals(defaults.second, set.defaultToUnitId)
            }
        }
    }

    @Test
    fun sameCategoryUnitIdentityIsRequiredEvenWhenUnitIdsRepeat() {
        assertSuccess(
            UnitConverter.convert(1.0, "speed", "ms", "speed", "kmh"),
            3.6,
        )
        assertSuccess(
            UnitConverter.convert(1.0, "time", "ms", "time", "s"),
            0.001,
        )
        assertFailure(
            UnitConverter.convert(1.0, "speed", "ms", "time", "ms"),
            UnitConversionError.IncompatibleDimensions("speed", "time"),
        )
    }

    private fun legacyToCelsius(value: Double, unitId: String): Double =
        when (unitId) {
            "c" -> value
            "f" -> (value - 32.0) * 5.0 / 9.0
            "k" -> value - 273.15
            else -> error("Unknown legacy temperature unit: $unitId")
        }

    private fun legacyFromCelsius(value: Double, unitId: String): Double =
        when (unitId) {
            "c" -> value
            "f" -> value * 9.0 / 5.0 + 32.0
            "k" -> value + 273.15
            else -> error("Unknown legacy temperature unit: $unitId")
        }

    private fun assertClose(message: String, expected: Double, actual: Double) {
        val delta = maxOf(1e-10, abs(expected) * 1e-11)
        assertEquals(message, expected, actual, delta)
    }

    private fun assertSuccess(result: UnitConversionResult, expected: Double) {
        assertTrue("Expected success but got $result", result is UnitConversionResult.Success)
        assertEquals(expected, (result as UnitConversionResult.Success).value, 1e-10)
    }

    private fun assertFailure(result: UnitConversionResult, expected: UnitConversionError) {
        assertTrue("Expected failure but got $result", result is UnitConversionResult.Failure)
        assertEquals(expected, (result as UnitConversionResult.Failure).error)
    }
}
