package ci.us.zhixing.app.ui.my

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.R
import ci.us.zhixing.app.navigation.AppNavigationIcon
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.navigation.ZhixingNavigationIcon
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

@Composable
internal fun MyScreen(
    backdrop: Backdrop,
    material: GlassMaterialState,
    modifier: Modifier = Modifier,
    onOpenHistory: () -> Unit,
    onOpenMaterial: () -> Unit,
) {
    val context = LocalContext.current
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
    val calmMaterial = material.copy(
        blurRadiusDp = 0f,
        refractionEnabled = false,
        chromaticDispersionEnabled = false,
        environmentReflectionEnabled = false,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ZhixingTheme.spacing.pageHorizontal)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.zhixing_logo),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "我的",
                    style = ZhixingTheme.type.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "本地空间 · 无需登录",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "最近使用",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            Text(
                text = "暂时没有本地计算记录",
                style = ZhixingTheme.type.featureTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            Text(
                text = "计算功能接入后，最近使用的内容会显示在这里。",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "记录与外观",
                style = ZhixingTheme.type.sectionTitle,
                color = ZhixingTheme.colors.onBackground,
            )
            ZhixingGlassSurface(
                backdrop = backdrop,
                material = calmMaterial,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ZhixingGlassShapes.card),
                shape = ZhixingGlassShapes.card,
            ) {
                Column {
                    MyActionRow(
                        icon = AppNavigationIcon.HISTORY,
                        title = "历史记录",
                        description = "查看本地计算记录",
                        onClick = onOpenHistory,
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 54.dp, end = 14.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ZhixingTheme.colors.outline.copy(alpha = 0.55f)),
                    )
                    MyActionRow(
                        icon = AppNavigationIcon.MATERIAL,
                        title = "材质设置",
                        description = "透明度、模糊、折射与触摸效果",
                        onClick = onOpenMaterial,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "知行计算器",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Text(
                text = if (versionName.isBlank()) "版本信息暂不可用" else "版本 $versionName",
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MyActionRow(
    icon: AppNavigationIcon,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        ZhixingNavigationIcon(
            icon = icon,
            selected = false,
            modifier = Modifier.size(24.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = ZhixingTheme.type.featureTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            Text(
                text = description,
                style = ZhixingTheme.type.supporting,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }
        Text(
            text = "›",
            style = ZhixingTheme.type.sectionTitle,
            color = ZhixingTheme.colors.onSurfaceVariant,
        )
    }
}
