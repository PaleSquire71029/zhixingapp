package ci.us.zhixing.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingMotionTokens
import ci.us.zhixing.app.ui.theme.ZhixingShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme

internal enum class ZhixingButtonVariant {
    PRIMARY,
    SECONDARY,
    TERTIARY,
}

/** Shared action control with explicit loading, disabled, selected and focus states. */
@Composable
internal fun ZhixingButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean = false,
    variant: ZhixingButtonVariant = ZhixingButtonVariant.PRIMARY,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !loading) {
            ZhixingMotionTokens.buttonPressScale
        } else {
            1f
        },
        animationSpec = spring(
            dampingRatio = ZhixingMotionTokens.pressSpringDampingRatio,
            stiffness = ZhixingMotionTokens.pressSpringStiffness,
        ),
        label = "zhixingButtonPress",
    )

    val shape = ZhixingShapes.compactControl
    val interactive = enabled && !loading
    val containerColor = when {
        !enabled -> ZhixingTheme.colors.surfaceVariant
        selected -> ZhixingTheme.palette.selectedContainer
        variant == ZhixingButtonVariant.PRIMARY -> ZhixingTheme.colors.primary
        variant == ZhixingButtonVariant.SECONDARY -> ZhixingTheme.colors.surface
        else -> Color.Transparent
    }
    val contentColor = when {
        !enabled -> ZhixingTheme.colors.onSurfaceVariant.copy(alpha = 0.38f)
        selected -> ZhixingTheme.colors.primary
        variant == ZhixingButtonVariant.PRIMARY -> ZhixingTheme.colors.onPrimary
        else -> ZhixingTheme.colors.primary
    }
    val borderWidth = when {
        focused -> ZhixingTheme.interaction.focusOutlineWidth
        variant == ZhixingButtonVariant.SECONDARY -> 1.dp
        else -> 0.dp
    }
    val borderColor = if (focused) ZhixingTheme.colors.primary else ZhixingTheme.colors.outline

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = ZhixingTheme.interaction.minimumTouchTarget)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor, shape)
            .border(borderWidth, borderColor, shape)
            .onFocusChanged { focused = it.isFocused }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = interactive,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {
                this.selected = selected
                if (loading) {
                    stateDescription = "加载中"
                } else if (selected) {
                    stateDescription = "已选中"
                }
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke()
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = contentColor,
                    strokeWidth = 2.dp,
                )
            }
            Text(
                text = label,
                color = contentColor,
                style = ZhixingTheme.typography.sectionLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            trailingContent?.invoke()
        }
    }
}
