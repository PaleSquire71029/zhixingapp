package ci.us.zhixing.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared radius scale; values preserve existing home and glass geometry. */
internal object ZhixingShapeTokens {
    val chip: Dp = 12.dp
    val compactControl: Dp = 16.dp
    val feature: Dp = 18.dp
    val control: Dp = 20.dp
    val card: Dp = 24.dp
    val hero: Dp = 28.dp
    val floatingPanel: Dp = 32.dp
}

/** Canonical shapes shared by regular surfaces and the glass adapter. */
internal object ZhixingShapes {
    val chip: Shape = RoundedCornerShape(ZhixingShapeTokens.chip)
    val compactControl: Shape = RoundedCornerShape(ZhixingShapeTokens.compactControl)
    val feature: Shape = RoundedCornerShape(ZhixingShapeTokens.feature)
    val control: Shape = RoundedCornerShape(ZhixingShapeTokens.control)
    val card: Shape = RoundedCornerShape(ZhixingShapeTokens.card)
    val hero: Shape = RoundedCornerShape(ZhixingShapeTokens.hero)
    val floatingPanel: Shape = RoundedCornerShape(ZhixingShapeTokens.floatingPanel)
    val pill: Shape = CircleShape
}
