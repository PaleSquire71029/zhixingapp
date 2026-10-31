package ci.us.zhixing.app.ui.lab

import kotlin.math.sqrt

internal data class RefractionOffset(
    val x: Float,
    val y: Float,
)

internal fun clampRefractionStrength(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0f, 8f) else 0f

/**
 * Mirrors the AGSL lens mask while calculating the displacement direction in
 * physical pixels. This keeps the displacement isotropic on non-square panels.
 */
internal fun calculateRefractionOffset(
    fragX: Float,
    fragY: Float,
    width: Float,
    height: Float,
    strengthPx: Float,
): RefractionOffset {
    if (
        !fragX.isFinite() || !fragY.isFinite() ||
        !width.isFinite() || !height.isFinite() ||
        width <= 0f || height <= 0f
    ) {
        return RefractionOffset(0f, 0f)
    }

    val strength = clampRefractionStrength(strengthPx)
    if (strength == 0f) return RefractionOffset(0f, 0f)

    val lensX = (fragX / width) * 2f - 1f
    val lensY = (fragY / height) * 2f - 1f
    val lensRadius = sqrt(lensX * lensX + lensY * lensY)
    val edge = smoothStep(0.48f, 1.15f, lensRadius)
    if (edge <= 0f) return RefractionOffset(0f, 0f)

    val pixelX = fragX - width * 0.5f
    val pixelY = fragY - height * 0.5f
    val pixelRadius = sqrt(pixelX * pixelX + pixelY * pixelY)
    if (pixelRadius <= 0f) return RefractionOffset(0f, 0f)

    val displacement = edge * strength
    return RefractionOffset(
        x = -(pixelX / pixelRadius) * displacement,
        y = -(pixelY / pixelRadius) * displacement,
    )
}

private fun smoothStep(edge0: Float, edge1: Float, value: Float): Float {
    val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}
