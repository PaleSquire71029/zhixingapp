package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop

/**
 * Static capsule shell for the future floating liquid-glass bottom bar.
 *
 * This step deliberately provides only the glass material and capsule outline.
 * Navigation items, selection state, positioning, insets, and animations are
 * added in separate steps so each interaction can be verified independently.
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
                color = Color.White.copy(alpha = 0.78f),
                cornerRadius = corner,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawRoundRect(
                color = Color(0xFFB8CFE0).copy(alpha = 0.22f),
                topLeft = androidx.compose.ui.geometry.Offset(1.dp.toPx(), 1.dp.toPx()),
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
