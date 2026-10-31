package ci.us.zhixing.app.ui.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GlassPerformanceTierTest {
    @Test
    fun premiumEnablesFullEffectsOnApi33() {
        val config = resolveGlassPerformanceConfig(GlassPerformanceTier.PREMIUM, 33)

        assertTrue(config.dynamicSamplingEnabled)
        assertEquals(10f, config.blurRadiusDp, 0f)
        assertTrue(config.refractionEnabled)
        assertEquals(3.5f, config.refractionStrengthPx, 0f)
        assertTrue(config.edgeHighlightEnabled)
        assertFalse(config.chromaticDispersionEnabled)
        assertTrue(config.touchFeedbackEnabled)
        assertTrue(config.backdropCaptureEnabled)
    }

    @Test
    fun standardUsesModerateEffectsAndStaticSampling() {
        val config = resolveGlassPerformanceConfig(GlassPerformanceTier.STANDARD, 33)

        assertFalse(config.dynamicSamplingEnabled)
        assertEquals(6f, config.blurRadiusDp, 0f)
        assertTrue(config.refractionEnabled)
        assertEquals(1.8f, config.refractionStrengthPx, 0f)
        assertTrue(config.edgeHighlightEnabled)
        assertFalse(config.chromaticDispersionEnabled)
        assertTrue(config.backdropCaptureEnabled)
    }

    @Test
    fun liteKeepsOnlyStaticSnapshotAndLightOverlay() {
        val config = resolveGlassPerformanceConfig(GlassPerformanceTier.LITE, 33)

        assertFalse(config.dynamicSamplingEnabled)
        assertEquals(0f, config.blurRadiusDp, 0f)
        assertFalse(config.refractionEnabled)
        assertFalse(config.edgeHighlightEnabled)
        assertFalse(config.chromaticDispersionEnabled)
        assertFalse(config.touchFeedbackEnabled)
        assertTrue(config.backdropCaptureEnabled)
        assertEquals(0.78f, config.panelOverlayAlpha, 0f)
    }

    @Test
    fun offDoesNotCaptureOrRunGlassEffects() {
        val config = resolveGlassPerformanceConfig(GlassPerformanceTier.OFF, 35)

        assertFalse(config.backdropCaptureEnabled)
        assertFalse(config.dynamicSamplingEnabled)
        assertFalse(config.refractionEnabled)
        assertFalse(config.edgeHighlightEnabled)
        assertFalse(config.chromaticDispersionEnabled)
        assertFalse(config.touchFeedbackEnabled)
        assertEquals(0.96f, config.panelOverlayAlpha, 0f)
    }

    @Test
    fun olderAndroidVersionsAutomaticallyDisableUnsupportedEffects() {
        val api30 = resolveGlassPerformanceConfig(GlassPerformanceTier.PREMIUM, 30)
        val api31 = resolveGlassPerformanceConfig(GlassPerformanceTier.STANDARD, 31)
        val api32 = resolveGlassPerformanceConfig(GlassPerformanceTier.STANDARD, 32)

        assertEquals(0f, api30.blurRadiusDp, 0f)
        assertEquals(0f, api31.refractionStrengthPx, 0f)
        assertFalse(api32.refractionEnabled)
        assertEquals(6f, api32.blurRadiusDp, 0f)
        assertTrue(api32.backdropCaptureEnabled)
    }
}
