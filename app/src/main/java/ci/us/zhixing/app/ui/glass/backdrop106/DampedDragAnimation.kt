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
    private var lastDirectValue = initialValue
    private var lastDirectTimeMillis = 0L
    private var directDragGeneration: Long = 0L
    private var isPressed: Boolean = false
    private var pressGeneration: Long = 0L

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
        directDragGeneration += 1L
        if (!directDragging) {
            directValue = valueAnimation.value
        }
        directVelocity = 0f
        directDragging = true
        velocityTracker.resetTracking()
        val now = SystemClock.uptimeMillis()
        lastDirectValue = directValue
        lastDirectTimeMillis = now
        velocityTracker.addPosition(
            now,
            Offset(directValue, 0f),
        )
    }

    fun updateDirectDragValue(value: Float) {
        val next = value.coerceIn(valueRange)
        val now = SystemClock.uptimeMillis()
        val elapsedMillis = (now - lastDirectTimeMillis).coerceAtLeast(1L)
        val range = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
        val instantaneousVelocity =
            ((next - lastDirectValue) * 1000f / elapsedMillis) / range
        if (instantaneousVelocity.isFinite()) {
            // Low-cost velocity smoothing for the live squash effect. The
            // full least-squares VelocityTracker calculation is deferred
            // until release, keeping the 120Hz pointer path lightweight.
            directVelocity = directVelocity * 0.65f + instantaneousVelocity * 0.35f
        }
        directValue = next
        lastDirectValue = next
        lastDirectTimeMillis = now
        velocityTracker.addPosition(
            now,
            Offset(next, 0f),
        )
    }

    fun finishDirectDrag(finalValue: Float = directValue) {
        val position = directValue
        val range = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.0001f)
        val trackedVelocity = velocityTracker.calculateVelocity().x / range
        val dragVelocity = trackedVelocity.takeIf { it.isFinite() } ?: directVelocity
        val target = finalValue.coerceIn(valueRange)
        val generation = directDragGeneration
        animationScope.launch {
            // Cancel any stale spring and synchronize once on release. A
            // quick re-grab creates a new generation and must not be cleared
            // by this older release coroutine.
            valueAnimation.snapTo(position)
            velocityAnimation.snapTo(dragVelocity)
            if (generation != directDragGeneration) return@launch
            directDragging = false
            launch {
                valueAnimation.animateTo(
                    targetValue = target,
                    animationSpec = valueAnimationSpec,
                    // directVelocity is normalized to the 0..1 range for
                    // the squash effect; Animatable expects value-units/sec.
                    initialVelocity = dragVelocity *
                        (valueRange.endInclusive - valueRange.start),
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
                    if (touchFeedbackEnabled) {
                        press()
                    } else {
                        velocityTracker.resetTracking()
                    }
                    // Begin after press(), because press resets the velocity
                    // tracker; direct dragging then seeds the first sample.
                    onDragStarted(position)
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
                    if (touchFeedbackEnabled) {
                        press()
                    } else {
                        velocityTracker.resetTracking()
                    }
                    onDragStarted(down.position)
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
        if (isPressed) return
        isPressed = true
        pressGeneration += 1L
        velocityTracker.resetTracking()
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        if (!isPressed) return
        isPressed = false
        val generation = pressGeneration
        animationScope.launch {
            awaitFrame()
            // A new press may begin before this release frame. Ignore stale
            // release work so a quick re-grab does not collapse the new press.
            if (generation != pressGeneration || isPressed) return@launch
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            if (generation != pressGeneration || isPressed) return@launch
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
            SystemClock.uptimeMillis(),
            Offset(value, 0f),
        )
        val targetVelocity =
            velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
        animationScope.launch {
            velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec)
        }
    }
}
