package ci.us.zhixing.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * 知行统一主题入口。
 *
 * 颜色、文字、间距与形状从这里统一访问；Material 3 的默认 typography / shapes
 * 保持原样，避免改变尚未显式指定样式的组件外观。
 */
internal object ZhixingTheme {
    val colors = ZhixingLightColors
    val type = ZhixingType
    val spacing = ZhixingSpacing
    val shapes = ZhixingShapes

    @Composable
    operator fun invoke(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = colors,
            content = content,
        )
    }
}
