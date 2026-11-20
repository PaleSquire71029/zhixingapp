package ci.us.zhixing.app.ui.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignGalleryLayoutTest {
    @Test
    fun galleryUsesSingleColumnBelowWideBreakpoint() {
        assertFalse(isWideGalleryLayout(599.dp))
    }

    @Test
    fun gallerySwitchesToTwoColumnsAtWideBreakpoint() {
        assertTrue(isWideGalleryLayout(600.dp))
        assertTrue(isWideGalleryLayout(720.dp))
    }
}
