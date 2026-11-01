package ci.us.zhixing.app.ui.glass

/**
 * Resolved values consumed by the ported Catalog bottom bar.
 *
 * The public settings remain expressed in Zhixing's existing units and
 * preferences. This adapter translates them into the Catalog's optical
 * baseline without storing a second, competing set of material values.
 */
internal data class CatalogBottomBarMaterialParameters(
    val overlayAlpha: Float,
    val blurRadiusDp: Float,
    val useBackdropSampling: Boolean,
    val useBlur: Boolean,
    val useRefraction: Boolean,
    val refractionScale: Float,
    val chromaticDispersionEnabled: Boolean,
    val edgeHighlightEnabled: Boolean,
    val environmentReflectionEnabled: Boolean,
    val touchFeedbackEnabled: Boolean,
)

/**
 * Resolves Zhixing material settings for the pinned Catalog 1.0.6 tabs.
 *
 * A refraction strength of 1.8dp is the Zhixing home default and maps to the
 * Catalog's original lens scale (1x). The full 0–8dp range maps linearly up
 * to 1.5x, keeping the strongest user value useful without producing an
 * excessively stretched lens. Refraction remains API 33+ only, matching the
 * upstream RuntimeShader / lens capability policy.
 */
internal fun resolveCatalogBottomBarMaterialParameters(
    material: GlassMaterialState,
    apiLevel: Int,
): CatalogBottomBarMaterialParameters {
    val capabilities = resolveGlassSurfaceCapabilities(
        apiLevel = apiLevel,
        enabled = material.enabled,
        blurRadiusDp = material.blurRadiusDp,
        refractionEnabled = material.refractionEnabled,
        refractionStrengthDp = material.refractionStrengthDp,
    )

    val refractionScale = if (capabilities.useRefraction) {
        (
            material.refractionStrengthDp /
                GlassMaterialState.DEFAULT_REFRACTION_STRENGTH_DP
            ).coerceIn(0f, MAX_CATALOG_REFRACTION_SCALE)
    } else {
        0f
    }

    return CatalogBottomBarMaterialParameters(
        overlayAlpha = resolveGlassOverlayAlpha(material.transparencyPercent),
        blurRadiusDp = if (capabilities.useBlur) material.blurRadiusDp else 0f,
        useBackdropSampling = capabilities.useBackdropSampling,
        useBlur = capabilities.useBlur,
        useRefraction = capabilities.useRefraction,
        refractionScale = refractionScale,
        chromaticDispersionEnabled =
            capabilities.useRefraction && material.chromaticDispersionEnabled,
        edgeHighlightEnabled = material.enabled && material.edgeHighlightEnabled,
        environmentReflectionEnabled =
            material.enabled && material.environmentReflectionEnabled,
        touchFeedbackEnabled = material.enabled && material.touchFeedbackEnabled,
    )
}

internal const val MAX_CATALOG_REFRACTION_SCALE = 1.5f
