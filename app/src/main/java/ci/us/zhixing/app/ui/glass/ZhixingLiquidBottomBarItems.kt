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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/**
 * Four-destination content for the floating glass navigation shell.
 *
 * Selection and click handling are controlled by the caller. The selected
 * background moves as one shared capsule; rapid destination changes retarget
 * the same spring animation. Icon scale and label opacity are draw-only
 * transitions and do not change tab measurement or touch bounds.
 *
 * A horizontal drag can begin on the selected capsule; system insets and
 * placement remain owned by the surrounding navigation host.
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
            // Mirror the tab faces into a dedicated layer so the selected lens
            // can refract both page content and the labels/icons beneath it.
            val tabContentBackdrop = rememberLayerBackdrop()
            val selectionBackdrop = rememberCombinedBackdrop(backdrop, tabContentBackdrop)
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
            var dragDirection by remember { mutableStateOf(1) }
            var dragStretchTarget by remember { mutableStateOf(0f) }
            val dragStretch by animateFloatAsState(
                targetValue = if (dragActive && material.touchFeedbackEnabled) {
                    dragStretchTarget
                } else {
                    0f
                },
                animationSpec = tween(
                    durationMillis = 72,
                    easing = FastOutSlowInEasing,
                ),
                label = "zhixingBottomBarDirectionalDragStretch",
            )
            val dragTrailOpacity by animateFloatAsState(
                targetValue = if (
                    dragActive && material.touchFeedbackEnabled && dragStretchTarget > 0.01f
                ) {
                    0.20f + (dragStretchTarget / 0.12f).coerceIn(0f, 1f) * 0.28f
                } else {
                    0f
                },
                animationSpec = tween(durationMillis = 120),
                label = "zhixingBottomBarDragTrailOpacity",
            )
            val dragTrailDirectionProgress by animateFloatAsState(
                targetValue = if (dragDirection > 0) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 100,
                    easing = FastOutSlowInEasing,
                ),
                label = "zhixingBottomBarDragTrailDirection",
            )
            val dragTrailWidth = with(density) {
                (itemWidthPx * (0.22f + dragStretch * 0.50f)).toDp()
            }
            val dragTrailTint = ZhixingTheme.colors.primary
            val indicatorOffset = remember { Animatable(itemWidthPx * selectedIndex) }
            // The raw pointer target stays exact for hit / selection math; this separate
            // spring-driven offset gives the visible glass capsule a restrained follow lag.
            val dragFollowOffset = remember { Animatable(itemWidthPx * selectedIndex) }
            var dragOffsetPx by remember { mutableStateOf(itemWidthPx * selectedIndex) }
            var settleFromDragPx by remember { mutableStateOf<Float?>(null) }
            val latestSelectedIndex = rememberUpdatedState(selectedIndex)
            val latestOnDestinationSelected = rememberUpdatedState(onDestinationSelected)
            LaunchedEffect(itemWidthPx, selectedIndex, dragActive) {
                if (!dragActive) {
                    settleFromDragPx?.let { dragStart ->
                        indicatorOffset.snapTo(dragStart)
                        settleFromDragPx = null
                    }
                    indicatorOffset.animateTo(
                        targetValue = itemWidthPx * selectedIndex,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    )
                    dragFollowOffset.snapTo(indicatorOffset.value)
                }
            }
            LaunchedEffect(dragActive) {
                if (dragActive) {
                    // Start from the settled capsule position, then follow raw pointer
                    // updates with a low-bounce spring. The drag target itself is not delayed.
                    dragFollowOffset.snapTo(indicatorOffset.value)
                    snapshotFlow { dragOffsetPx }.collectLatest { targetOffset ->
                        dragFollowOffset.animateTo(
                            targetValue = targetOffset,
                            animationSpec = spring(
                                dampingRatio = 0.86f,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                        )
                    }
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
            val selectionOverlayAlpha = resolveGlassOverlayAlpha(material.transparencyPercent)

            // A separate live lens sits behind the foreground tab labels.
            // It keeps the same animated position and never participates in hit testing.
            val selectionLensModifier = if (useSelectionBackdrop) {
                Modifier.drawBackdrop(
                    backdrop = selectionBackdrop,
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
                                alpha = resolveGlassHighlightAlpha(
                                    transparencyPercent = material.transparencyPercent,
                                    pressProgress = selectionPressProgress,
                                ),
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
                            ZhixingTheme.colors.primaryContainer.copy(alpha = selectionOverlayAlpha),
                        )
                        if (material.environmentReflectionEnabled) {
                            drawEnvironmentReflection()
                        }
                    },
                )
            } else {
                Modifier
                    .clip(indicatorShape)
                    .background(
                        ZhixingTheme.colors.primaryContainer.copy(alpha = selectionOverlayAlpha),
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
                            val velocityTracker = VelocityTracker()
                            velocityTracker.addPosition(down.uptimeMillis, down.position)
                            velocityTracker.addPosition(dragStart.uptimeMillis, dragStart.position)
                            dragOffsetPx = startIndicatorOffset
                            dragActive = true
                            var pointerChange = dragStart
                            var lastPointerX = down.position.x
                            var released = false
                            try {
                                while (true) {
                                    val nextOffset = (
                                        startIndicatorOffset +
                                            pointerChange.position.x - startPointerX
                                        ).coerceIn(0f, maxIndicatorOffset)
                                    dragOffsetPx = nextOffset
                                    velocityTracker.addPosition(
                                        pointerChange.uptimeMillis,
                                        pointerChange.position,
                                    )
                                    val deltaX = pointerChange.position.x - lastPointerX
                                    val directionalDeformation = resolveDirectionalDragDeformation(
                                        deltaXPx = deltaX,
                                        itemWidthPx = itemWidthPx,
                                    )
                                    if (directionalDeformation.direction != 0) {
                                        dragDirection = directionalDeformation.direction
                                    }
                                    val velocityStretch = resolveVelocityDragStretch(
                                        velocityXPxPerSecond = velocityTracker.calculateVelocity().x,
                                        itemWidthPx = itemWidthPx,
                                    )
                                    dragStretchTarget = maxOf(
                                        directionalDeformation.stretch,
                                        velocityStretch,
                                    )
                                    lastPointerX = pointerChange.position.x
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
                                    indicatorOffsetPx = dragOffsetPx,
                                    itemWidthPx = itemWidthPx,
                                    itemCount = destinations.size,
                                )
                                settleFromDragPx = dragFollowOffset.value
                                dragStretchTarget = 0f
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
            // The invisible duplicate contributes only tab artwork to the combined
            // backdrop. It has no pointer handlers, click target, or accessibility nodes.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabContentBackdrop),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                destinations.forEachIndexed { index, destination ->
                    key(destination.route) {
                        LiquidBottomBarSampleItem(
                            destination = destination,
                            selected = index == selectedIndex,
                        )
                    }
                }
            }

            // A short gradient wake trails the moving capsule and fades on release.
            if (dragTrailOpacity > 0.001f) {
                val trailColor = dragTrailTint
                val directionProgress = dragTrailDirectionProgress.coerceIn(0f, 1f)
                val trailColors = listOf(
                    trailColor.copy(alpha = 0.30f * (1f - directionProgress)),
                    trailColor.copy(alpha = 0.30f),
                    trailColor.copy(alpha = 0.30f * directionProgress),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            val capsuleOffset = if (dragActive) {
                                dragFollowOffset.value
                            } else {
                                indicatorOffset.value
                            }
                            val trailingLeft = capsuleOffset - dragTrailWidth.toPx() + 3.dp.toPx()
                            val trailingRight = capsuleOffset + itemWidthPx - 3.dp.toPx()
                            val trailLeft = trailingRight +
                                (trailingLeft - trailingRight) * directionProgress
                            IntOffset(trailLeft.roundToInt(), 0)
                        }
                        .width(dragTrailWidth)
                        .fillMaxHeight(0.30f)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            brush = Brush.horizontalGradient(trailColors),
                            shape = RoundedCornerShape(percent = 50),
                        )
                        .graphicsLayer { alpha = dragTrailOpacity },
                )
            }

            // One shared refractive indicator travels behind the four equal-width tabs.
            Box(
                modifier = Modifier
                    .offset {
                        val currentOffset = if (dragActive) dragFollowOffset.value else indicatorOffset.value
                        IntOffset(currentOffset.roundToInt(), 0)
                    }
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .graphicsLayer {
                        // Anchor the capsule at the trailing edge so drag stretch
                        // extends toward the direction of travel.
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (dragDirection > 0) 0f else 1f,
                            pivotFractionY = 0.5f,
                        )
                        scaleX = 1f +
                            selectionStretch.value * 0.14f +
                            dragStretch +
                            (1f - touchDeformationScale) * 0.25f
                        scaleY = touchDeformationScale *
                            (1f - selectionStretch.value * 0.035f) *
                            (1f - dragStretch * 0.18f)
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
private fun RowScope.LiquidBottomBarSampleItem(
    destination: AppDestination,
    selected: Boolean,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(horizontal = 2.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (selected) 1f else 0.96f
                    scaleY = if (selected) 1f else 0.96f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = destination.symbol,
                modifier = Modifier.graphicsLayer {
                    alpha = if (selected) 1f else 0.88f
                    scaleX = if (selected) 1f else 0.96f
                    scaleY = if (selected) 1f else 0.96f
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
                    alpha = if (selected) 1f else 0.78f
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
