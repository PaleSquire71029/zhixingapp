package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * Shared glass capsule shell for the floating liquid-glass bottom bar.
 *
 * The shell owns the shared material and outline. Touch-driven optical
 * deformation is forwarded to the same Backdrop lens used by other surfaces.
 */
@Composable
internal fun ZhixingLiquidBottomBarSurface(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    material: GlassMaterialState = GlassMaterialState(
        transparencyPercent = 68,
        blurRadiusDp = 12f,
    ),
    refractionPressProgress: Float = 0f,
    content: @Composable BoxScope.() -> Unit,
) {
    val capsuleShape: Shape = RoundedCornerShape(percent = 50)
    val themeTint = ZhixingTheme.colors.primary
    val transparency = material.transparencyPercent / 100f
    val outerRimAlpha = 0.52f + transparency * 0.22f
    val innerRimAlpha = 0.08f + transparency * 0.12f

    ZhixingGlassSurface(
        backdrop = backdrop,
        material = material,
        modifier = modifier.drawWithContent {
            drawContent()

            val corner = CornerRadius(
                x = size.height / 2f,
                y = size.height / 2f,
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = outerRimAlpha),
                        themeTint.copy(alpha = innerRimAlpha),
                        Color.White.copy(alpha = outerRimAlpha * 0.72f),
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height),
                ),
                cornerRadius = corner,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = innerRimAlpha * 0.55f),
                        themeTint.copy(alpha = innerRimAlpha),
                        Color.White.copy(alpha = innerRimAlpha * 0.38f),
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f),
                ),
                topLeft = Offset(1.dp.toPx(), 1.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(
                    width = (size.width - 2.dp.toPx()).coerceAtLeast(0f),
                    height = (size.height - 2.dp.toPx()).coerceAtLeast(0f),
                ),
                cornerRadius = CornerRadius(
                    x = (size.height / 2f - 1.dp.toPx()).coerceAtLeast(0f),
                    y = (size.height / 2f - 1.dp.toPx()).coerceAtLeast(0f),
                ),
                style = Stroke(width = 0.5.dp.toPx()),
            )
        },
        shape = capsuleShape,
        refractionPressProgress = refractionPressProgress,
        content = content,
    )
}
