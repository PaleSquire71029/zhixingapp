package ci.us.zhixing.app.ui.lab

import android.os.Build
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/**
 * Glass Lab 第 2 阶段：独立背景层柔化实验。
 *
 * Blur 只作用于本实验区域内单独绘制的背景层；前景玻璃样本与文字保持清晰。
 * 这不是跨组件或整屏的实时 backdrop capture。
 */
@Composable
internal fun GlassLabScreen(onBack: () -> Unit) {
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var blurEnabled by rememberSaveable { mutableStateOf(blurSupported) }
    val backgroundEffect = if (blurEnabled && blurSupported) {
        Modifier.blur(
            radius = 10.dp,
            edgeTreatment = BlurredEdgeTreatment.Unbounded,
        )
    } else {
        Modifier
    }

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
                text = "GLASS LAB / 02",
                modifier = Modifier.weight(1f),
                style = ZhixingTheme.type.brandCaption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.tight)) {
            Text(
                text = "背景层模糊实验",
                style = ZhixingTheme.type.screenTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "单独柔化玻璃后方的实验背景，前景样本与文字不参与模糊。",
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (blurEnabled && blurSupported) "背景模糊 · 10 dp" else "背景清晰对照",
                style = ZhixingTheme.type.sectionLabel,
                color = ZhixingTheme.colors.secondary,
            )
            TextButton(
                onClick = { blurEnabled = !blurEnabled },
                enabled = blurSupported,
            ) {
                Text(
                    text = if (blurEnabled) "关闭模糊" else "开启模糊",
                    style = ZhixingTheme.type.featureTitle,
                    color = ZhixingTheme.colors.primary,
                )
            }
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
                    .fillMaxSize()
                    .then(backgroundEffect),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-32).dp, y = 26.dp)
                        .size(188.dp)
                        .background(Color(0xFF86BFE3).copy(alpha = 0.92f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 34.dp, y = 22.dp)
                        .size(214.dp)
                        .background(Color(0xFFA99AE5).copy(alpha = 0.88f), CircleShape),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 22.dp, y = (-48).dp)
                        .size(128.dp)
                        .background(Color(0xFFE9B995).copy(alpha = 0.94f), CircleShape),
                )
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(188.dp),
                color = Color.White.copy(alpha = 0.62f),
                shape = ZhixingTheme.shapes.hero,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.96f)),
            ) {
                Column(
                    modifier = Modifier.padding(ZhixingTheme.spacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.cardContentGap),
                ) {
                    Text(
                        text = "LIGHT / LAYER 02",
                        style = ZhixingTheme.type.brandCaption,
                        color = ZhixingTheme.colors.secondary,
                    )
                    Text(
                        text = "柔化背景，保留焦点",
                        style = ZhixingTheme.type.sectionTitle,
                        color = ZhixingTheme.colors.onBackground,
                    )
                    Text(
                        text = "色彩位于后层，玻璃表面的文字依然清晰锐利。",
                        style = ZhixingTheme.type.body,
                        color = ZhixingTheme.colors.onSurface,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(ZhixingTheme.spacing.groupGap)) {
            Text(
                text = "实验说明",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = if (blurSupported) {
                    "当前系统支持硬件模糊。按钮可切换 10 dp 背景层模糊与清晰状态。"
                } else {
                    "当前 Android 版本低于 12，已回退为清晰背景；透明叠层仍可正常显示。"
                },
                style = ZhixingTheme.type.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = "技术边界：Compose Modifier.blur 只模糊该组件自身绘制的内容，不会自动采集任意兄弟组件或窗口背景。当前验证的是分离背景层的柔化与清晰前景，不是真正的实时 backdrop blur。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
    }
}
