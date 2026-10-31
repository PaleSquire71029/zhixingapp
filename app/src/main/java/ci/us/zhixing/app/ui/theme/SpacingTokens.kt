package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 知行首页与基础组件的间距基线。
 *
 * 本次只集中管理已有 dp 间距，不改变数值、布局顺序或视觉效果。
 * 图标尺寸、圆角、描边等非间距尺寸暂不纳入此对象。
 */
internal object ZhixingSpacing {
    val pageHorizontal: Dp = 22.dp
    val pageVertical: Dp = 28.dp
    val section: Dp = 20.dp
    val headerGap: Dp = 12.dp
    val tight: Dp = 3.dp
    val badgeHorizontal: Dp = 13.dp
    val badgeVertical: Dp = 8.dp
    val cardPadding: Dp = 24.dp
    val cardContentGap: Dp = 14.dp
    val hairlineGap: Dp = 2.dp
    val groupGap: Dp = 10.dp
    val featureHorizontal: Dp = 16.dp
    val featureVertical: Dp = 13.dp
}
