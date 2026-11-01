package ci.us.zhixing.app.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GlassMaterialStateTest {
    @Test
    fun defaultsUseTheDocumentedTransparencyAndBlur() {
        val state = GlassMaterialState()

        assertEquals(78, state.transparencyPercent)
        assertEquals(12f, state.blurRadiusDp, 0f)
    }

    @Test
    fun transparencyAcceptsBothInclusiveBoundaries() {
        assertEquals(0, GlassMaterialState(transparencyPercent = 0).transparencyPercent)
        assertEquals(100, GlassMaterialState(transparencyPercent = 100).transparencyPercent)
    }

    @Test
    fun blurAcceptsBothInclusiveBoundaries() {
        assertEquals(0f, GlassMaterialState(blurRadiusDp = 0f).blurRadiusDp, 0f)
        assertEquals(24f, GlassMaterialState(blurRadiusDp = 24f).blurRadiusDp, 0f)
    }

    @Test
    fun transparencyAndBlurCanBeConfiguredIndependently() {
        val state = GlassMaterialState(
            transparencyPercent = 35,
            blurRadiusDp = 18f,
        )

        assertEquals(35, state.transparencyPercent)
        assertEquals(18f, state.blurRadiusDp, 0f)
    }

    @Test
    fun transparencyOutsideRangeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(transparencyPercent = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(transparencyPercent = 101)
        }
    }

    @Test
    fun blurOutsideRangeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(blurRadiusDp = -0.01f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(blurRadiusDp = 24.01f)
        }
    }

    @Test
    fun nonFiniteBlurValuesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(blurRadiusDp = Float.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(blurRadiusDp = Float.POSITIVE_INFINITY)
        }
        assertThrows(IllegalArgumentException::class.java) {
            GlassMaterialState(blurRadiusDp = Float.NEGATIVE_INFINITY)
        }
    }
}
