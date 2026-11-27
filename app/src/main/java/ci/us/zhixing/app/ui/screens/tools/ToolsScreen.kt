package ci.us.zhixing.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.foundation.scroll.ZhixingPageScrollContainer
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingGlassSurface
import ci.us.zhixing.app.ui.theme.ZhixingGlassShapes
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * F5.1 migrated tools directory.
 *
 * The directory keeps its existing content and action contract, but now uses
 * the shared rigid page-scroll viewport. The Unit Converter remains a separate
 * secondary page and is intentionally not migrated in this step.
 */
@Composable
internal fun ToolsScreen(
    backdrop: Backdrop,
    material: GlassMaterialState,
    modifier: Modifier = Modifier,
    onOpenUnitConverter: () -> Unit,
) {
    val calmMaterial = material.copy(
        refractionEnabled = false,
        chromaticDispersionEnabled = false,
        environmentReflectionEnabled = false,
    )

    ZhixingPageScrollContainer(
        modifier = modifier.testTag("tools-page-scroll-container"),
        contentPadding = PaddingValues(
            horizontal = ZhixingTheme.spacing.pageHorizontal,
            top = 30.dp,
            bottom = 112.dp,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "工具",
                    style = ZhixingTheme.type.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "常用工具独立呈现，计算逻辑在本地完成。",
                    style = ZhixingTheme.type.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "实用工具",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(ZhixingGlassShapes.card)
                            .clickable(role = Role.Button, onClick = onOpenUnitConverter)
                            .padding(horizontal = 17.dp, vertical = 17.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(15.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⇄",
                                    style = ZhixingTheme.type.screenTitle,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "单位换算",
                                style = ZhixingTheme.type.featureTitle,
                                color = ZhixingTheme.colors.onSurface,
                            )
                            Text(
                                text = "长度、面积、体积、质量、温度等九大类别",
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
            }
        }
    }
}
