package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Interaction states supported by the shared Zhixing component language. */
internal enum class ZhixingInteractionState {
    IDLE,
    PRESSED,
    SELECTED,
    DISABLED,
    FOCUSED,
    LOADING,
    ERROR,
}

/** Shared touch-target, state-layer and selected-state constraints. */
internal object ZhixingInteractionTokens {
    val minimumTouchTarget: Dp = 48.dp
    val focusOutlineWidth: Dp = 2.dp

    const val pressedStateLayerAlpha = 0.12f
    const val selectedContainerAlpha = 0.12f
    const val disabledContentAlpha = 0.38f
    const val touchFeedbackCanBeDisabled = true
}
