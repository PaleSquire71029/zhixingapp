package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassBackdropCropTest {
    @Test
    fun centeredPanelCropUsesExpectedInsetsAndVerticalCenter() {
        val crop = calculateBackdropCrop(
            imageWidth = 1080,
            imageHeight = 960,
            panelInset = 60,
            panelHeight = 564,
        )

        assertEquals(BackdropCropRect(left = 60, top = 198, width = 960, height = 564), crop)
    }

    @Test
    fun cropStaysInsideVerySmallSnapshotAndUsesAvailablePixels() {
        val crop = calculateBackdropCrop(
            imageWidth = 20,
            imageHeight = 14,
            panelInset = 100,
            panelHeight = 188,
        ) ?: error("A positive image should produce a crop")

        assertEquals(BackdropCropRect(left = 9, top = 0, width = 2, height = 14), crop)
        assertTrue(crop.left >= 0)
        assertTrue(crop.top >= 0)
        assertTrue(crop.left + crop.width <= 20)
        assertTrue(crop.top + crop.height <= 14)
    }

    @Test
    fun invalidSnapshotDimensionsReturnNull() {
        assertNull(calculateBackdropCrop(0, 100, 10, 20))
        assertNull(calculateBackdropCrop(100, -1, 10, 20))
        assertNull(calculateBackdropCrop(100, 100, 10, 0))
    }
}
