package ci.us.zhixing.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/** Consistent result container for normal output and typed error presentation. */
@Composable
internal fun ZhixingResultPanel(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
) {
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        ZhixingTheme.colors.surfaceVariant
    }
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        ZhixingTheme.colors.onSurface
    }
    ZhixingSurface(
        modifier = modifier,
        shape = ZhixingShapes.card,
        containerColor = containerColor,
        contentColor = contentColor,
        borderColor = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.28f) else {
            ZhixingTheme.colors.outline.copy(alpha = 0.45f)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = if (isError) "结果错误" else "计算结果"
                }
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = ZhixingTheme.typography.supporting,
                color = contentColor,
            )
            Text(
                text = value,
                style = ZhixingTheme.typography.sectionTitle,
                color = contentColor,
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = ZhixingTheme.typography.supporting,
                    color = contentColor,
                )
            }
        }
    }
}
