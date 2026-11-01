package ci.us.zhixing.app.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BottomBarDragMathTest {
    @Test
    fun resolvesNearestTabFromCapsuleOffset() {
        assertEquals(0, resolveBottomBarDragIndex(0f, 100f, 4))
        assertEquals(1, resolveBottomBarDragIndex(60f, 100f, 4))
        assertEquals(1, resolveBottomBarDragIndex(149f, 100f, 4))
        assertEquals(2, resolveBottomBarDragIndex(150f, 100f, 4))
        assertEquals(3, resolveBottomBarDragIndex(999f, 100f, 4))
    }

    @Test
    fun clampsOffsetsOutsideTheNavigationRange() {
        assertEquals(0, resolveBottomBarDragIndex(-80f, 100f, 4))
        assertEquals(3, resolveBottomBarDragIndex(900f, 100f, 4))
    }

    @Test
    fun supportsSingleDestination() {
        assertEquals(0, resolveBottomBarDragIndex(400f, 100f, 1))
    }

    @Test
    fun rejectsInvalidGeometry() {
        assertThrows(IllegalArgumentException::class.java) {
            resolveBottomBarDragIndex(0f, 0f, 4)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveBottomBarDragIndex(Float.NaN, 100f, 4)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveBottomBarDragIndex(0f, 100f, 0)
        }
    }
    @Test
    fun velocityStretchScalesWithTabWidthsPerSecond() {
        assertEquals(0f, resolveVelocityDragStretch(0f, 100f), 0f)
        assertEquals(0.035f, resolveVelocityDragStretch(100f, 100f), 0.0001f)
        assertEquals(0.07f, resolveVelocityDragStretch(200f, 100f), 0.0001f)
    }

    @Test
    fun velocityStretchIsIndependentOfDisplayPixelScale() {
        assertEquals(
            resolveVelocityDragStretch(200f, 100f),
            resolveVelocityDragStretch(600f, 300f),
            0f,
        )
    }

    @Test
    fun velocityStretchIsBoundedForFastGestures() {
        assertEquals(0.12f, resolveVelocityDragStretch(2_000f, 100f), 0f)
    }

    @Test
    fun velocityStretchRejectsInvalidMeasurements() {
        assertThrows(IllegalArgumentException::class.java) {
            resolveVelocityDragStretch(Float.NaN, 100f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveVelocityDragStretch(Float.POSITIVE_INFINITY, 100f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveVelocityDragStretch(100f, 0f)
        }
    }

    @Test
    fun directionalStretchTracksMovementDirection() {
        val right = resolveDirectionalDragDeformation(
            deltaXPx = 8f,
            itemWidthPx = 100f,
        )
        val left = resolveDirectionalDragDeformation(
            deltaXPx = -8f,
            itemWidthPx = 100f,
        )

        assertEquals(1, right.direction)
        assertEquals(-1, left.direction)
        assertEquals(right.stretch, left.stretch, 0f)
        assertEquals(0.072f, right.stretch, 0.0001f)
    }

    @Test
    fun directionalStretchIgnoresJitterAndClampsLargeDeltas() {
        assertEquals(
            0f,
            resolveDirectionalDragDeformation(1f, 100f).stretch,
            0f,
        )
        assertEquals(
            0.12f,
            resolveDirectionalDragDeformation(500f, 100f).stretch,
            0f,
        )
    }

    @Test
    fun directionalStretchRejectsInvalidInputs() {
        assertThrows(IllegalArgumentException::class.java) {
            resolveDirectionalDragDeformation(Float.NaN, 100f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveDirectionalDragDeformation(10f, 0f)
        }
    }

}
