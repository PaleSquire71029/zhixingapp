package ci.us.zhixing.app.ui.glass

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop
import kotlinx.coroutines.flow.collect

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
    material: GlassMaterialState = GlassMaterialState(
        transparencyPercent = 68,
        blurRadiusDp = 12f,
    ),
) {
    require(destinations.isNotEmpty()) { "At least one navigation destination is required." }
    require(selectedIndex in destinations.indices) { "Selected destination index is out of range." }

    var touchActive by remember { mutableStateOf(false) }
    val refractionPressProgress by animateFloatAsState(
        targetValue = if (
            touchActive && material.touchFeedbackEnabled &&
            material.enabled && material.refractionEnabled
        ) 1f else 0f,
        animationSpec = tween(durationMillis = 130),
        label = "zhixingBottomBarLensPressure",
    )

    ZhixingLiquidBottomBarSurface(
        backdrop = backdrop,
        modifier = modifier,
        material = material,
        refractionPressProgress = refractionPressProgress,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 6.dp),
        ) {
            val itemWidth = maxWidth / destinations.size
            val density = LocalDensity.current
            val itemWidthPx = with(density) { itemWidth.toPx() }
            var touchPoint by remember { mutableStateOf<Offset?>(null) }
            val touchGlowAlpha by animateFloatAsState(
                targetValue = if (touchActive && material.touchFeedbackEnabled) 0.22f else 0f,
                animationSpec = tween(durationMillis = 160),
                label = "zhixingBottomBarTouchGlow",
            )
            val touchDeformationScale by animateFloatAsState(
                targetValue = if (touchActive && material.touchFeedbackEnabled) 0.975f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "zhixingBottomBarTouchDeformation",
            )
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
                    .graphicsLayer {
                        scaleX = 1f + (1f - touchDeformationScale) * 0.25f
                        scaleY = touchDeformationScale
                    }
                    .clip(indicatorShape)
                    .background(
                        ZhixingTheme.colors.primaryContainer.copy(alpha = 0.82f),
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .drawBehind {
                        val point = touchPoint
                        if (material.touchFeedbackEnabled && point != null && touchGlowAlpha > 0.001f) {
                            drawTouchGlow(point, touchGlowAlpha)
                        }
                    },
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
                            itemIndex = index,
                            itemCount = destinations.size,
                            itemWidthPx = itemWidthPx,
                            touchFeedbackEnabled = material.touchFeedbackEnabled,
                            onTouchChanged = { point, active ->
                                if (point != null) touchPoint = point
                                touchActive = active
                            },
                            onClick = { onDestinationSelected(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.LiquidBottomBarItem(
    destination: AppDestination,
    selected: Boolean,
    itemIndex: Int,
    itemCount: Int,
    itemWidthPx: Float,
    touchFeedbackEnabled: Boolean,
    onTouchChanged: (Offset?, Boolean) -> Unit,
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val latestOnTouchChanged by rememberUpdatedState(onTouchChanged)
    LaunchedEffect(interactionSource, itemIndex, itemWidthPx, touchFeedbackEnabled) {
        var activePress: PressInteraction.Press? = null
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    activePress = interaction
                    if (touchFeedbackEnabled) {
                        latestOnTouchChanged(
                            mapBottomBarTouchPosition(
                                itemIndex = itemIndex,
                                itemCount = itemCount,
                                itemWidthPx = itemWidthPx,
                                localPosition = interaction.pressPosition,
                            ),
                            true,
                        )
                    }
                }
                is PressInteraction.Release -> {
                    if (activePress == interaction.press) {
                        activePress = null
                        latestOnTouchChanged(null, false)
                    }
                }
                is PressInteraction.Cancel -> {
                    if (activePress == interaction.press) {
                        activePress = null
                        latestOnTouchChanged(null, false)
                    }
                }
            }
        }
    }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && touchFeedbackEnabled) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "zhixingBottomBarPressScale",
    )
    val pressHighlightAlpha by animateFloatAsState(
        targetValue = if (isPressed && touchFeedbackEnabled) 0.16f else 0f,
        animationSpec = tween(durationMillis = 110),
        label = "zhixingBottomBarPressHighlight",
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .drawBehind {
                if (pressHighlightAlpha > 0f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = pressHighlightAlpha),
                        cornerRadius = CornerRadius(
                            x = 20.dp.toPx(),
                            y = 20.dp.toPx(),
                        ),
                    )
                }
            }
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Only the visual content scales. The parent selectable bounds stay fixed.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = pressScale
                    scaleY = pressScale
                },
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
}
