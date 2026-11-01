package ci.us.zhixing.app.ui.glass

/**
 * Optical parameters for the bottom-bar lens while a tab is being pressed.
 *
 * This scales the library's existing lens input rather than layering a second
 * visual effect over the glass surface.
 */
internal data class TouchRefractionParameters(
    val heightDp: Float,
    val amountDp: Float,
)

internal fun resolveTouchRefractionParameters(
    baseHeightDp: Float,
    baseAmountDp: Float,
    pressProgress: Float,
): TouchRefractionParameters {
    require(baseHeightDp.isFinite() && baseHeightDp > 0f) {
        "Base refraction height must be finite and positive."
    }
    require(baseAmountDp.isFinite() && baseAmountDp >= 0f) {
        "Base refraction amount must be finite and non-negative."
    }
    require(pressProgress.isFinite() && pressProgress in 0f..1f) {
        "Press progress must be finite and between 0 and 1."
    }

    return TouchRefractionParameters(
        heightDp = baseHeightDp + 4f * pressProgress,
        amountDp = baseAmountDp * (1f + 0.16f * pressProgress),
    )
}
