package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ci.us.zhixing.app.ui.glass.backdrop106.components.LiquidButton
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * Zhixing light-style adapter for the pinned Catalog LiquidButton.
 * Keeps the live Backdrop and upstream touch response while applying
 * the compact-control geometry used by toolbar actions.
 */
@Composable
internal fun ZhixingLiquidButton(
    backdrop: Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    touchFeedbackEnabled: Boolean = true,
    selected: Boolean = false,
    shape: Shape = ZhixingGlassShapes.compactControl,
    surfaceColor: Color = Color.White.copy(alpha = 0.10f),
    content: @Composable RowScope.() -> Unit,
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        enabled = enabled,
        isInteractive = touchFeedbackEnabled,
        shape = shape,
        surfaceColor = if (selected) ZhixingTheme.colors.primary.copy(alpha = 0.12f) else surfaceColor,
        content = content,
    )
}
