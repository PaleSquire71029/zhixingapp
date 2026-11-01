package ci.us.zhixing.app.ui.glass

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToInt

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

    var itemTouchActive by remember { mutableStateOf(false) }
    var dragActive by remember { mutableStateOf(false) }
    val touchActive = itemTouchActive || dragActive
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
            val indicatorOffset = remember { Animatable(itemWidthPx * selectedIndex) }
            val latestSelectedIndex = rememberUpdatedState(selectedIndex)
            val latestOnDestinationSelected = rememberUpdatedState(onDestinationSelected)
            LaunchedEffect(itemWidthPx, selectedIndex, dragActive) {
                if (!dragActive) {
                    indicatorOffset.animateTo(
                        targetValue = itemWidthPx * selectedIndex,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                }
            }
            val selectionStretch = remember { Animatable(0f) }
            var previousSelectedIndex by remember { mutableStateOf(selectedIndex) }
            LaunchedEffect(selectedIndex) {
                val selectionChanged = previousSelectedIndex != selectedIndex
                previousSelectedIndex = selectedIndex
                if (selectionChanged) {
                    selectionStretch.snapTo(0f)
                    selectionStretch.animateTo(
                        targetValue = 0f,
                        animationSpec = keyframes {
                            durationMillis = 280
                            0.16f at 55
                            0.045f at 170
                            0f at 280
                        },
                    )
                }
            }
            val indicatorShape = RoundedCornerShape(24.dp)
            val indicatorCapabilities = resolveGlassSurfaceCapabilities(
                apiLevel = Build.VERSION.SDK_INT,
                enabled = material.enabled,
                blurRadiusDp = material.blurRadiusDp,
                refractionEnabled = material.refractionEnabled,
                refractionStrengthDp = material.refractionStrengthDp,
            )
            val selectionPressProgress = if (
                material.enabled && material.refractionEnabled &&
                material.touchFeedbackEnabled
            ) {
                refractionPressProgress.coerceIn(0f, 1f)
            } else {
                0f
            }
            val selectionLensParameters = resolveTouchRefractionParameters(
                baseHeightDp = 10f,
                baseAmountDp = material.refractionStrengthDp * 1.5f,
                pressProgress = selectionPressProgress,
            )
            val useSelectionBackdrop = indicatorCapabilities.useBackdropSampling &&
                (indicatorCapabilities.useBlur || indicatorCapabilities.useRefraction)

            // A separate live lens sits behind the foreground tab labels.
            // It keeps the same animated position and never participates in hit testing.
            val selectionLensModifier = if (useSelectionBackdrop) {
                Modifier.drawBackdrop(
                    backdrop = backdrop,
                    shape = { indicatorShape },
                    effects = {
                        if (indicatorCapabilities.useVibrancy) {
                            vibrancy()
                        }
                        if (indicatorCapabilities.useBlur) {
                            blur(material.blurRadiusDp.dp.toPx())
                        }
                        if (indicatorCapabilities.useRefraction) {
                            lens(
                                refractionHeight = selectionLensParameters.heightDp.dp.toPx(),
                                refractionAmount = selectionLensParameters.amountDp.dp.toPx(),
                                chromaticAberration = material.chromaticDispersionEnabled,
                            )
                        }
                    },
                    highlight = {
                        if (material.edgeHighlightEnabled) {
                            Highlight.Default.copy(
                                alpha = 0.52f + 0.16f * selectionPressProgress,
                            )
                        } else {
                            null
                        }
                    },
                    shadow = {
                        Shadow(
                            radius = 4.dp,
                            color = Color.Black.copy(alpha = 0.055f),
                            alpha = 0.42f + 0.28f * selectionPressProgress,
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 2.dp + 3.dp * selectionPressProgress,
                            alpha = 0.08f + 0.12f * selectionPressProgress,
                        )
                    },
                    onDrawSurface = {
                        drawRect(
                            ZhixingTheme.colors.primaryContainer.copy(alpha = 0.20f),
                        )
                    },
                )
            } else {
                Modifier
                    .clip(indicatorShape)
                    .background(
                        ZhixingTheme.colors.primaryContainer.copy(alpha = 0.82f),
                    )
            }

            // The parent observes horizontal movement, but only takes over after
            // touch slop and only when the gesture began on the selected capsule.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(itemWidthPx, destinations.size) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val capsuleStart = indicatorOffset.value
                            if (
                                down.position.x < capsuleStart ||
                                down.position.x > capsuleStart + itemWidthPx
                            ) {
                                return@awaitEachGesture
                            }

                            val dragStart = awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ ->
                                change.consume()
                            } ?: return@awaitEachGesture

                            val startPointerX = down.position.x
                            val startIndicatorOffset = indicatorOffset.value
                            val maxIndicatorOffset = itemWidthPx * (destinations.size - 1)
                            dragActive = true
                            var pointerChange = dragStart
                            var released = false
                            try {
                                while (true) {
                                    val nextOffset = (
                                        startIndicatorOffset +
                                            pointerChange.position.x - startPointerX
                                        ).coerceIn(0f, maxIndicatorOffset)
                                    indicatorOffset.snapTo(nextOffset)
                                    touchPoint = pointerChange.position
                                    if (!pointerChange.pressed) {
                                        released = true
                                        break
                                    }

                                    val event = awaitPointerEvent()
                                    val nextChange = event.changes.firstOrNull { it.id == down.id }
                                        ?: break
                                    if (nextChange.pressed) {
                                        nextChange.consume()
                                    }
                                    pointerChange = nextChange
                                }
                            } finally {
                                val finalIndex = resolveBottomBarDragIndex(
                                    indicatorOffsetPx = indicatorOffset.value,
                                    itemWidthPx = itemWidthPx,
                                    itemCount = destinations.size,
                                )
                                dragActive = false
                                touchPoint = null
                                if (
                                    released &&
                                    finalIndex != latestSelectedIndex.value
                                ) {
                                    latestOnDestinationSelected.value(finalIndex)
                                }
                            }
                        }
                    },
            ) {
            // One shared refractive indicator travels behind the four equal-width tabs.
            Box(
                modifier = Modifier
                    .offset { IntOffset(indicatorOffset.value.roundToInt(), 0) }
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .graphicsLayer {
                        // Stretch briefly as the capsule travels, then settle without
                        // changing the measured size or the tabs' hit targets.
                        scaleX = 1f +
                            selectionStretch.value * 0.14f +
                            (1f - touchDeformationScale) * 0.25f
                        scaleY = touchDeformationScale *
                            (1f - selectionStretch.value * 0.035f)
                    }
                    .then(selectionLensModifier),
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
                                itemTouchActive = active
                            },
                            onClick = { onDestinationSelected(index) },
                        )
                    }
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
