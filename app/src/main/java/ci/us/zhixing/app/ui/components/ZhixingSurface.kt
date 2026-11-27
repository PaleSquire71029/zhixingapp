package ci.us.zhixing.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/**
 * Light, non-glass content surface.
 *
 * This component deliberately does not create a Backdrop or apply blur /
 * refraction. Floating glass remains owned by ui.glass and its shared host.
 */
@Composable
internal fun ZhixingSurface(
    modifier: Modifier = Modifier,
    shape: Shape = ZhixingShapes.card,
    containerColor: Color = ZhixingTheme.colors.surface,
    contentColor: Color = ZhixingTheme.colors.onSurface,
    borderColor: Color? = ZhixingTheme.colors.outline.copy(alpha = 0.55f),
    elevation: Dp = ZhixingTheme.elevation.flat,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = elevation,
        border = borderColor?.let { BorderStroke(1.dp, it) },
        content = content,
    )
}
