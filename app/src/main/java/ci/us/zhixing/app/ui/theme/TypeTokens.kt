package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 知行文字层级 Tokens。
 *
 * 字号、行高、字重与字距对应当前首页已经使用的 Material 3 默认文字样式；
 * 不指定自定义字体文件，继续使用 Android / Compose 默认字体回退。
 * 本文件只集中管理样式，不在本步骤引入新字体或改变视觉效果。
 */
internal object ZhixingType {
    val brandTitle = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
    val brandCaption = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp)
    val badge = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp)
    val sectionLabel = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp)
    val screenTitle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
    val body = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp)
    val sectionTitle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.sp)
    val featureTitle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.25.sp)
    val supporting = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp)
    val navigationSymbol = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium)
}
