/*
 * Copyright 2025 Kyant
 *
 * Ported from Kyant0/AndroidLiquidGlass, Catalog tag 1.0.6
 * (commit 896a94a3ade1cc1a940b92365f942a34971fecda).
 * Original package: com.kyant.backdrop.catalog.components
 *
 * Zhixing adaptation:
 * - keep upstream track-tap + thumb-drag interaction structure;
 * - use Foundation horizontal drag detection so verticalScroll owns vertical intent;
 * - keep the existing accessibility range, step snapping and enlarged thumb target;
 * - keep the progress fill on a graphics layer to avoid layout measurement per frame.
 * Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6/catalog
 * SPDX-License-Identifier: Apache-2.0
 */

package ci.us.zhixing.app.ui.glass.backdrop106.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import ci.us.zhixing.app.ui.glass.backdrop106.DampedDragAnimation
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

@Composable
internal fun LiquidSlider(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    visibilityThreshold: Float,
    backdrop: Backdrop,
    label: String,
    accentColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    touchFeedbackEnabled: Boolean = true,
) {
    val valueState = rememberUpdatedState(value)
    val valueReader: () -> Float = remember { { valueState.value() } }
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState = rememberUpdatedState(onValueChangeFinished)
    val stepCount = steps.coerceAtLeast(0)
    val snapValue: (Float) -> Float = remember(valueRange, stepCount) {
        { candidate ->
            val clamped = candidate.coerceIn(valueRange)
            if (stepCount == 0) {
                clamped
            } else {
                val stepSize = (valueRange.endInclusive - valueRange.start) / (stepCount + 1)
                val index = ((clamped - valueRange.start) / stepSize).roundToInt()
                (valueRange.start + index * stepSize).coerceIn(valueRange)
            }
        }
    }
    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics {
                contentDescription = label
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = valueReader().coerceIn(valueRange),
                    range = valueRange,
                    steps = stepCount,
                )
                if (enabled) {
                    setProgress { requested ->
                        val next = snapValue(requested)
                        onValueChangeState.value(next)
                        onValueChangeFinishedState.value()
                        true
                    }
                } else {
                    disabled()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth.toFloat()
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var didDrag by remember { mutableStateOf(false) }
        val dampedDragAnimation = remember(
            animationScope,
            valueRange,
            visibilityThreshold,
            trackWidth,
            isLtr,
            enabled,
            touchFeedbackEnabled,
        ) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = valueReader().coerceIn(valueRange),
                valueRange = valueRange,
                visibilityThreshold = visibilityThreshold,
                initialScale = 1f,
                pressedScale = 1.5f,
                touchFeedbackEnabled = touchFeedbackEnabled,
                horizontalDragOnly = true,
                onDragStarted = {
                    didDrag = false
                    beginDirectDrag()
                },
                onDragStopped = {
                    if (enabled && didDrag) {
                        val finalValue = directDragValue.coerceIn(valueRange)
                        finishDirectDrag(finalValue)
                        onValueChangeState.value(finalValue)
                        onValueChangeFinishedState.value()
                    } else {
                        cancelDirectDrag()
                    }
                    didDrag = false
                },
                onDrag = { _, dragAmount ->
                    if (enabled && trackWidth > 0f) {
                        if (!didDrag) didDrag = dragAmount.x != 0f
                        val delta =
                            (valueRange.endInclusive - valueRange.start) *
                                (dragAmount.x / trackWidth)
                        val candidate = if (isLtr) directDragValue + delta else directDragValue - delta
                        val next = candidate.coerceIn(valueRange)
                        updateDirectDragValue(next)
                        onValueChangeState.value(next)
                    }
                },
            )
        }

        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { valueReader() to dampedDragAnimation.isDirectDragging }
                .collectLatest { (nextValue, isDragging) ->
                    if (!isDragging) {
                        val next = nextValue.coerceIn(valueRange)
                        if (dampedDragAnimation.targetValue != next) {
                            dampedDragAnimation.updateValue(next)
                        }
                    }
                }
        }

        // The full rail is a generous tap target, like the Catalog's thin
        // visual track. It does not own drag gestures: horizontal dragging is
        // reserved for the glass thumb below; vertical gestures bubble to the
        // settings page's verticalScroll.
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .fillMaxWidth()
                .height(48.dp)
                .then(
                    if (enabled) {
                        Modifier.pointerInput(animationScope, trackWidth, isLtr, valueRange) {
                            detectTapGestures { position ->
                                val fraction = (position.x / trackWidth).fastCoerceIn(0f, 1f)
                                val delta =
                                    (valueRange.endInclusive - valueRange.start) * fraction
                                val next = snapValue(
                                    if (isLtr) valueRange.start + delta
                                    else valueRange.endInclusive - delta,
                                )
                                dampedDragAnimation.animateToValue(next)
                                onValueChangeState.value(next)
                                onValueChangeFinishedState.value()
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .clip(Capsule())
                    .background(
                        if (enabled) trackColor
                        else trackColor.copy(alpha = trackColor.alpha * 0.65f),
                    )
                    .fillMaxWidth()
                    .height(6.dp),
            )
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .clip(Capsule())
                    .background(if (enabled) accentColor else accentColor.copy(alpha = 0.35f))
                    .fillMaxWidth()
                    .height(6.dp)
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(
                            pivotFractionX = if (isLtr) 0f else 1f,
                            pivotFractionY = 0.5f,
                        )
                        scaleX = dampedDragAnimation.progress.fastCoerceIn(0f, 1f)
                    },
            )
        }

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .graphicsLayer {
                    translationX =
                        (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                            .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) *
                            if (isLtr) 1f else -1f
                }
                .size(48.dp)
                .then(if (enabled) dampedDragAnimation.modifier else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .drawBackdrop(
                        backdrop = rememberCombinedBackdrop(
                            backdrop,
                            rememberBackdrop(trackBackdrop) { drawBackdrop ->
                                val progress = dampedDragAnimation.pressProgress
                                val scaleX = lerp(2f / 3f, 1f, progress)
                                val scaleY = lerp(0f, 1f, progress)
                                scale(scaleX, scaleY) {
                                    drawBackdrop()
                                }
                            },
                        ),
                        shape = { Capsule() },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            blur(8.dp.toPx() * (1f - progress))
                            lens(
                                10.dp.toPx() * progress,
                                14.dp.toPx() * progress,
                                chromaticAberration = true,
                            )
                        },
                        highlight = {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Ambient.copy(
                                width = Highlight.Ambient.width / 1.5f,
                                blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                                alpha = progress,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 4.dp,
                                color = Color.Black.copy(alpha = 0.05f),
                            )
                        },
                        innerShadow = {
                            val progress = dampedDragAnimation.pressProgress
                            InnerShadow(
                                radius = 4.dp * progress,
                                alpha = progress,
                            )
                        },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = dampedDragAnimation.velocity / 10f
                            scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        },
                        onDrawSurface = {
                            val progress = dampedDragAnimation.pressProgress
                            drawRect(Color.White.copy(alpha = 1f - progress))
                        },
                    )
                    .size(40.dp, 24.dp),
            )
        }
    }
}
