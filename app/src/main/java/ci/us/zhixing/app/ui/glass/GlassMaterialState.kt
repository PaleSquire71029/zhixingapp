package ci.us.zhixing.app.ui.glass

/**
 * Immutable user-facing settings for Zhixing's light glass material.
 *
 * Transparency and background blur are deliberately independent values:
 * changing one never derives or mutates the other.
 */
internal data class GlassMaterialState(
    val transparencyPercent: Int = DEFAULT_TRANSPARENCY_PERCENT,
    val blurRadiusDp: Float = DEFAULT_BLUR_RADIUS_DP,
) {
    init {
        require(transparencyPercent in MIN_TRANSPARENCY_PERCENT..MAX_TRANSPARENCY_PERCENT) {
            "Transparency must be between 0 and 100 percent."
        }
        require(blurRadiusDp.isFinite() && blurRadiusDp in MIN_BLUR_RADIUS_DP..MAX_BLUR_RADIUS_DP) {
            "Blur radius must be finite and between 0 and 24 dp."
        }
    }

    companion object {
        const val MIN_TRANSPARENCY_PERCENT = 0
        const val MAX_TRANSPARENCY_PERCENT = 100
        const val DEFAULT_TRANSPARENCY_PERCENT = 78

        const val MIN_BLUR_RADIUS_DP = 0f
        const val MAX_BLUR_RADIUS_DP = 24f
        const val DEFAULT_BLUR_RADIUS_DP = 12f
    }
}
