package ci.us.zhixing.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shared geometry and motion values for Zhixing Light Liquid Glass.
 *
 * These are design-system starting points, not platform or Apple implementation constants.
 * Keep material shape, clipping and backdrop sampling on the same Shape instance.
 */
internal object ZhixingGlassRadii {
    val chip = 12.dp
    val compactControl = 16.dp
    val control = 20.dp
    val card = 24.dp
    val hero = 28.dp
    val floatingPanel = 32.dp
}

/** Reusable shapes for the main glass component families. */
internal object ZhixingGlassShapes {
    val chip: Shape = RoundedCornerShape(ZhixingGlassRadii.chip)
    val compactControl: Shape = RoundedCornerShape(ZhixingGlassRadii.compactControl)
    val control: Shape = RoundedCornerShape(ZhixingGlassRadii.control)
    val card: Shape = RoundedCornerShape(ZhixingGlassRadii.card)
    val hero: Shape = RoundedCornerShape(ZhixingGlassRadii.hero)
    val floatingPanel: Shape = RoundedCornerShape(ZhixingGlassRadii.floatingPanel)
    val pill: Shape = CircleShape
}

/**
 * Motion tokens are expressed as stable values so controls can share one response.
 * Animations must alter drawing/layer properties, not layout size or hit targets.
 */
internal object ZhixingGlassMotion {
    const val buttonPressScale = 0.965f
    const val keypadPressScale = 0.95f
    const val cardPressScale = 0.985f

    const val pressSpringDampingRatio = 0.78f
    const val pressSpringStiffness = 520f

    const val controlStateTransitionMillis = 180
    const val pageTransitionMillis = 220
    const val sheetTransitionMillis = 280

    const val sheetEnterOffsetDp = 12f
    const val pageEnterOffsetDp = 8f
    const val itemEnterOffsetDp = 8f
    const val cardLiftDp = 2f
}
