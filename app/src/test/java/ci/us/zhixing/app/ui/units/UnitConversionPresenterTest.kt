package ci.us.zhixing.app.ui.units

import ci.us.zhixing.app.domain.units.UnitCategory
import ci.us.zhixing.app.domain.units.UnitConversionError
import ci.us.zhixing.app.domain.units.UnitConversionResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UnitConversionPresenterTest {
    @Test
    fun categoryAndDefaultOrderMatchesLegacyCalculator() {
        assertEquals(
            listOf("length", "area", "volume", "mass", "temp", "speed", "time", "data", "angle"),
            UnitConversionPresenter.categories().map(UnitCategory::id),
        )
        assertEquals("m" to "ft", UnitConversionPresenter.defaults("length"))
        assertEquals("c" to "f", UnitConversionPresenter.defaults("temp"))
        assertEquals("mb" to "gb", UnitConversionPresenter.defaults("data"))
    }

    @Test
    fun blankAndMalformedInputNeverBecomeZero() {
        assertTrue(
            UnitConversionPresenter.present("", "length", "m", "ft") is
                UnitConversionPresentation.EmptyInput,
        )
        assertTrue(
            UnitConversionPresenter.present("   ", "length", "m", "ft") is
                UnitConversionPresentation.EmptyInput,
        )
        assertTrue(
            UnitConversionPresenter.present("not-a-number", "length", "m", "ft") is
                UnitConversionPresentation.InvalidInput,
        )
        assertTrue(
            UnitConversionPresenter.present("NaN", "length", "m", "ft") is
                UnitConversionPresentation.InvalidInput,
        )
        assertTrue(
            UnitConversionPresenter.present("1,5", "length", "m", "ft") is
                UnitConversionPresentation.InvalidInput,
        )
    }

    @Test
    fun unfinishedTypingStatesAreNotReportedAsInvalidInput() {
        listOf("-", "+", ".", "-.", "1e", "1e-", "2.5E+").forEach { partial ->
            assertTrue(
                "$partial should be treated as an unfinished value",
                UnitConversionPresenter.present(partial, "length", "m", "ft") is
                    UnitConversionPresentation.IncompleteInput,
            )
        }
    }

    @Test
    fun decimalSignedAndScientificValuesAreAccepted() {
        listOf("-0.5", ".25", "1.", "1e-3", "+2.5E+2").forEach { value ->
            assertTrue(
                "$value should be parsed",
                UnitConversionPresenter.present(value, "length", "m", "ft") is
                    UnitConversionPresentation.Converted,
            )
        }
    }

    @Test
    fun excessivelyLongInputGetsASeparateState() {
        val tooLong = "1".repeat(UnitConversionPresenter.MAX_INPUT_LENGTH + 1)
        assertTrue(
            UnitConversionPresenter.present(tooLong, "length", "m", "ft") is
                UnitConversionPresentation.InputTooLong,
        )
    }

    @Test
    fun linearInputProducesTargetValueAndCompleteSameCategoryOverview() {
        val state = UnitConversionPresenter.present("1", "length", "m", "ft")
        assertTrue(state is UnitConversionPresentation.Converted)
        state as UnitConversionPresentation.Converted
        assertEquals(3.280839895013123, state.value, 1e-10)
        assertEquals(15, state.rows.size)
        assertEquals(
            3.280839895013123,
            (state.rows.first { it.unit.id == "ft" }.result as UnitConversionResult.Success).value,
            1e-10,
        )
        assertEquals(
            1.0,
            (state.rows.first { it.unit.id == "m" }.result as UnitConversionResult.Success).value,
            0.0,
        )
    }

    @Test
    fun temperatureOverviewUsesAffineConversionForAllThreeUnits() {
        val state = UnitConversionPresenter.present("0", "temp", "c", "f")
        assertTrue(state is UnitConversionPresentation.Converted)
        state as UnitConversionPresentation.Converted
        assertEquals(32.0, state.value, 1e-10)
        assertEquals(3, state.rows.size)
        assertEquals(
            273.15,
            (state.rows.first { it.unit.id == "k" }.result as UnitConversionResult.Success).value,
            1e-10,
        )
    }

    @Test
    fun physicalAndNumericErrorsRemainTyped() {
        val belowAbsoluteZero = UnitConversionPresenter.present(
            "-273.151",
            "temp",
            "c",
            "f",
        )
        assertTrue(belowAbsoluteZero is UnitConversionPresentation.Failure)
        assertEquals(
            UnitConversionError.BelowAbsoluteZero,
            (belowAbsoluteZero as UnitConversionPresentation.Failure).error,
        )

        val overflowInput = UnitConversionPresenter.present("1e999", "length", "m", "ft")
        assertTrue(overflowInput is UnitConversionPresentation.Failure)
        assertEquals(
            UnitConversionError.NonFiniteInput,
            (overflowInput as UnitConversionPresentation.Failure).error,
        )
    }

    @Test
    fun copyTextIncludesOriginalInputAndFormattedTarget() {
        assertEquals(
            "1.250 m = 4.101049869 ft",
            UnitConversionPresenter.copyText(
                inputText = " 1.250 ",
                sourceUnitLabel = "m",
                resultValue = 4.101049868959722,
                targetUnitLabel = "ft",
            ),
        )
    }

    @Test
    fun formatterHandlesZeroPrecisionAndScientificRanges() {
        assertEquals("0", UnitValueFormatter.format(-0.0))
        assertEquals("3.280839895", UnitValueFormatter.format(3.280839895013123))
        assertEquals("-1e-9", UnitValueFormatter.format(-1e-9))
        assertEquals("1e+15", UnitValueFormatter.format(1e15))
        assertTrue(UnitValueFormatter.format(1e-9).startsWith("1e-"))
        assertEquals("未定义", UnitValueFormatter.format(Double.NaN))
    }
}
