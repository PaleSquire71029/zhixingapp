/*
 * Copyright 2025 Kyant
 *
 * Ported from Kyant0/AndroidLiquidGlass, Catalog tag 1.0.6
 * (commit 896a94a3ade1cc1a940b92365f942a34971fecda).
 * Original package: com.kyant.backdrop.catalog.utils
 *
 * Zhixing adaptation:
 * - keep the upstream Animatable / spring / velocity pipeline intact;
 * - optionally use Compose's horizontal drag detector for sliders inside
 *   verticalScroll containers, allowing vertical gestures to reach the parent;
 * - allow the touch-feedback preference to gate press/release effects.
 * Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6/catalog
 * SPDX-License-Identifier: Apache-2.0
 */

package ci.us.zhixing.app.ui.glass.backdrop106

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    private val touchFeedbackEnabled: Boolean = true,
    private val horizontalDragOnly: Boolean = false,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {

    private val valueAnimationSpec =
        spring(1f, 1000f, visibilityThreshold)
    private val velocityAnimationSpec =
        spring(0.5f, 300f, visibilityThreshold * 10f)
    private val pressProgressAnimationSpec =
        spring(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec =
        spring(0.6f, 250f, 0.001f)
    private val scaleYAnimationSpec =
        spring(0.7f, 250f, 0.001f)

    private val valueAnimation =
        Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation =
        Animatable(0f, 5f)
    private val pressProgressAnimation =
        Animatable(0f, 0.001f)
    private val scaleXAnimation =
        Animatable(initialScale, 0.001f)
    private val scaleYAnimation =
        Animatable(initialScale, 0.001f)

    private val mutatorMutex = MutatorMutex()
    private val velocityTracker = VelocityTracker()

    // During slider drag, read this state from graphicsLayer properties and
    // follow the pointer directly. Do not start a new Animatable spring for
    // every MotionEvent; the spring/velocity animations remain responsible
    // for release, press, squash and rebound.
    private var directDragging by mutableStateOf(false)
    private var directValue by mutableFloatStateOf(initialValue)
    private var directVelocity by mutableFloatStateOf(0f)

    val isDirectDragging: Boolean get() = directDragging
    val directDragValue: Float get() = directValue
    val value: Float get() = if (directDragging) directValue else valueAnimation.value
    val progress: Float get() =
        (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val targetValue: Float get() = if (directDragging) directValue else valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = if (directDragging) directVelocity else velocityAnimation.value

    fun beginDirectDrag() {
        directValue = valueAnimation.value
        directVelocity = 0f
        directDragging = true
        velocityTracker.resetTracking()
        velocityTracker.addPosition(
            SystemClock.uptimeMillis(),
            Offset(directValue, 0f),
        )
    }

    fun updateDirectDragValue(value: Float) {
        val next = value.coerceIn(valueRange)
        directValue = next
        velocityTracker.addPosition(
            SystemClock.uptimeMillis(),
            Offset(next, 0f),
        )
        val range = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
        directVelocity = velocityTracker.calculateVelocity().x / range
    }

    fun finishDirectDrag(finalValue: Float = directValue) {
        val position = directValue
        val dragVelocity = directVelocity
        val target = finalValue.coerceIn(valueRange)
        animationScope.launch {
            // Cancel any stale spring and synchronize once on release.
            valueAnimation.snapTo(position)
            velocityAnimation.snapTo(dragVelocity)
            directDragging = false
            launch {
                valueAnimation.animateTo(
                    targetValue = target,
                    animationSpec = valueAnimationSpec,
                    initialVelocity = dragVelocity,
                )
            }
            launch {
                velocityAnimation.animateTo(0f, velocityAnimationSpec)
            }
        }
    }

    fun cancelDirectDrag() {
        if (!directDragging) return
        finishDirectDrag(directValue)
    }

    val modifier: Modifier = Modifier.pointerInput(Unit) {
        if (horizontalDragOnly) {
            // The Foundation detector waits for horizontal touch slop and
            // cancels on vertical intent. Parent verticalScroll therefore
            // receives vertical gestures without a competing custom recognizer.
            detectHorizontalDragGestures(
                onDragStart = { position ->
                    velocityTracker.resetTracking()
                    onDragStarted(position)
                    if (touchFeedbackEnabled) press()
                },
                onDragEnd = {
                    onDragStopped()
                    if (touchFeedbackEnabled) release()
                },
                onDragCancel = {
                    onDragStopped()
                    if (touchFeedbackEnabled) release()
                },
            ) { change, dragAmount ->
                onDrag(size, Offset(dragAmount, 0f))
            }
        } else {
            inspectDragGestures(
                onDragStart = { down ->
                    velocityTracker.resetTracking()
                    onDragStarted(down.position)
                    if (touchFeedbackEnabled) press()
                },
                onDragEnd = {
                    onDragStopped()
                    if (touchFeedbackEnabled) release()
                },
                onDragCancel = {
                    onDragStopped()
                    if (touchFeedbackEnabled) release()
                },
            ) { change, dragAmount ->
                onDrag(size, dragAmount)
            }
        }
    }

    fun press() {
        velocityTracker.resetTracking()
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        animationScope.launch {
            awaitFrame()
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
        }
    }

    fun updateValue(value: Float) {
        if (directDragging) return
        val targetValue = value.coerceIn(valueRange)
        animationScope.launch {
            launch {
                valueAnimation.animateTo(targetValue, valueAnimationSpec) {
                    updateVelocity()
                }
            }
        }
    }

    fun animateToValue(value: Float) {
        animationScope.launch {
            mutatorMutex.mutate {
                if (touchFeedbackEnabled) press()
                val targetValue = value.coerceIn(valueRange)
                launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) }
                if (velocity != 0f) {
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                }
                if (touchFeedbackEnabled) release()
            }
        }
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            System.currentTimeMillis(),
            Offset(value, 0f),
        )
        val targetVelocity =
            velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
        animationScope.launch {
            velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec)
        }
    }
}
