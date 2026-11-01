package ci.us.zhixing.app.ui.glass

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassSurfaceCapabilitiesTest {
    @Test
    fun api26Through30KeepBackdropButDisableBlur() {
        listOf(26, 28, 29, 30).forEach { apiLevel ->
            val capabilities = resolveGlassSurfaceCapabilities(
                apiLevel = apiLevel,
                enabled = true,
                blurRadiusDp = 12f,
            )

            assertTrue(capabilities.useBackdropSampling)
            assertFalse(capabilities.useBlur)
        }
    }

    @Test
    fun api31And32EnableBlurWhenRadiusIsPositive() {
        listOf(31, 32).forEach { apiLevel ->
            val capabilities = resolveGlassSurfaceCapabilities(
                apiLevel = apiLevel,
                enabled = true,
                blurRadiusDp = 12f,
            )

            assertTrue(capabilities.useBackdropSampling)
            assertTrue(capabilities.useBlur)
        }
    }

    @Test
    fun api33AndNewerKeepBackdropAndBlurAvailable() {
        listOf(33, 34, 35, 36, 37).forEach { apiLevel ->
            val capabilities = resolveGlassSurfaceCapabilities(
                apiLevel = apiLevel,
                enabled = true,
                blurRadiusDp = 12f,
            )

            assertTrue(capabilities.useBackdropSampling)
            assertTrue(capabilities.useBlur)
        }
    }

    @Test
    fun zeroBlurDoesNotEnableBlurCalculation() {
        val capabilities = resolveGlassSurfaceCapabilities(
            apiLevel = 36,
            enabled = true,
            blurRadiusDp = 0f,
        )

        assertTrue(capabilities.useBackdropSampling)
        assertFalse(capabilities.useBlur)
    }

    @Test
    fun offModeDisablesBackdropSamplingAndBlur() {
        val capabilities = resolveGlassSurfaceCapabilities(
            apiLevel = 36,
            enabled = false,
            blurRadiusDp = 12f,
        )

        assertFalse(capabilities.useBackdropSampling)
        assertFalse(capabilities.useBlur)
    }

    @Test
    fun blurOutsideMaterialRangeIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            resolveGlassSurfaceCapabilities(36, true, -0.01f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            resolveGlassSurfaceCapabilities(36, true, 24.01f)
        }
    }

    @Test
    fun nonFiniteBlurIsRejected() {
        listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).forEach { radius ->
            assertThrows(IllegalArgumentException::class.java) {
                resolveGlassSurfaceCapabilities(36, true, radius)
            }
        }
    }
}
