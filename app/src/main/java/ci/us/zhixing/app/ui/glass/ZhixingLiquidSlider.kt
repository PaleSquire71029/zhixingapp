package ci.us.zhixing.app.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import ci.us.zhixing.app.ui.glass.backdrop106.components.LiquidSlider
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * Zhixing light-style adapter for the pinned Catalog LiquidSlider.
 *
 * Keeps value and callback readers stable while applying the app accent,
 * touch-feedback preference, accessibility label and original step behavior.
 */
@Composable
internal fun ZhixingLiquidSlider(
    backdrop: Backdrop,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    touchFeedbackEnabled: Boolean = true,
) {
    val valueState = rememberUpdatedState(value)
    val valueReader: () -> Float = remember { { valueState.value } }
    val callbackState = rememberUpdatedState(onValueChange)
    val callback: (Float) -> Unit = remember {
        { next -> callbackState.value(next) }
    }
    val finishState = rememberUpdatedState(onValueChangeFinished)
    val finish: () -> Unit = remember {
        { finishState.value() }
    }

    LiquidSlider(
        value = valueReader,
        onValueChange = callback,
        onValueChangeFinished = finish,
        valueRange = valueRange,
        steps = steps,
        visibilityThreshold = 0.1f,
        backdrop = backdrop,
        label = label,
        accentColor = ZhixingTheme.colors.primary,
        trackColor = ZhixingTheme.colors.onSurfaceVariant.copy(alpha = 0.18f),
        modifier = modifier,
        enabled = enabled,
        touchFeedbackEnabled = touchFeedbackEnabled,
    )
}
