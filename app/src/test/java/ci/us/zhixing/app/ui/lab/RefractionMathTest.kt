package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Test

class RefractionMathTest {
    @Test
    fun refractionStrengthStaysWithinSupportedRange() {
        assertEquals(0f, clampRefractionStrength(-2f), 0f)
        assertEquals(8f, clampRefractionStrength(10f), 0f)
        assertEquals(3.5f, clampRefractionStrength(3.5f), 0f)
    }

    @Test
    fun nonFiniteStrengthFallsBackToZero() {
        assertEquals(0f, clampRefractionStrength(Float.NaN), 0f)
        assertEquals(0f, clampRefractionStrength(Float.POSITIVE_INFINITY), 0f)
    }
}
