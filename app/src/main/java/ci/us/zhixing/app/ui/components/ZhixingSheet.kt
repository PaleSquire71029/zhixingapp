package ci.us.zhixing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/** Content-only sheet surface; presentation and visibility remain owned by the caller. */
@Composable
internal fun ZhixingSheet(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(
            topStart = ZhixingTheme.shapeTokens.floatingPanel,
            topEnd = ZhixingTheme.shapeTokens.floatingPanel,
        ),
        color = ZhixingTheme.colors.surface,
        contentColor = ZhixingTheme.colors.onSurface,
        shadowElevation = ZhixingTheme.elevation.flat,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            ZhixingTheme.colors.outline.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.12f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ZhixingTheme.colors.outline),
            )
            Text(
                text = title,
                style = ZhixingTheme.typography.sectionTitle,
                color = ZhixingTheme.colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            content()
        }
    }
}
