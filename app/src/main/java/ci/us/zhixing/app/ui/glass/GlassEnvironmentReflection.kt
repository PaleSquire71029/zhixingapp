package ci.us.zhixing.app.ui.glass

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Draws a restrained ambient-light reflection wash inside the current glass
 * surface bounds. It uses a few cached-style gradients instead of a full
 * frame capture, per-pixel shader, or continuously running animation.
 *
 * The cool / violet / warm palette follows the Zhixing light background and
 * is clipped by the caller's glass surface.
 */
internal fun DrawScope.drawEnvironmentReflection() {
    val width = size.width
    val height = size.height
    val shortestSide = size.minDimension
    if (width <= 0f || height <= 0f || shortestSide <= 0f) return

    drawRect(
        brush = Brush.linearGradient(
            0f to Color(0x168FD7F0),
            0.48f to Color(0x10B2A5E8),
            1f to Color(0x16F0C7A8),
            start = Offset.Zero,
            end = Offset(width, height),
        ),
    )

    val coolCenter = Offset(width * 0.16f, height * 0.08f)
    val coolRadius = shortestSide * 0.68f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x18FFFFFF), Color.Transparent),
            center = coolCenter,
            radius = coolRadius,
        ),
        radius = coolRadius,
        center = coolCenter,
    )

    val warmCenter = Offset(width * 0.88f, height * 0.92f)
    val warmRadius = shortestSide * 0.58f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x12E9C7E8), Color.Transparent),
            center = warmCenter,
            radius = warmRadius,
        ),
        radius = warmRadius,
        center = warmCenter,
    )

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color(0x0FFFFFFF),
                Color.Transparent,
            ),
            start = Offset(0f, height * 0.12f),
            end = Offset(width, height * 0.42f),
        ),
    )
}


/**
 * A local specular glow driven by the latest pointer position.
 *
 * Alpha is animated by the caller; this drawing helper stays stateless and
 * does not start its own animation or allocate an off-screen bitmap.
 */
internal fun DrawScope.drawTouchGlow(
    center: Offset,
    alpha: Float,
) {
    if (
        !center.x.isFinite() ||
        !center.y.isFinite() ||
        !alpha.isFinite() ||
        alpha <= 0f ||
        size.minDimension <= 0f
    ) return

    val strength = alpha.coerceIn(0f, 0.32f)
    val radius = size.minDimension * 0.72f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = strength),
                Color(0xFFB8E7FF).copy(alpha = strength * 0.62f),
                Color(0xFFC9C8F5).copy(alpha = strength * 0.24f),
                Color.Transparent,
            ),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}
