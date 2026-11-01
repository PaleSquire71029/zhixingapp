package ci.us.zhixing.app.ui.glass

/**
 * Runtime rendering decisions for a single glass surface.
 *
 * Kept as a pure Kotlin policy so API fallbacks and the Off path can be tested
 * without Android framework or Compose runtime dependencies.
 */
internal data class GlassSurfaceCapabilities(
    val useBackdropSampling: Boolean,
    val useBlur: Boolean,
)

/**
 * Resolves the features that are safe and useful on the current Android API.
 *
 * Backdrop sampling remains available on API 26+; blur is enabled only on
 * API 31+ where Android RenderEffect support is available. Disabling glass
 * turns off both features so the composable can avoid attaching drawBackdrop.
 */
internal fun resolveGlassSurfaceCapabilities(
    apiLevel: Int,
    enabled: Boolean,
    blurRadiusDp: Float,
): GlassSurfaceCapabilities {
    require(blurRadiusDp.isFinite() && blurRadiusDp in 0f..GlassMaterialState.MAX_BLUR_RADIUS_DP) {
        "Blur radius must be finite and between 0 and 24 dp."
    }

    if (!enabled) {
        return GlassSurfaceCapabilities(
            useBackdropSampling = false,
            useBlur = false,
        )
    }

    return GlassSurfaceCapabilities(
        useBackdropSampling = true,
        useBlur = apiLevel >= 31 && blurRadiusDp > 0f,
    )
}
