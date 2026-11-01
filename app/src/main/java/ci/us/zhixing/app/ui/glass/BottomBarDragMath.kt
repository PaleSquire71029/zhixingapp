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
