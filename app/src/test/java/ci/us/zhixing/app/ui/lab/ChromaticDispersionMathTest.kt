package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class ChromaticDispersionMathTest {
    @Test
    fun strengthIsClampedToSubPixelRange() {
        assertEquals(0f, clampChromaticDispersionStrength(-1f), 0f)
        assertEquals(0.8f, clampChromaticDispersionStrength(2f), 0f)
        assertEquals(0.25f, clampChromaticDispersionStrength(0.25f), 0f)
        assertEquals(0f, clampChromaticDispersionStrength(Float.NaN), 0f)
        assertEquals(0f, clampChromaticDispersionStrength(Float.POSITIVE_INFINITY), 0f)
    }

    @Test
    fun centerAndInnerAreaHaveNoDispersion() {
        val center = calculateChromaticDispersionOffset(100f, 50f, 200f, 100f, 0.8f)
        val inner = calculateChromaticDispersionOffset(110f, 50f, 200f, 100f, 0.8f)

        assertEquals(RefractionOffset(0f, 0f), center)
        assertEquals(RefractionOffset(0f, 0f), inner)
    }

    @Test
    fun maximumEdgeOffsetNeverExceedsPointEightPixels() {
        val offset = calculateChromaticDispersionOffset(
            fragX = 200f,
            fragY = 100f,
            width = 200f,
            height = 100f,
            strengthPx = 5f,
        )
        val magnitude = sqrt(offset.x * offset.x + offset.y * offset.y)

        assertEquals(0.8f, magnitude, 0.0001f)
    }

    @Test
    fun offsetUsesPhysicalPixelDirectionOnWidePanels() {
        val offset = calculateChromaticDispersionOffset(
            fragX = 200f,
            fragY = 50f,
            width = 200f,
            height = 100f,
            strengthPx = 0.8f,
        )

        assertTrue(offset.x > 0f)
        assertEquals(0f, offset.y, 0.0001f)
    }

    @Test
    fun invalidGeometryReturnsZeroOffset() {
        assertEquals(
            RefractionOffset(0f, 0f),
            calculateChromaticDispersionOffset(5f, 5f, 0f, 100f, 0.3f),
        )
        assertEquals(
            RefractionOffset(0f, 0f),
            calculateChromaticDispersionOffset(Float.NaN, 5f, 100f, 100f, 0.3f),
        )
    }
}
