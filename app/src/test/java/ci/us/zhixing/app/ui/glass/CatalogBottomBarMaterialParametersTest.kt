package ci.us.zhixing.app.ui.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogBottomBarMaterialParametersTest {
    @Test
    fun homeDefaultsPreserveCatalogLensScaleAndZhixingMaterialValues() {
        val parameters = resolveCatalogBottomBarMaterialParameters(
            material = GlassMaterialState(
                transparencyPercent = 68,
                blurRadiusDp = 12f,
                refractionStrengthDp = 1.8f,
            ),
            apiLevel = 35,
        )

        assertEquals(0.32f, parameters.overlayAlpha, 0.0001f)
        assertEquals(12f, parameters.blurRadiusDp, 0.0001f)
        assertTrue(parameters.useBackdropSampling)
        assertTrue(parameters.useBlur)
        assertTrue(parameters.useRefraction)
        assertEquals(1f, parameters.refractionScale, 0.0001f)
    }

    @Test
    fun transparencyAndBlurRemainIndependent() {
        val baseline = GlassMaterialState(
            transparencyPercent = 68,
            blurRadiusDp = 12f,
        )
        val moreTransparent = resolveCatalogBottomBarMaterialParameters(
            material = baseline.copy(transparencyPercent = 90),
            apiLevel = 35,
        )
        val moreBlurred = resolveCatalogBottomBarMaterialParameters(
            material = baseline.copy(blurRadiusDp = 20f),
            apiLevel = 35,
        )

        assertEquals(0.10f, moreTransparent.overlayAlpha, 0.0001f)
        assertEquals(12f, moreTransparent.blurRadiusDp, 0.0001f)
        assertEquals(0.32f, moreBlurred.overlayAlpha, 0.0001f)
        assertEquals(20f, moreBlurred.blurRadiusDp, 0.0001f)
    }

    @Test
    fun refractionStrengthScalesFromZeroToBoundedCatalogMaximum() {
        val zero = resolveCatalogBottomBarMaterialParameters(
            GlassMaterialState(refractionStrengthDp = 0f),
            apiLevel = 35,
        )
        val normal = resolveCatalogBottomBarMaterialParameters(
            GlassMaterialState(refractionStrengthDp = 1.8f),
            apiLevel = 35,
        )
        val maximum = resolveCatalogBottomBarMaterialParameters(
            GlassMaterialState(refractionStrengthDp = 8f),
            apiLevel = 35,
        )

        assertFalse(zero.useRefraction)
        assertEquals(0f, zero.refractionScale, 0.0001f)
        assertEquals(1f, normal.refractionScale, 0.0001f)
        assertEquals(MAX_CATALOG_REFRACTION_SCALE, maximum.refractionScale, 0.0001f)
    }

    @Test
    fun opticalSettingsRespectAndroidApiCapabilities() {
        val material = GlassMaterialState(
            blurRadiusDp = 12f,
            refractionEnabled = true,
            refractionStrengthDp = 1.8f,
        )

        val api26 = resolveCatalogBottomBarMaterialParameters(material, apiLevel = 26)
        val api31 = resolveCatalogBottomBarMaterialParameters(material, apiLevel = 31)
        val api33 = resolveCatalogBottomBarMaterialParameters(material, apiLevel = 33)

        assertTrue(api26.useBackdropSampling)
        assertFalse(api26.useBlur)
        assertFalse(api26.useVibrancy)
        assertFalse(api26.useRefraction)
        assertEquals(0f, api26.blurRadiusDp, 0.0001f)

        assertTrue(api31.useBlur)
        assertTrue(api31.useVibrancy)
        assertFalse(api31.useRefraction)
        assertEquals(12f, api31.blurRadiusDp, 0.0001f)

        assertTrue(api33.useRefraction)
        assertEquals(1f, api33.refractionScale, 0.0001f)
    }

    @Test
    fun opticalTogglesAreMappedIndependently() {
        val parameters = resolveCatalogBottomBarMaterialParameters(
            material = GlassMaterialState(
                refractionEnabled = false,
                chromaticDispersionEnabled = true,
                edgeHighlightEnabled = false,
                environmentReflectionEnabled = false,
                touchFeedbackEnabled = false,
            ),
            apiLevel = 35,
        )

        assertFalse(parameters.useRefraction)
        assertFalse(parameters.useVibrancy)
        assertFalse(parameters.chromaticDispersionEnabled)
        assertFalse(parameters.edgeHighlightEnabled)
        assertFalse(parameters.environmentReflectionEnabled)
        assertFalse(parameters.touchFeedbackEnabled)
    }

    @Test
    fun glassOffDisablesSamplingAndEveryOpticalFeedbackFeature() {
        val parameters = resolveCatalogBottomBarMaterialParameters(
            material = GlassMaterialState(enabled = false),
            apiLevel = 35,
        )

        assertFalse(parameters.useBackdropSampling)
        assertFalse(parameters.useBlur)
        assertFalse(parameters.useRefraction)
        assertEquals(0f, parameters.blurRadiusDp, 0.0001f)
        assertEquals(0f, parameters.refractionScale, 0.0001f)
        assertFalse(parameters.edgeHighlightEnabled)
        assertFalse(parameters.environmentReflectionEnabled)
        assertFalse(parameters.touchFeedbackEnabled)
    }
}
