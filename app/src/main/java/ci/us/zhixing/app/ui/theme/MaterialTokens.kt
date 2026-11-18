package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.graphics.Color

/** Named surface roles in the Zhixing Flow material language. */
internal enum class ZhixingMaterialRole {
    CANVAS,
    SOFT_SURFACE,
    FLOW_GLASS,
    OVERLAY,
}

/**
 * Material policy only. Backdrop rendering and GlassMaterialState remain in
 * ui.glass; F1 does not introduce another renderer or glass implementation.
 */
internal object ZhixingMaterialTokens {
    val canvasColor: Color get() = ZhixingColorTokens.background
    val softSurfaceColor: Color get() = ZhixingColorTokens.surface
    val softSurfaceVariantColor: Color get() = ZhixingColorTokens.surfaceVariant

    val canvasRole = ZhixingMaterialRole.CANVAS
    val contentRole = ZhixingMaterialRole.SOFT_SURFACE
    val floatingRole = ZhixingMaterialRole.FLOW_GLASS
    val overlayRole = ZhixingMaterialRole.OVERLAY

    const val regularContentUsesFlowGlass = false
    const val mathContentAllowsRefraction = false
    const val sharedBackdropHostPerPage = true
    const val transparencyAndBlurAreIndependent = true
}
