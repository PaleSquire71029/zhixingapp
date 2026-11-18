package ci.us.zhixing.app.ui.theme

/** Compatibility adapters for the existing Backdrop glass implementation. */
internal object ZhixingGlassRadii {
    val chip get() = ZhixingShapeTokens.chip
    val compactControl get() = ZhixingShapeTokens.compactControl
    val control get() = ZhixingShapeTokens.control
    val card get() = ZhixingShapeTokens.card
    val hero get() = ZhixingShapeTokens.hero
    val floatingPanel get() = ZhixingShapeTokens.floatingPanel
}

/** Existing glass shape names now resolve to the shared shape instances. */
internal object ZhixingGlassShapes {
    val chip = ZhixingShapes.chip
    val compactControl = ZhixingShapes.compactControl
    val control = ZhixingShapes.control
    val card = ZhixingShapes.card
    val hero = ZhixingShapes.hero
    val floatingPanel = ZhixingShapes.floatingPanel
    val pill = ZhixingShapes.pill
}

/** Legacy motion accessors retained for existing glass callers and tests. */
internal object ZhixingGlassMotion {
    const val buttonPressScale = ZhixingMotionTokens.buttonPressScale
    const val keypadPressScale = ZhixingMotionTokens.keypadPressScale
    const val cardPressScale = ZhixingMotionTokens.cardPressScale
    const val pressSpringDampingRatio = ZhixingMotionTokens.pressSpringDampingRatio
    const val pressSpringStiffness = ZhixingMotionTokens.pressSpringStiffness
    const val controlStateTransitionMillis = ZhixingMotionTokens.controlStateTransitionMillis
    const val pageTransitionMillis = ZhixingMotionTokens.pageTransitionMillis
    const val sheetTransitionMillis = ZhixingMotionTokens.sheetTransitionMillis
    const val sheetEnterOffsetDp = ZhixingMotionTokens.sheetEnterOffsetDp
    const val pageEnterOffsetDp = ZhixingMotionTokens.pageEnterOffsetDp
    const val itemEnterOffsetDp = ZhixingMotionTokens.itemEnterOffsetDp
    const val cardLiftDp = ZhixingMotionTokens.cardLiftDp
}
