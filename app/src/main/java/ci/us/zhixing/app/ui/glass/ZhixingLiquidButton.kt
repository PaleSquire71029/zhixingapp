package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ci.us.zhixing.app.ui.glass.backdrop106.components.LiquidButton
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
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
    content: @Composable RowScope.() -> Unit,
) {
    LiquidButton(
        onClick = onClick,
        backdrop = backdrop,
        modifier = modifier,
        enabled = enabled,
        isInteractive = true,
        shape = ZhixingGlassShapes.compactControl,
        surfaceColor = Color.White.copy(alpha = 0.10f),
        content = content,
    )
}
