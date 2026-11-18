package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Current app geometry and fixed-layer policy. Viewport flags describe the
 * F3 target contract only; they do not implement scrolling in F1.
 */
internal object ZhixingLayoutTokens {
    val pageHorizontalPadding: Dp = ZhixingSpacingTokens.pageHorizontal
    val pageVerticalPadding: Dp = ZhixingSpacingTokens.pageVertical
    val dockHeight: Dp = 68.dp

    const val dockOutsideScrollViewport = true
    const val systemBarsOutsideScrollViewport = true
    const val imeOutsideViewportTransform = true
    const val pageOverscrollUsesSingleViewportLayer = true
}
