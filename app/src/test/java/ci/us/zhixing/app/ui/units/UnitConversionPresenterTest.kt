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

        val nonFinite = UnitConversionPresenter.present("NaN", "length", "m", "ft")
        assertTrue(nonFinite is UnitConversionPresentation.Failure)
        assertEquals(
            UnitConversionError.NonFiniteInput,
            (nonFinite as UnitConversionPresentation.Failure).error,
        )
    }

    @Test
    fun formatterHandlesZeroPrecisionAndScientificRanges() {
        assertEquals("0", UnitValueFormatter.format(-0.0))
        assertEquals("3.280839895", UnitValueFormatter.format(3.280839895013123))
        assertEquals("1e+15", UnitValueFormatter.format(1e15))
        assertTrue(UnitValueFormatter.format(1e-9).startsWith("1e-"))
    }
}
