package ci.us.zhixing.app.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Zhixing Light palette. Values remain identical to the pre-F1 theme. */
internal object ZhixingColorTokens {
    val primary = Color(0xFF4D7187)
    val onPrimary = Color.White
    val primaryContainer = Color(0xFFDCEAF1)
    val onPrimaryContainer = Color(0xFF1E3948)
    val secondary = Color(0xFF92775B)
    val onSecondary = Color.White
    val background = Color(0xFFF5F8FB)
    val onBackground = Color(0xFF202D37)
    val surface = Color(0xFFFFFFFF)
    val onSurface = Color(0xFF202D37)
    val surfaceVariant = Color(0xFFEDF2F6)
    val onSurfaceVariant = Color(0xFF687985)
    val outline = Color(0xFFD6E0E7)
}

/** Existing Material 3 scheme, now sourced from the shared palette. */
internal val ZhixingLightColors = lightColorScheme(
    primary = ZhixingColorTokens.primary,
    onPrimary = ZhixingColorTokens.onPrimary,
    primaryContainer = ZhixingColorTokens.primaryContainer,
    onPrimaryContainer = ZhixingColorTokens.onPrimaryContainer,
    secondary = ZhixingColorTokens.secondary,
    onSecondary = ZhixingColorTokens.onSecondary,
    background = ZhixingColorTokens.background,
    onBackground = ZhixingColorTokens.onBackground,
    surface = ZhixingColorTokens.surface,
    onSurface = ZhixingColorTokens.onSurface,
    surfaceVariant = ZhixingColorTokens.surfaceVariant,
    onSurfaceVariant = ZhixingColorTokens.onSurfaceVariant,
    outline = ZhixingColorTokens.outline,
)
