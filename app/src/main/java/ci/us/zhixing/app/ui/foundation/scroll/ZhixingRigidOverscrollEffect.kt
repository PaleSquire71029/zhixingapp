package ci.us.zhixing.app.ui.foundation.scroll

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import kotlin.math.abs

/**
 * The single page-level owner of edge drag and fling overscroll.
 * ScrollState remains within content bounds; only the outer viewport layer moves.
 */
@OptIn(ExperimentalFoundationApi::class)
internal class ZhixingRigidOverscrollEffect(
    private val maximumOffsetPx: Float,
) : OverscrollEffect {
    private val offsetState = mutableFloatStateOf(0f)

    // Rendering is intentionally owned by the outer viewport graphicsLayer.
    // The effect node is still attached by verticalScroll's overscroll parameter.
    override val node: DelegatableNode = object : Modifier.Node() {}

    val translationYPx: Float
        get() = offsetState.floatValue

    override val isInProgress: Boolean
        get() = abs(offsetState.floatValue) > 0.5f

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        // Reverse input first releases the current viewport displacement.
        val releaseDelta = ZhixingRigidOverscrollPhysics.releaseConsumedDelta(
            currentOffsetPx = offsetState.floatValue,
            incomingScrollDeltaPx = delta.y,
        )
        if (releaseDelta != 0f) {
            offsetState.floatValue -= releaseDelta
        }

        val remaining = delta - Offset(0f, releaseDelta)
        val consumedByContent = performScroll(remaining)
        val unconsumed = remaining - consumedByContent

        if (unconsumed.y != 0f) {
            offsetState.floatValue = ZhixingRigidOverscrollPhysics.resist(
                currentOffsetPx = offsetState.floatValue,
                unconsumedScrollDeltaPx = unconsumed.y,
                maximumOffsetPx = maximumOffsetPx,
            )
        }

        // Only the vertical edge remainder is consumed by this page effect.
        return consumedByContent + Offset(0f, releaseDelta + unconsumed.y)
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        // Foundation's contract requires the fling callback to be called once.
        val consumedVelocity = performFling(velocity)
        val remainingVelocity = velocity - consumedVelocity

        if (!isInProgress && abs(remainingVelocity.y) > 1f) {
            offsetState.floatValue = ZhixingRigidOverscrollPhysics.flingStartingOffset(
                remainingScrollVelocityPxPerSecond = remainingVelocity.y,
                maximumOffsetPx = maximumOffsetPx,
            )
        }

        val startOffset = offsetState.floatValue
        if (abs(startOffset) <= 0.5f) {
            offsetState.floatValue = 0f
            return
        }

        AnimationState(
            initialValue = startOffset,
            initialVelocity = -remainingVelocity.y,
        ).animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = ZhixingTheme.motion.pressSpringDampingRatio,
                stiffness = ZhixingTheme.motion.pressSpringStiffness,
            ),
        ) {
            offsetState.floatValue = value.coerceIn(-maximumOffsetPx, maximumOffsetPx)
        }
        offsetState.floatValue = 0f
    }
}

/**
 * Standalone page viewport with bounded rigid translation.
 * Keep Dock, system bars, IME affordances and fixed actions as siblings outside it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ZhixingPageScrollContainer(
    modifier: Modifier = Modifier,
    state: androidx.compose.foundation.ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val maximumOffsetPx = with(LocalDensity.current) { 48.dp.toPx() }
    val overscrollEffect = remember(maximumOffsetPx) {
        ZhixingRigidOverscrollEffect(maximumOffsetPx)
    }

    // The page effect is the only vertical overscroll owner inside this viewport.
    // Explicitly passing it to verticalScroll keeps the effect active even though
    // the local factory is null (which prevents child scrollables adding a second
    // platform stretch effect over the page).
    CompositionLocalProvider(LocalOverscrollFactory provides null) {
        Box(modifier = modifier.clipToBounds()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        // Deferred state read: per-frame motion changes only the
                        // outer layer translation, not composition or measurement.
                        translationY = overscrollEffect.translationYPx
                    }
                    .testTag("zhixing-rigid-viewport-layer"),
            ) {
                Column(
                    modifier = Modifier
                        .matchParentSize()
                        .verticalScroll(
                            state = state,
                            overscrollEffect = overscrollEffect,
                        ),
                ) {
                    Column(
                        modifier = Modifier.padding(contentPadding),
                        content = content,
                    )
                }
            }
        }
    }
}
