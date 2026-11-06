/*
 * Copyright 2025 Kyant
 *
 * Ported from Kyant0/AndroidLiquidGlass, Catalog tag 1.0.6
 * (commit 896a94a3ade1cc1a940b92365f942a34971fecda).
 * Original package: com.kyant.backdrop.catalog.components
 *
 * Zhixing adaptation:
 * - preserves the Catalog translucent tint track and combined-Backdrop glass thumb;
 * - unifies tap, horizontal drag, long-press and vertical-scroll arbitration
 *   on one pointer stream so a held switch cannot leak movement to verticalScroll;
 * - uses direct frame state during dragging and commits one spring settle on release.
 * Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6/catalog
 * SPDX-License-Identifier: Apache-2.0
 */

package ci.us.zhixing.app.ui.glass.backdrop106.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
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
import kotlin.math.abs
import kotlin.math.sign

@Composable
internal fun LiquidToggle(
    selected: () -> Boolean,
    onSelect: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    label: String,
    enabled: Boolean = true,
    touchFeedbackEnabled: Boolean = true,
) {
    val accentColor = Color(0xFF34C759)
    val trackColor = Color(0xFF787878).copy(alpha = 0.20f)

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val dragWidth = with(density) { 20f.dp.toPx() }
    val animationScope = rememberCoroutineScope()
    var fraction by remember { mutableFloatStateOf(if (selected()) 1f else 0f) }
    var skipNextSelectedAnimation by remember { mutableStateOf(false) }

    val dampedDragAnimation = remember(
        animationScope,
        enabled,
        touchFeedbackEnabled,
        dragWidth,
        isLtr,
    ) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            touchFeedbackEnabled = touchFeedbackEnabled,
            horizontalDragOnly = false,
            onDragStarted = {},
            onDragStopped = {},
            onDrag = { _, _ -> },
        )
    }

    // External state changes animate the toggle. Drag updates are handled by
    // the direct frame state below, not by repeatedly launching updateValue().
    LaunchedEffect(dampedDragAnimation, selected) {
        snapshotFlow { selected() }
            .collectLatest { isSelected ->
                val target = if (isSelected) 1f else 0f
                if (skipNextSelectedAnimation) {
                    skipNextSelectedAnimation = false
                    fraction = target
                } else if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }

    fun selectAndAnimate(next: Boolean) {
        val target = if (next) 1f else 0f
        fraction = target
        skipNextSelectedAnimation = true
        dampedDragAnimation.animateToValue(target)
        onSelect(next)
    }

    val trackBackdrop = rememberLayerBackdrop()

    // One recognizer owns the switch. It waits for horizontal / vertical
    // touch slop, recognizes a long press by event timestamps, and consumes
    // movement in Initial pass after long-press so the ancestor verticalScroll
    // never receives those later moves. Vertical intent before long-press is
    // deliberately left unconsumed for normal page scrolling.
    val switchGesture = if (enabled) {
        Modifier.pointerInput(
            enabled,
            touchFeedbackEnabled,
            dampedDragAnimation,
            dragWidth,
            isLtr,
            selected,
        ) {
            awaitEachGesture {
                val down = awaitFirstDown(
                    requireUnconsumed = false,
                    pass = PointerEventPass.Initial,
                )
                val pointerId = down.id
                val downTime = down.uptimeMillis
                val touchSlop = viewConfiguration.touchSlop
                val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                var accumulated = Offset.Zero
                var mode = SwitchGestureMode.Pending
                var ended = false

                if (touchFeedbackEnabled) dampedDragAnimation.press()

                while (!ended) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == pointerId }
                    if (change == null) {
                        if (mode == SwitchGestureMode.Drag) {
                            dampedDragAnimation.finishDirectDrag(
                                if (selected()) 1f else 0f,
                            )
                        }
                        ended = true
                        continue
                    }

                    // Long-press wins over movement that begins after the
                    // timeout. The first such movement is consumed right here
                    // in Initial pass, before verticalScroll handles Main pass.
                    if (
                        mode == SwitchGestureMode.Pending &&
                        event.uptimeMillis - downTime >= longPressTimeout
                    ) {
                        mode = SwitchGestureMode.LongPressed
                    }

                    val delta = change.positionChange()
                    if (!change.pressed) {
                        when (mode) {
                            SwitchGestureMode.Pending -> {
                                change.consume()
                                selectAndAnimate(!selected())
                            }
                            SwitchGestureMode.Drag -> {
                                val next = dampedDragAnimation.directDragValue
                                    .coerceIn(0f, 1f) >= 0.5f
                                val target = if (next) 1f else 0f
                                fraction = target
                                dampedDragAnimation.finishDirectDrag(target)
                                if (next != selected()) {
                                    skipNextSelectedAnimation = true
                                    onSelect(next)
                                }
                            }
                            SwitchGestureMode.LongPressed,
                            SwitchGestureMode.Scroll -> Unit
                        }
                        ended = true
                    } else {
                        when (mode) {
                            SwitchGestureMode.Pending -> {
                                accumulated += delta
                                val absX = abs(accumulated.x)
                                val absY = abs(accumulated.y)
                                when {
                                    absX > touchSlop && absX > absY -> {
                                        mode = SwitchGestureMode.Drag
                                        dampedDragAnimation.beginDirectDrag()
                                        val overSlop = accumulated.x -
                                            sign(accumulated.x) * touchSlop
                                        if (overSlop != 0f) {
                                            val amount = overSlop / dragWidth
                                            val next = if (isLtr) {
                                                dampedDragAnimation.directDragValue + amount
                                            } else {
                                                dampedDragAnimation.directDragValue - amount
                                            }
                                            dampedDragAnimation.updateDirectDragValue(next)
                                        }
                                        change.consume()
                                    }
                                    absY > touchSlop && absY >= absX -> {
                                        mode = SwitchGestureMode.Scroll
                                        ended = true
                                    }
                                }
                            }
                            SwitchGestureMode.Drag -> {
                                if (delta.x != 0f) {
                                    val amount = delta.x / dragWidth
                                    val next = if (isLtr) {
                                        dampedDragAnimation.directDragValue + amount
                                    } else {
                                        dampedDragAnimation.directDragValue - amount
                                    }
                                    dampedDragAnimation.updateDirectDragValue(next)
                                }
                                change.consume()
                            }
                            SwitchGestureMode.LongPressed -> change.consume()
                            SwitchGestureMode.Scroll -> ended = true
                        }
                    }
                }

                if (mode == SwitchGestureMode.Scroll) {
                    // The movement that crossed vertical touch slop remains
                    // unconsumed, so the parent scroll container takes over.
                    if (touchFeedbackEnabled) dampedDragAnimation.release()
                } else if (touchFeedbackEnabled) {
                    dampedDragAnimation.release()
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier.then(switchGesture),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Catalog's transparent track: only a tinted translucent fill, without
        // an extra blur/lens pass that would turn it into matte glass.
        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .clip(Capsule())
                .drawBehind {
                    val current = dampedDragAnimation.value
                    drawRect(lerpColor(trackColor, accentColor, current))
                }
                .size(64.dp, 28.dp),
        )

        Box(
            Modifier
                .graphicsLayer {
                    val current = dampedDragAnimation.value
                    val padding = 2f.dp.toPx()
                    translationX = if (isLtr) {
                        lerp(padding, padding + dragWidth, current)
                    } else {
                        lerp(-padding, -(padding + dragWidth), current)
                    }
                }
                .size(40.dp, 24.dp)
                .semantics {
                    role = Role.Switch
                    contentDescription = label
                    toggleableState =
                        if (selected()) ToggleableState.On else ToggleableState.Off
                    if (enabled) {
                        onClick(label) {
                            selectAndAnimate(!selected())
                            true
                        }
                    } else {
                        disabled()
                    }
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 0.75f, progress)
                            val scaleY = lerp(0f, 0.75f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        },
                    ),
                    shape = { Capsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        blur(8f.dp.toPx() * (1f - progress))
                        lens(
                            5f.dp.toPx() * progress,
                            10f.dp.toPx() * progress,
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
                            radius = 4f.dp,
                            color = Color.Black.copy(alpha = 0.05f),
                        )
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(
                            radius = 4f.dp * progress,
                            alpha = progress,
                        )
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 50f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(Color.White.copy(alpha = 1f - progress))
                    },
                ),
        )
    }
}

private enum class SwitchGestureMode {
    Pending,
    Drag,
    LongPressed,
    Scroll,
}
