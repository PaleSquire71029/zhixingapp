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
}
