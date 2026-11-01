package ci.us.zhixing.app.ui.glass

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GlassTouchFeedbackTest {
    @Test
    fun mapsLocalPressIntoSharedBottomBarCoordinates() {
        val mapped = mapBottomBarTouchPosition(
            itemIndex = 2,
            itemCount = 4,
            itemWidthPx = 100f,
            localPosition = Offset(24f, 18f),
        )

        assertEquals(224f, mapped.x, 0f)
        assertEquals(18f, mapped.y, 0f)
    }

    @Test
    fun rejectsInvalidItemIndexAndWidth() {
        assertThrows(IllegalArgumentException::class.java) {
            mapBottomBarTouchPosition(4, 4, 100f, Offset(1f, 1f))
        }
        assertThrows(IllegalArgumentException::class.java) {
            mapBottomBarTouchPosition(0, 4, Float.NaN, Offset(1f, 1f))
        }
    }

    @Test
    fun rejectsNonFinitePointerCoordinates() {
        assertThrows(IllegalArgumentException::class.java) {
            mapBottomBarTouchPosition(
                itemIndex = 0,
                itemCount = 4,
                itemWidthPx = 100f,
                localPosition = Offset(Float.NaN, 1f),
            )
        }
    }
}
