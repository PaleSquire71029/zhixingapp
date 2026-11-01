package ci.us.zhixing.app.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * Four-destination content for the floating glass navigation shell.
 *
 * Selection and click handling are controlled by the caller. The selected
 * background moves as one shared capsule; rapid destination changes retarget
 * the same spring animation. Icon scale and label opacity are draw-only
 * transitions and do not change tab measurement or touch bounds.
 *
 * Positioning, system insets, and drag gestures remain separate concerns.
 */
@Composable
internal fun ZhixingLiquidBottomBar(
    backdrop: Backdrop,
    destinations: List<AppDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(destinations.isNotEmpty()) { "At least one navigation destination is required." }
    require(selectedIndex in destinations.indices) { "Selected destination index is out of range." }

    ZhixingLiquidBottomBarSurface(
        backdrop = backdrop,
        modifier = modifier,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 6.dp),
        ) {
            val itemWidth = maxWidth / destinations.size
            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "zhixingBottomBarSelectionOffset",
            )
            val indicatorShape = RoundedCornerShape(24.dp)

            // One shared indicator travels behind the four equal-width tabs.
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .clip(indicatorShape)
                    .background(
                        ZhixingTheme.colors.primaryContainer.copy(alpha = 0.82f),
                    ),
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEachIndexed { index, destination ->
                    key(destination.route) {
                        LiquidBottomBarItem(
                            destination = destination,
                            selected = index == selectedIndex,
                            onClick = { onDestinationSelected(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidBottomBarItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.96f,
        animationSpec = tween(
            durationMillis = 170,
            easing = FastOutSlowInEasing,
        ),
        label = "zhixingBottomBarIconScale",
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.88f,
        animationSpec = tween(
            durationMillis = 170,
            easing = FastOutSlowInEasing,
        ),
        label = "zhixingBottomBarIconAlpha",
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.78f,
        animationSpec = tween(
            durationMillis = 170,
            easing = FastOutSlowInEasing,
        ),
        label = "zhixingBottomBarLabelAlpha",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = destination.symbol,
            modifier = Modifier.graphicsLayer {
                scaleX = iconScale
                scaleY = iconScale
                alpha = iconAlpha
            },
            style = ZhixingTheme.type.navigationSymbol,
            color = if (selected) {
                ZhixingTheme.colors.primary
            } else {
                ZhixingTheme.colors.onSurfaceVariant
            },
        )
        Text(
            text = destination.label,
            modifier = Modifier.graphicsLayer {
                alpha = labelAlpha
            },
            style = ZhixingTheme.type.supporting,
            color = if (selected) {
                ZhixingTheme.colors.primary
            } else {
                ZhixingTheme.colors.onSurfaceVariant
            },
            maxLines = 1,
        )
    }
}
