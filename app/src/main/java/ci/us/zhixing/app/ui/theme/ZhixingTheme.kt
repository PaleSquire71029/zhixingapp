package ci.us.zhixing.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Zhixing Flow theme entry point.
 * Existing colors / type / spacing / shapes accessors remain source compatible.
 * Material 3 default Typography and Shapes are intentionally unchanged in F1.
 */
internal object ZhixingTheme {
    val colors = ZhixingLightColors
    val palette = ZhixingColorTokens
    val type = ZhixingType
    val typography = ZhixingTypographyTokens
    val spacing = ZhixingSpacing
    val spacingTokens = ZhixingSpacingTokens
    val shapes = ZhixingShapes
    val elevation = ZhixingElevationTokens
    val material = ZhixingMaterialTokens
    val motion = ZhixingMotionTokens
    val interaction = ZhixingInteractionTokens
    val layout = ZhixingLayoutTokens

    @Composable
    operator fun invoke(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = colors,
            content = content,
        )
    }
}
