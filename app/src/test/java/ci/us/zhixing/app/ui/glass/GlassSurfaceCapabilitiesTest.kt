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
                refractionEnabled = true,
                refractionStrengthDp = 1.8f,
            )

            assertTrue(capabilities.useBackdropSampling)
            assertFalse(capabilities.useBlur)
            assertFalse(capabilities.useRefraction)
            assertFalse(capabilities.useVibrancy)
        }
    }

    @Test
    fun api31And32EnableBlurWhenRadiusIsPositive() {
        listOf(31, 32).forEach { apiLevel ->
            val capabilities = resolveGlassSurfaceCapabilities(
                apiLevel = apiLevel,
                enabled = true,
                blurRadiusDp = 12f,
                refractionEnabled = true,
                refractionStrengthDp = 1.8f,
            )

            assertTrue(capabilities.useBackdropSampling)
            assertTrue(capabilities.useBlur)
            assertFalse(capabilities.useRefraction)
            assertTrue(capabilities.useVibrancy)
        }
    }

    @Test
    fun api33AndNewerKeepBackdropAndBlurAvailable() {
        listOf(33, 34, 35, 36, 37).forEach { apiLevel ->
            val capabilities = resolveGlassSurfaceCapabilities(
                apiLevel = apiLevel,
                enabled = true,
                blurRadiusDp = 12f,
                refractionEnabled = true,
                refractionStrengthDp = 1.8f,
            )

            assertTrue(capabilities.useBackdropSampling)
            assertTrue(capabilities.useBlur)
            assertTrue(capabilities.useRefraction)
            assertTrue(capabilities.useVibrancy)
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
        assertFalse(capabilities.useVibrancy)
    }

    @Test
    fun api33UsesVibrancyForRefractionEvenWithoutBlur() {
        val capabilities = resolveGlassSurfaceCapabilities(
            apiLevel = 33,
            enabled = true,
            blurRadiusDp = 0f,
            refractionEnabled = true,
            refractionStrengthDp = 1.8f,
        )

        assertFalse(capabilities.useBlur)
        assertTrue(capabilities.useRefraction)
        assertTrue(capabilities.useVibrancy)
    }

    @Test
    fun offModeDisablesBackdropSamplingAndBlur() {
        val capabilities = resolveGlassSurfaceCapabilities(
            apiLevel = 36,
            enabled = false,
            blurRadiusDp = 12f,
            refractionEnabled = true,
            refractionStrengthDp = 1.8f,
        )

        assertFalse(capabilities.useBackdropSampling)
        assertFalse(capabilities.useBlur)
        assertFalse(capabilities.useRefraction)
        assertFalse(capabilities.useVibrancy)
    }

    @Test
    fun disabledOrZeroRefractionDoesNotEnableLens() {
        val disabled = resolveGlassSurfaceCapabilities(
            apiLevel = 36,
            enabled = true,
            blurRadiusDp = 0f,
            refractionEnabled = false,
            refractionStrengthDp = 1.8f,
        )
        val zeroStrength = resolveGlassSurfaceCapabilities(
            apiLevel = 36,
            enabled = true,
            blurRadiusDp = 0f,
            refractionEnabled = true,
            refractionStrengthDp = 0f,
        )

        assertFalse(disabled.useRefraction)
        assertFalse(disabled.useVibrancy)
        assertFalse(zeroStrength.useRefraction)
        assertFalse(zeroStrength.useVibrancy)
    }

    @Test
    fun refractionStrengthOutsideRangeAndNonFiniteValuesAreRejected() {
        listOf(-0.01f, 8.01f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)
            .forEach { strength ->
                assertThrows(IllegalArgumentException::class.java) {
                    resolveGlassSurfaceCapabilities(
                        apiLevel = 36,
                        enabled = true,
                        blurRadiusDp = 0f,
                        refractionEnabled = true,
                        refractionStrengthDp = strength,
                    )
                }
            }
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
