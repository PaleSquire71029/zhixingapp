package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

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

    @Test
    fun centerAndInnerLensAreaHaveNoDisplacement() {
        val center = calculateRefractionOffset(50f, 50f, 100f, 100f, 8f)
        val inner = calculateRefractionOffset(60f, 50f, 100f, 100f, 8f)

        assertEquals(0f, center.x, 0f)
        assertEquals(0f, center.y, 0f)
        assertEquals(0f, inner.x, 0f)
        assertEquals(0f, inner.y, 0f)
    }

    @Test
    fun displacementPointsTowardPanelCenterInPixelSpace() {
        val offset = calculateRefractionOffset(
            fragX = 150f,
            fragY = 75f,
            width = 200f,
            height = 100f,
            strengthPx = 4f,
        )

        assertTrue(offset.x < 0f)
        assertTrue(offset.y < 0f)
        assertEquals(2f, offset.x / offset.y, 0.0001f)
    }

    @Test
    fun displacementMagnitudeNeverExceedsConfiguredStrength() {
        val strength = 5.5f
        val offset = calculateRefractionOffset(
            fragX = 100f,
            fragY = 100f,
            width = 100f,
            height = 100f,
            strengthPx = strength,
        )
        val magnitude = sqrt(offset.x * offset.x + offset.y * offset.y)

        assertEquals(strength, magnitude, 0.0001f)
    }

    @Test
    fun invalidGeometryAndNonFiniteCoordinatesReturnZero() {
        assertEquals(
            RefractionOffset(0f, 0f),
            calculateRefractionOffset(10f, 10f, 0f, 100f, 4f),
        )
        assertEquals(
            RefractionOffset(0f, 0f),
            calculateRefractionOffset(Float.NaN, 10f, 100f, 100f, 4f),
        )
    }
}
