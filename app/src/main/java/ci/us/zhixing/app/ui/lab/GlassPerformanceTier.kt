package ci.us.zhixing.app.ui.lab

/**
 * Glass Lab performance presets.
 * CUSTOM is selected after a user changes any individual control.
 */
internal enum class GlassPerformanceTier {
    PREMIUM,
    STANDARD,
    LITE,
    OFF,
    CUSTOM,
}

internal data class GlassPerformanceConfig(
    val blurRadiusDp: Float,
    val refractionEnabled: Boolean,
    val refractionStrengthPx: Float,
    val edgeHighlightEnabled: Boolean,
    val chromaticDispersionEnabled: Boolean,
    val chromaticDispersionStrengthPx: Float,
    val touchFeedbackEnabled: Boolean,
    val panelOverlayAlpha: Float,
    val backdropEnabled: Boolean,
)

internal fun resolveGlassPerformanceConfig(
    tier: GlassPerformanceTier,
    apiLevel: Int,
): GlassPerformanceConfig {
    val blurSupported = apiLevel >= 31
    val shaderSupported = apiLevel >= 33

    return when (tier) {
        GlassPerformanceTier.PREMIUM -> GlassPerformanceConfig(
            blurRadiusDp = if (blurSupported) 10f else 0f,
            refractionEnabled = shaderSupported,
            refractionStrengthPx = if (shaderSupported) 3.5f else 0f,
            edgeHighlightEnabled = shaderSupported,
            chromaticDispersionEnabled = false,
            chromaticDispersionStrengthPx = 0.25f,
            touchFeedbackEnabled = true,
            panelOverlayAlpha = 0.22f,
            backdropEnabled = true,
        )

        GlassPerformanceTier.STANDARD -> GlassPerformanceConfig(
            blurRadiusDp = if (blurSupported) 6f else 0f,
            refractionEnabled = shaderSupported,
            refractionStrengthPx = if (shaderSupported) 1.8f else 0f,
            edgeHighlightEnabled = shaderSupported,
            chromaticDispersionEnabled = false,
            chromaticDispersionStrengthPx = 0.25f,
            touchFeedbackEnabled = true,
            panelOverlayAlpha = 0.30f,
            backdropEnabled = true,
        )

        GlassPerformanceTier.LITE -> GlassPerformanceConfig(
            blurRadiusDp = 0f,
            refractionEnabled = false,
            refractionStrengthPx = 0f,
            edgeHighlightEnabled = false,
            chromaticDispersionEnabled = false,
            chromaticDispersionStrengthPx = 0f,
            touchFeedbackEnabled = false,
            panelOverlayAlpha = 0.52f,
            backdropEnabled = true,
        )

        GlassPerformanceTier.OFF -> GlassPerformanceConfig(
            blurRadiusDp = 0f,
            refractionEnabled = false,
            refractionStrengthPx = 0f,
            edgeHighlightEnabled = false,
            chromaticDispersionEnabled = false,
            chromaticDispersionStrengthPx = 0f,
            touchFeedbackEnabled = false,
            panelOverlayAlpha = 0.96f,
            backdropEnabled = false,
        )

        GlassPerformanceTier.CUSTOM -> GlassPerformanceConfig(
            blurRadiusDp = 0f,
            refractionEnabled = false,
            refractionStrengthPx = 0f,
            edgeHighlightEnabled = false,
            chromaticDispersionEnabled = false,
            chromaticDispersionStrengthPx = 0f,
            touchFeedbackEnabled = false,
            panelOverlayAlpha = 0.30f,
            backdropEnabled = true,
        )
    }
}
