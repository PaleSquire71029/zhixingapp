/*
 * Copyright 2025 Kyant
 *
 * Ported from Kyant0/AndroidLiquidGlass, Catalog tag 1.0.6
 * (commit 896a94a3ade1cc1a940b92365f942a34971fecda).
 * Original package: com.kyant.backdrop.catalog.components
 *
 * Zhixing adaptation:
 * - package and app palette are adapted to the light-only Zhixing UI;
 * - accessible switch semantics and an enabled flag are added;
 * - the original flat translucent track and combined-Backdrop glass thumb,
 *   blur/lens, highlight, shadow and spring deformation are retained.
 * Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6/catalog
 * SPDX-License-Identifier: Apache-2.0
 */

package ci.us.zhixing.app.ui.glass.backdrop106.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
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
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (selected()) 1f else 0f) }
    val dampedDragAnimation = remember(animationScope, enabled, touchFeedbackEnabled) {
        DampedDragAnimation(
            animationScope = animationScope,
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            touchFeedbackEnabled = touchFeedbackEnabled,
            horizontalDragOnly = true,
            onDragStarted = {},
            onDragStopped = {
                // Taps are handled by the root tap/long-press detector below.
                // This callback only commits an actual horizontal thumb drag.
                if (enabled && didDrag) {
                    fraction = if (fraction >= 0.5f) 1f else 0f
                    onSelect(fraction == 1f)
                }
                didDrag = false
            },
            onDrag = { _, dragAmount ->
                if (enabled) {
                    if (!didDrag) didDrag = dragAmount.x != 0f
                    val delta = dragAmount.x / dragWidth
                    fraction = if (isLtr) {
                        (fraction + delta).fastCoerceIn(0f, 1f)
                    } else {
                        (fraction - delta).fastCoerceIn(0f, 1f)
                    }
                }
            },
        )
    }

    LaunchedEffect(dampedDragAnimation) {
        snapshotFlow { fraction }
            .collectLatest { currentFraction ->
                dampedDragAnimation.updateValue(currentFraction)
            }
    }
    LaunchedEffect(selected) {
        snapshotFlow { selected() }
            .collectLatest { isSelected ->
                val target = if (isSelected) 1f else 0f
                if (target != fraction) {
                    fraction = target
                    dampedDragAnimation.animateToValue(target)
                }
            }
    }

    val trackBackdrop = rememberLayerBackdrop()

    // The thumb drag detector is horizontal-only. This tap detector owns
    // click and long-press; once the long-press timeout is reached it consumes
    // subsequent movement so the containing verticalScroll cannot steal a
    // held switch gesture. Before long-press, vertical intent still scrolls.
    val pressGesture = if (enabled) {
        Modifier.pointerInput(enabled, touchFeedbackEnabled, dampedDragAnimation) {
            detectTapGestures(
                onPress = {
                    if (touchFeedbackEnabled) dampedDragAnimation.press()
                    tryAwaitRelease()
                    if (touchFeedbackEnabled) dampedDragAnimation.release()
                },
                onTap = {
                    if (enabled) onSelect(!selected())
                },
                onLongPress = {
                    // Intentionally empty: recognizing the long press is
                    // enough to lock this pointer stream to the switch.
                },
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier.then(pressGesture),
        contentAlignment = Alignment.CenterStart,
    ) {
        // Match the original Catalog track: translucent tint, no extra blur
        // sampling layer. This keeps the switch transparent rather than matte.
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
                            onSelect(!selected())
                            true
                        }
                    } else {
                        disabled()
                    }
                }
                .then(if (enabled) dampedDragAnimation.modifier else Modifier)
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
