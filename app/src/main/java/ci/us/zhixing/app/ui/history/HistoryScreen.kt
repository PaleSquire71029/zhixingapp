package ci.us.zhixing.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.glass.BackdropHost
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.glass.ZhixingLiquidButton
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme

@Composable
internal fun HistoryScreen(
    onBack: () -> Unit,
) {
    BackdropHost(modifier = Modifier.fillMaxSize()) { backdrop ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ZhixingLiquidButton(
                    backdrop = backdrop,
                    onClick = onBack,
                    shape = ZhixingGlassShapes.pill,
                ) {
                    Text(
                        text = "‹  返回",
                        style = ZhixingTheme.type.featureTitle,
                        color = ZhixingTheme.colors.primary,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "历史记录",
                    style = ZhixingTheme.type.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "这里只展示本地计算记录，不会混入 AI 对话。",
                    style = ZhixingTheme.type.body,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }

            ZhixingGlassSurface(
                backdrop = backdrop,
                material = GlassMaterialState().copy(
                    refractionEnabled = false,
                    chromaticDispersionEnabled = false,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ZhixingGlassShapes.card),
                shape = ZhixingGlassShapes.card,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "还没有计算记录",
                        style = ZhixingTheme.type.featureTitle,
                        color = ZhixingTheme.colors.onSurface,
                    )
                    Text(
                        text = "本阶段尚未接入本地历史存储。计算功能与历史迁移完成后，这里才会显示真实记录。",
                        style = ZhixingTheme.type.supporting,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
