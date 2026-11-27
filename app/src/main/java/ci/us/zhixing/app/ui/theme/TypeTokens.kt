package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Zhixing Flow typography roles.
 * No font family is selected: Android / Compose default font fallback remains.
 * All sizes and metrics preserve the existing UI baseline.
 */
internal object ZhixingTypographyTokens {
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

/** Backward-compatible accessor used by existing screens. */
internal object ZhixingType {
    val brandTitle get() = ZhixingTypographyTokens.brandTitle
    val brandCaption get() = ZhixingTypographyTokens.brandCaption
    val badge get() = ZhixingTypographyTokens.badge
    val sectionLabel get() = ZhixingTypographyTokens.sectionLabel
    val screenTitle get() = ZhixingTypographyTokens.screenTitle
    val body get() = ZhixingTypographyTokens.body
    val sectionTitle get() = ZhixingTypographyTokens.sectionTitle
    val featureTitle get() = ZhixingTypographyTokens.featureTitle
    val supporting get() = ZhixingTypographyTokens.supporting
    val navigationSymbol get() = ZhixingTypographyTokens.navigationSymbol
}
