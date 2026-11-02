/*
 * Copyright 2025 Kyant
 *
 * Ported from Kyant0/AndroidLiquidGlass, Catalog tag 1.0.6
 * (commit 896a94a3ade1cc1a940b92365f942a34971fecda).
 * Original package: com.kyant.backdrop.catalog.utils
 *
 * Modified for Zhixing: package namespace relocated and optional touch-only
 * press animation can be disabled while retaining the original drag physics.
 * Source: https://github.com/Kyant0/AndroidLiquidGlass/tree/1.0.6/catalog
 * SPDX-License-Identifier: Apache-2.0
 */

package ci.us.zhixing.app.ui.glass.backdrop106

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import android.os.SystemClock
import kotlinx.coroutines.channels.Channel
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
    private val onTap: ((Offset) -> Unit)? = null,
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
    private data class DragFrame(val value: Float, val velocity: Float)
    private val directDragFrames = Channel<DragFrame>(Channel.CONFLATED)
    private var directDragTargetValue = initialValue

    init {
        if (horizontalDragOnly) {
            // Only sliders need this frame consumer. Other glass controls keep
            // the original Catalog animation path without extra idle coroutines.
            animationScope.launch {
                for (frame in directDragFrames) {
                    valueAnimation.snapTo(frame.value)
                    velocityAnimation.snapTo(frame.velocity)
                }
            }
        }
    }

    val directDragValue: Float get() = directDragTargetValue
    val value: Float get() = valueAnimation.value
    val progress: Float get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = velocityAnimation.value

    val modifier: Modifier = Modifier.pointerInput(Unit) {
        val start: (androidx.compose.ui.input.pointer.PointerInputChange) -> Unit = { down ->
            velocityTracker.resetTracking()
            onDragStarted(down.position)
            if (touchFeedbackEnabled) press()
        }
        val end: (androidx.compose.ui.input.pointer.PointerInputChange) -> Unit = {
            onDragStopped()
            if (touchFeedbackEnabled) release()
        }
        val cancel: () -> Unit = {
            onDragStopped()
            if (touchFeedbackEnabled) release()
        }
        if (horizontalDragOnly) {
            inspectHorizontalDragGestures(
                onDragStart = start,
                onDragEnd = end,
                onDragCancel = cancel,
                onTap = onTap,
            ) { change, dragAmount ->
                onDrag(size, dragAmount)
            }
        } else {
            inspectDragGestures(
                onDragStart = start,
                onDragEnd = end,
                onDragCancel = cancel,
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

    fun beginDirectDrag() {
        directDragTargetValue = valueAnimation.value
    }

    fun updateDirectDrag(value: Float) {
        val next = value.coerceIn(valueRange)
        directDragTargetValue = next
        velocityTracker.addPosition(
            SystemClock.uptimeMillis(),
            Offset(next, 0f),
        )
        val range = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
        val dragVelocity = velocityTracker.calculateVelocity().x / range
        directDragFrames.trySend(DragFrame(next, dragVelocity))
    }

    fun updateValue(value: Float) {
        val targetValue = value.coerceIn(valueRange)
        animationScope.launch {
            launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) { updateVelocity() } }
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
            Offset(value, 0f)
        )
        val targetVelocity = velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
        animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
    }
}
