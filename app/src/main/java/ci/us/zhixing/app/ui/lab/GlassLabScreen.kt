package ci.us.zhixing.app.ui.lab

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/**
 * Glass Lab 第 1 阶段：静态材质构图。
 *
 * 这里只演示彩色背景与半透明前景层叠，不声称实现实时背景采样、模糊或折射。
 */
@Composable
internal fun GlassLabScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZhixingTheme.colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZhixingTheme.spacing.pageHorizontal)
            .padding(vertical = ZhixingTheme.spacing.pageVertical),
        verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.section),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "← 返回主界面",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
            Text(
                text = "GLASS LAB / 01",
                modifier = Modifier.weight(1f),
                style = ZhixingTheme.type.brandCaption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "静态材质样本",
                style = ZhixingTheme.type.screenTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "先确认透明叠层、轮廓与文字可读性，再逐步加入真实光学效果。",
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(ZhixingTheme.shapes.hero)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE6F1F8),
                            Color(0xFFEDEAF8),
                            Color(0xFFF8EEE5),
                        ),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-32).dp, y = 26.dp)
                    .size(188.dp)
                    .background(Color(0xFFB6D8EF).copy(alpha = 0.88f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 34.dp, y = 22.dp)
                    .size(214.dp)
                    .background(Color(0xFFC9C0F0).copy(alpha = 0.82f), CircleShape),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 22.dp, y = (-48).dp)
                    .size(128.dp)
                    .background(Color(0xFFF1D7C4).copy(alpha = 0.9f), CircleShape),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(188.dp),
                color = Color.White.copy(alpha = 0.66f),
                shape = ZhixingTheme.shapes.hero,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.92f)),
            ) {
                Column(
                    modifier = Modifier.padding(ZhixingTheme.spacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.cardContentGap),
                ) {
                    Text(
                        text = "LIGHT / LAYER 01",
                        style = ZhixingTheme.type.brandCaption,
                        color = ZhixingTheme.colors.secondary,
                    )
                    Text(
                        text = "轻透叠层",
                        style = ZhixingTheme.type.sectionTitle,
                        color = ZhixingTheme.colors.onBackground,
                    )
                    Text(
                        text = "让背景色彩透过浅色表层，主体文字仍保持清晰。",
                        style = ZhixingTheme.type.body,
                        color = ZhixingTheme.colors.onSurface,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.groupGap)) {
            Text(
                text = "本轮观察重点",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "透明度 · 边缘轮廓 · 内容可读性",
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = "说明：当前仅为静态透明叠层示意，不包含实时背景采样、模糊、折射、色散或动态效果。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
    }
}
