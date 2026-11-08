package ci.us.zhixing.app.ui.glass

import androidx.compose.ui.geometry.Offset

/**
 * Converts a press position local to one tab into the shared bottom-bar
 * coordinate space. Navigation hit targets are not transformed by this map.
 */
internal fun mapBottomBarTouchPosition(
    itemIndex: Int,
    itemCount: Int,
    itemWidthPx: Float,
    localPosition: Offset,
): Offset {
    require(itemCount > 0) { "At least one navigation item is required." }
    require(itemIndex in 0 until itemCount) { "Navigation item index is out of range." }
    require(itemWidthPx.isFinite() && itemWidthPx > 0f) {
        "Navigation item width must be finite and positive."
    }
    require(localPosition.x.isFinite() && localPosition.y.isFinite()) {
        "Touch coordinates must be finite."
    }

    return Offset(
        x = itemIndex * itemWidthPx + localPosition.x,
        y = localPosition.y,
    )
}
