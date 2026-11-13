package ci.us.zhixing.app.domain.units

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConverterTest {
    @Test
    fun allNineCategoriesHaveAConversionPath() {
        val supportedIds = UnitDefinitionCatalog.supportedCategories.map(UnitCategory::id) +
            UnitCategory.TEMPERATURE.id

        assertEquals(9, supportedIds.size)
        assertEquals(UnitCategoryCatalog.all.map(UnitCategory::id).toSet(), supportedIds.toSet())

        assertSuccess(convert(1.0, "length", "km", "length", "m"), 1000.0)
        assertSuccess(convert(1.0, "area", "ha", "area", "m2"), 10000.0)
        assertSuccess(convert(1.0, "volume", "l", "volume", "ml"), 1000.0)
        assertSuccess(convert(1.0, "mass", "kg", "mass", "g"), 1000.0)
        assertSuccess(convert(36.0, "speed", "kmh", "speed", "ms"), 10.0)
        assertSuccess(convert(1.0, "time", "h", "time", "min"), 60.0)
        assertSuccess(convert(1.0, "data", "mib", "data", "b"), 1048576.0)
        assertSuccess(convert(180.0, "angle", "deg", "angle", "rad"), Math.PI)
        assertSuccess(convert(0.0, "temp", "c", "temp", "f"), 32.0)
    }

    @Test
    fun sourceAndTargetMustHaveCompatiblePhysicalDimensions() {
        assertFailure(
            convert(1.0, "length", "m", "mass", "kg"),
            UnitConversionError.IncompatibleDimensions("length", "mass"),
        )
        assertFailure(
            convert(1.0, "data", "b", "angle", "rad"),
            UnitConversionError.IncompatibleDimensions("data", "angle"),
        )
        assertFailure(
            convert(1.0, "temperature", "c", "temp", "f"),
            UnitConversionError.UnknownSourceCategory("temperature"),
        )
    }

    @Test
    fun allCrossCategoryConversionsAreRejectedByDimensionValidation() {
        val sampleUnits = mapOf(
            UnitCategory.LENGTH to ("m" to "m"),
            UnitCategory.AREA to ("m2" to "m2"),
            UnitCategory.VOLUME to ("m3" to "m3"),
            UnitCategory.MASS to ("kg" to "kg"),
            UnitCategory.TEMPERATURE to ("c" to "c"),
            UnitCategory.SPEED to ("ms" to "ms"),
            UnitCategory.TIME to ("s" to "s"),
            UnitCategory.DATA_STORAGE to ("b" to "b"),
            UnitCategory.ANGLE to ("rad" to "rad"),
        )

        UnitCategoryCatalog.all.forEach { source ->
            UnitCategoryCatalog.all
                .filter { target -> target != source }
                .forEach { target ->
                    val sourceUnits = sampleUnits.getValue(source)
                    val targetUnits = sampleUnits.getValue(target)
                    assertFailure(
                        convert(
                            1.0,
                            source.id,
                            sourceUnits.first,
                            target.id,
                            targetUnits.first,
                        ),
                        UnitConversionError.IncompatibleDimensions(source.id, target.id),
                    )
                }
        }
    }

    @Test
    fun unknownCategoryAndUnitIdsReturnTypedFailures() {
        assertFailure(
            convert(1.0, "unknown", "m", "length", "m"),
            UnitConversionError.UnknownSourceCategory("unknown"),
        )
        assertFailure(
            convert(1.0, "length", "m", "unknown", "m"),
            UnitConversionError.UnknownTargetCategory("unknown"),
        )
        assertFailure(
            convert(1.0, "length", "kg", "length", "m"),
            UnitConversionError.UnknownSourceUnit("length", "kg"),
        )
        assertFailure(
            convert(1.0, "length", "m", "length", "kg"),
            UnitConversionError.UnknownTargetUnit("length", "kg"),
        )
        assertFailure(
            convert(1.0, "temp", "unknown", "temp", "c"),
            UnitConversionError.UnknownSourceUnit("temp", "unknown"),
        )
        assertFailure(
            convert(1.0, "temp", "c", "temp", "unknown"),
            UnitConversionError.UnknownTargetUnit("temp", "unknown"),
        )
    }

    @Test
    fun duplicateUnitIdsAreScopedByCategory() {
        assertSuccess(convert(1.0, "speed", "ms", "speed", "kmh"), 3.6)
        assertSuccess(convert(1.0, "time", "ms", "time", "s"), 0.001)
        assertFailure(
            convert(1.0, "speed", "ms", "time", "ms"),
            UnitConversionError.IncompatibleDimensions("speed", "time"),
        )
    }

    @Test
    fun nonFiniteInputsAreRejectedBeforeLookup() {
        assertFailure(
            convert(Double.NaN, "length", "m", "length", "m"),
            UnitConversionError.NonFiniteInput,
        )
        assertFailure(
            convert(Double.POSITIVE_INFINITY, "temp", "c", "temp", "f"),
            UnitConversionError.NonFiniteInput,
        )
    }

    @Test
    fun linearOverflowAndUnderflowReturnTypedErrors() {
        assertFailure(
            convert(Double.MAX_VALUE, "length", "km", "length", "mm"),
            UnitConversionError.NonFiniteResult,
        )
        assertFailure(
            convert(Double.MIN_VALUE, "length", "m", "length", "km"),
            UnitConversionError.PrecisionUnderflow,
        )
    }

    @Test
    fun linearBigDecimalIntermediateAvoidsFalseOverflow() {
        assertSuccess(
            convert(Double.MAX_VALUE, "data", "tib", "data", "tib"),
            Double.MAX_VALUE,
            delta = 0.0,
        )
        assertSuccess(
            convert(1.0e300, "data", "gb", "data", "tb"),
            1.0e297,
            delta = 1.0e283,
        )
    }

    @Test
    fun affineErrorsAreMappedIntoTheUnifiedErrorModel() {
        assertFailure(
            convert(-273.151, "temp", "c", "temp", "k"),
            UnitConversionError.BelowAbsoluteZero,
        )
        assertFailure(
            convert(Double.MAX_VALUE, "temp", "k", "temp", "f"),
            UnitConversionError.NonFiniteResult,
        )
        assertSuccess(convert(-40.0, "temp", "c", "temp", "f"), -40.0)
    }

    private fun convert(
        value: Double,
        sourceCategoryId: String,
        sourceUnitId: String,
        targetCategoryId: String,
        targetUnitId: String,
    ) = UnitConverter.convert(
        value = value,
        sourceCategoryId = sourceCategoryId,
        sourceUnitId = sourceUnitId,
        targetCategoryId = targetCategoryId,
        targetUnitId = targetUnitId,
    )

    private fun assertSuccess(
        result: UnitConversionResult,
        expected: Double,
        delta: Double = 1e-10,
    ) {
        assertTrue("Expected success but got $result", result is UnitConversionResult.Success)
        assertEquals(expected, (result as UnitConversionResult.Success).value, delta)
    }

    private fun assertFailure(
        result: UnitConversionResult,
        expected: UnitConversionError,
    ) {
        assertTrue("Expected failure but got $result", result is UnitConversionResult.Failure)
        assertEquals(expected, (result as UnitConversionResult.Failure).error)
    }
}
