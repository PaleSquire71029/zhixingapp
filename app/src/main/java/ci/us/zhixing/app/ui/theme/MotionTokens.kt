package ci.us.zhixing.app.ui.theme

/** Shared animation values; existing response values are kept exactly. */
internal object ZhixingMotionTokens {
    const val microTransitionMillis = 120
    const val buttonPressScale = 0.965f
    const val keypadPressScale = 0.95f
    const val cardPressScale = 0.985f

    const val pressSpringDampingRatio = 0.78f
    const val pressSpringStiffness = 520f

    const val controlStateTransitionMillis = 180
    const val contentTransitionMillis = 220
    const val navigationTransitionMillis = 220
    const val pageTransitionMillis = 220
    const val sheetTransitionMillis = 280

    const val sheetEnterOffsetDp = 12f
    const val pageEnterOffsetDp = 8f
    const val itemEnterOffsetDp = 8f
    const val cardLiftDp = 2f
}
