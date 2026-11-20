package ci.us.zhixing.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/** Compact, accessible single-selection control for two to four peer options. */
@Composable
internal fun ZhixingSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    require(options.isNotEmpty()) { "At least one segment is required." }
    require(selectedIndex in options.indices) { "selectedIndex must reference an existing segment." }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(ZhixingTheme.colors.surfaceVariant)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val containerColor by animateColorAsState(
                targetValue = if (selected) ZhixingTheme.colors.surface else Color.Transparent,
                animationSpec = tween(ZhixingTheme.motion.controlStateTransitionMillis),
                label = "zhixingSegmentContainer",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = ZhixingTheme.interaction.minimumTouchTarget)
                    .clip(CircleShape)
                    .background(containerColor, CircleShape)
                    .then(
                        if (selected) Modifier.border(
                            width = 1.dp,
                            color = ZhixingTheme.colors.outline.copy(alpha = 0.65f),
                            shape = CircleShape,
                        ) else Modifier,
                    )
                    .selectable(
                        selected = selected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelectedIndexChange(index) },
                    )
                    .testTag("zhixing-segment-$index")
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (enabled && selected) ZhixingTheme.colors.primary else {
                        ZhixingTheme.colors.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.38f)
                    },
                    style = ZhixingTheme.typography.sectionLabel,
                    maxLines = 1,
                )
            }
        }
    }
}
