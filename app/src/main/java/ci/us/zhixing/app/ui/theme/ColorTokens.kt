package ci.us.zhixing.app.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 知行浅色配色基线。
 *
 * 本次只集中管理既有颜色，不调整任何颜色值或页面视觉效果。
 * 后续颜色变更应优先在这里进行，并保持浅色外观。
 */
internal val ZhixingLightColors = lightColorScheme(
    primary = Color(0xFF4D7187),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEAF1),
    onPrimaryContainer = Color(0xFF1E3948),
    secondary = Color(0xFF92775B),
    onSecondary = Color.White,
    background = Color(0xFFF5F8FB),
    onBackground = Color(0xFF202D37),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202D37),
    surfaceVariant = Color(0xFFEDF2F6),
    onSurfaceVariant = Color(0xFF687985),
    outline = Color(0xFFD6E0E7),
)
