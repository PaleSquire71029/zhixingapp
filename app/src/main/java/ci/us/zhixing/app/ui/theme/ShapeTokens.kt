package ci.us.zhixing.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * 知行浅色界面的基础形状规范。
 *
 * 只集中管理当前首页已经使用的圆角，不改变圆角数值或页面视觉效果。
 */
internal object ZhixingShapes {
    val hero: Shape = RoundedCornerShape(28.dp)
    val feature: Shape = RoundedCornerShape(18.dp)
}
