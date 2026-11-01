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
    val enabled: Boolean = true,
    val refractionEnabled: Boolean = DEFAULT_REFRACTION_ENABLED,
    val refractionStrengthDp: Float = DEFAULT_REFRACTION_STRENGTH_DP,
) {
    init {
        require(transparencyPercent in MIN_TRANSPARENCY_PERCENT..MAX_TRANSPARENCY_PERCENT) {
            "Transparency must be between 0 and 100 percent."
        }
        require(blurRadiusDp.isFinite() && blurRadiusDp in MIN_BLUR_RADIUS_DP..MAX_BLUR_RADIUS_DP) {
            "Blur radius must be finite and between 0 and 24 dp."
        }
        require(refractionStrengthDp.isFinite() &&
            refractionStrengthDp in MIN_REFRACTION_STRENGTH_DP..MAX_REFRACTION_STRENGTH_DP
        ) {
            "Refraction strength must be finite and between 0 and 8 dp."
        }
    }

    companion object {
        const val MIN_TRANSPARENCY_PERCENT = 0
        const val MAX_TRANSPARENCY_PERCENT = 100
        const val DEFAULT_TRANSPARENCY_PERCENT = 78

        const val MIN_BLUR_RADIUS_DP = 0f
        const val MAX_BLUR_RADIUS_DP = 24f
        const val DEFAULT_BLUR_RADIUS_DP = 12f
        const val MIN_REFRACTION_STRENGTH_DP = 0f
        const val MAX_REFRACTION_STRENGTH_DP = 8f
        const val DEFAULT_REFRACTION_ENABLED = true
        const val DEFAULT_REFRACTION_STRENGTH_DP = 1.8f
    }
}
