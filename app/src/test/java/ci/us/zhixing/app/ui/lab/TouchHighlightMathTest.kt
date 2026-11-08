package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchHighlightMathTest {
    @Test
    fun touchHighlightStrengthIsClamped() {
        assertEquals(0f, clampTouchHighlightStrength(-1f), 0f)
        assertEquals(0.36f, clampTouchHighlightStrength(1f), 0f)
        assertEquals(0.2f, clampTouchHighlightStrength(0.2f), 0f)
        assertEquals(0f, clampTouchHighlightStrength(Float.NaN), 0f)
        assertEquals(0f, clampTouchHighlightStrength(Float.POSITIVE_INFINITY), 0f)
    }

    @Test
    fun highlightIsStrongestAtTouchPoint() {
        assertEquals(
            1f,
            calculateTouchHighlightIntensity(40f, 60f, 40f, 60f, 68f),
            0f,
        )
    }

    @Test
    fun highlightFadesToZeroAtRadius() {
        assertEquals(
            0f,
            calculateTouchHighlightIntensity(108f, 60f, 40f, 60f, 68f),
            0f,
        )
    }

    @Test
    fun highlightDecreasesAsDistanceIncreases() {
        val near = calculateTouchHighlightIntensity(45f, 60f, 40f, 60f, 68f)
        val far = calculateTouchHighlightIntensity(85f, 60f, 40f, 60f, 68f)

        assertTrue(near > far)
        assertTrue(far > 0f)
    }

    @Test
    fun invalidTouchGeometryReturnsZero() {
        assertEquals(0f, calculateTouchHighlightIntensity(1f, 1f, 1f, 1f, 0f), 0f)
        assertEquals(0f, calculateTouchHighlightIntensity(Float.NaN, 1f, 1f, 1f, 10f), 0f)
    }
}
