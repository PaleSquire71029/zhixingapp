package ci.us.zhixing.app.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import ci.us.zhixing.app.ui.glass.backdrop106.components.LiquidToggle
import com.kyant.backdrop.Backdrop

/**
 * Zhixing adapter for the pinned Catalog liquid toggle.
 * Keeps the selected reader stable across recompositions so the upstream
 * animation follows state changes instead of resetting its remembered state.
 */
@Composable
internal fun ZhixingLiquidToggle(
    backdrop: Backdrop,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    touchFeedbackEnabled: Boolean = true,
) {
    val checkedState = rememberUpdatedState(checked)
    val selectedReader: () -> Boolean = remember {
        { checkedState.value }
    }
    val callbackState = rememberUpdatedState(onCheckedChange)
    val onSelect: (Boolean) -> Unit = remember {
        { value -> callbackState.value(value) }
    }

    LiquidToggle(
        selected = selectedReader,
        onSelect = onSelect,
        backdrop = backdrop,
        modifier = modifier,
        label = label,
        enabled = enabled,
        touchFeedbackEnabled = touchFeedbackEnabled,
    )
}
