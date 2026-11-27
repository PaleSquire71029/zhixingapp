package ci.us.zhixing.app.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/** Labelled text input with one consistent error and supporting-text contract. */
@Composable
internal fun ZhixingInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    val helperText = errorMessage ?: supportingText
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .semantics {
                if (errorMessage != null) {
                    stateDescription = "错误：$errorMessage"
                }
            },
        enabled = enabled,
        label = { Text(label) },
        placeholder = placeholder?.let { text -> { Text(text) } },
        supportingText = helperText?.let { text ->
            {
                Text(
                    text = text,
                    color = if (errorMessage != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        ZhixingTheme.colors.onSurfaceVariant
                    },
                )
            }
        },
        isError = errorMessage != null,
        singleLine = singleLine,
        shape = ZhixingShapes.control,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ZhixingTheme.colors.primary,
            unfocusedBorderColor = ZhixingTheme.colors.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = ZhixingTheme.colors.primary,
            unfocusedLabelColor = ZhixingTheme.colors.onSurfaceVariant,
            cursorColor = ZhixingTheme.colors.primary,
            disabledBorderColor = ZhixingTheme.colors.outline.copy(alpha = 0.5f),
        ),
    )
}
