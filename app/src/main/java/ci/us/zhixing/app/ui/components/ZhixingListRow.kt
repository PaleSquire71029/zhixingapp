package ci.us.zhixing.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

/** Reusable single-line / supporting-text row for menus and grouped lists. */
@Composable
internal fun ZhixingListRow(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) ZhixingMotionTokens.cardPressScale else 1f,
        animationSpec = spring(
            dampingRatio = ZhixingMotionTokens.pressSpringDampingRatio,
            stiffness = ZhixingMotionTokens.pressSpringStiffness,
        ),
        label = "zhixingListRowPress",
    )
    val targetColor = when {
        !enabled -> ZhixingTheme.colors.surfaceVariant
        selected -> ZhixingTheme.palette.selectedContainer
        pressed -> ZhixingTheme.colors.surfaceVariant
        else -> ZhixingTheme.colors.surface
    }
    val containerColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(ZhixingMotionTokens.controlStateTransitionMillis),
        label = "zhixingListRowContainer",
    )
    val rowShape = ZhixingShapes.feature
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = ZhixingTheme.interaction.minimumTouchTarget)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(rowShape)
            .background(containerColor, rowShape)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = ZhixingTheme.colors.primary.copy(alpha = 0.28f),
                shape = rowShape,
            )
            .then(clickModifier)
            .semantics(mergeDescendants = true) {
                this.selected = selected
                if (selected) stateDescription = "已选中"
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leadingContent?.invoke()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                color = if (enabled) ZhixingTheme.colors.onSurface else {
                    ZhixingTheme.colors.onSurfaceVariant.copy(alpha = 0.38f)
                },
                style = ZhixingTheme.typography.featureTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                    style = ZhixingTheme.typography.supporting,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailingContent?.invoke()
    }
}
