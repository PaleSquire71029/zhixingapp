package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeHighlightMathTest {
    @Test
    fun highlightIsStrongestAlongTheTopEdge() {
        val top = calculateEdgeHighlightIntensity(
            fragX = 100f,
            fragY = 0f,
            width = 200f,
            height = 100f,
            rimWidthPx = 2f,
            rimSoftnessPx = 4f,
        )
        val bottom = calculateEdgeHighlightIntensity(
            fragX = 100f,
            fragY = 100f,
            width = 200f,
            height = 100f,
            rimWidthPx = 2f,
            rimSoftnessPx = 4f,
        )

        assertTrue(top > bottom)
        assertEquals(0.92f, top, 0.0001f)
    }

    @Test
    fun leftEdgeIsBrighterThanRightEdge() {
        val left = calculateEdgeHighlightIntensity(
            fragX = 0f,
            fragY = 50f,
            width = 200f,
            height = 100f,
            rimWidthPx = 2f,
            rimSoftnessPx = 4f,
        )
        val right = calculateEdgeHighlightIntensity(
            fragX = 200f,
            fragY = 50f,
            width = 200f,
            height = 100f,
            rimWidthPx = 2f,
            rimSoftnessPx = 4f,
        )

        assertEquals(0.68f, left, 0.0001f)
        assertEquals(0.12f, right, 0.0001f)
        assertTrue(left > right)
    }

    @Test
    fun innerPanelAreaHasNoRimHighlight() {
        val center = calculateEdgeHighlightIntensity(
            fragX = 100f,
            fragY = 50f,
            width = 200f,
            height = 100f,
            rimWidthPx = 2f,
            rimSoftnessPx = 4f,
        )
        assertEquals(0f, center, 0f)
    }

    @Test
    fun invalidGeometryReturnsNoHighlight() {
        assertEquals(
            0f,
            calculateEdgeHighlightIntensity(10f, 10f, 0f, 100f, 2f, 4f),
            0f,
        )
        assertEquals(
            0f,
            calculateEdgeHighlightIntensity(Float.NaN, 10f, 100f, 100f, 2f, 4f),
            0f,
        )
    }
}
