package ci.us.zhixing.app.ui.foundation.scroll

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sign

/**
 * Pure pixel-space rules for the rigid page overscroll prototype.
 * Scroll deltas use content direction; viewport translation uses the opposite sign.
 */
internal object ZhixingRigidOverscrollPhysics {
    const val baseResistance = 0.52f
    const val flingProjectionSeconds = 0.016f

    fun hasScrollableContent(maxScrollPx: Int): Boolean = maxScrollPx > 0

    fun releaseConsumedDelta(
        currentOffsetPx: Float,
        incomingScrollDeltaPx: Float,
    ): Float {
        if (!currentOffsetPx.isFinite() || !incomingScrollDeltaPx.isFinite()) return 0f
        if (currentOffsetPx == 0f || currentOffsetPx * incomingScrollDeltaPx <= 0f) return 0f

        return sign(currentOffsetPx) * min(
            abs(currentOffsetPx),
            abs(incomingScrollDeltaPx),
        )
    }

    fun resist(
        currentOffsetPx: Float,
        unconsumedScrollDeltaPx: Float,
        maximumOffsetPx: Float,
    ): Float {
        if (!maximumOffsetPx.isFinite() || maximumOffsetPx <= 0f) return 0f
        if (!unconsumedScrollDeltaPx.isFinite()) {
            return currentOffsetPx.coerceIn(-maximumOffsetPx, maximumOffsetPx)
        }

        val current = if (currentOffsetPx.isFinite()) {
            currentOffsetPx.coerceIn(-maximumOffsetPx, maximumOffsetPx)
        } else {
            0f
        }
        val progress = (abs(current) / maximumOffsetPx).coerceIn(0f, 1f)
        val resistance = baseResistance * (1f - progress).pow(2)
        val viewportDelta = -unconsumedScrollDeltaPx * resistance

        return (current + viewportDelta).coerceIn(-maximumOffsetPx, maximumOffsetPx)
    }

    fun flingStartingOffset(
        remainingScrollVelocityPxPerSecond: Float,
        maximumOffsetPx: Float,
    ): Float {
        if (!remainingScrollVelocityPxPerSecond.isFinite()) return 0f
        return resist(
            currentOffsetPx = 0f,
            unconsumedScrollDeltaPx =
                remainingScrollVelocityPxPerSecond * flingProjectionSeconds,
            maximumOffsetPx = maximumOffsetPx,
        )
    }
}
