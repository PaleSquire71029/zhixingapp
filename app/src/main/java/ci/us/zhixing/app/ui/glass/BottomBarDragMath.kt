package ci.us.zhixing.app.ui.glass

import kotlin.math.roundToInt

/**
 * Resolves the nearest tab from the selected capsule's horizontal drag offset.
 * The offset is measured in pixels from the start of the bottom-bar content.
 */
internal fun resolveBottomBarDragIndex(
    indicatorOffsetPx: Float,
    itemWidthPx: Float,
    itemCount: Int,
): Int {
    require(itemCount > 0) { "At least one navigation item is required." }
    require(itemWidthPx.isFinite() && itemWidthPx > 0f) {
        "Navigation item width must be finite and positive."
    }
    require(indicatorOffsetPx.isFinite()) {
        "Indicator offset must be finite."
    }

    val maxOffset = itemWidthPx * (itemCount - 1)
    return (indicatorOffsetPx.coerceIn(0f, maxOffset) / itemWidthPx)
        .roundToInt()
        .coerceIn(0, itemCount - 1)
}

/** Direction and normalized stretch amount for one horizontal drag sample. */
internal data class DirectionalDragDeformation(
    val direction: Int,
    val stretch: Float,
)

/**
 * Converts horizontal pointer delta into a bounded, direction-aware capsule stretch.
 * Tiny jitter is ignored; larger deltas increase stretch up to a conservative cap.
 */
internal fun resolveDirectionalDragDeformation(
    deltaXPx: Float,
    itemWidthPx: Float,
): DirectionalDragDeformation {
    require(deltaXPx.isFinite()) { "Drag delta must be finite." }
    require(itemWidthPx.isFinite() && itemWidthPx > 0f) {
        "Navigation item width must be finite and positive."
    }

    val direction = when {
        deltaXPx > 0f -> 1
        deltaXPx < 0f -> -1
        else -> 0
    }
    val magnitude = kotlin.math.abs(deltaXPx)
    val stretch = if (magnitude < 1.5f) {
        0f
    } else {
        (magnitude / itemWidthPx * 0.9f).coerceIn(0.035f, 0.12f)
    }

    return DirectionalDragDeformation(
        direction = direction,
        stretch = stretch,
    )
}
