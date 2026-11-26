package ci.us.zhixing.app.ui.foundation.scroll

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.components.ZhixingButton
import ci.us.zhixing.app.ui.components.ZhixingInput
import ci.us.zhixing.app.ui.components.ZhixingSurface
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/**
 * F3's isolated Scroll Lab. It is deliberately not registered in MainActivity or navigation.
 */
@Composable
internal fun ZhixingScrollPrototypeScreen(
    modifier: Modifier = Modifier,
) {
    var input by rememberSaveable { mutableStateOf("") }
    var selectedChip by rememberSaveable { mutableIntStateOf(0) }
    var sliderValue by remember { mutableFloatStateOf(0.42f) }
    var switchValue by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("zhixing-scroll-prototype"),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .testTag("f3-fixed-header"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "知行·流序",
                style = ZhixingTheme.typography.sectionTitle,
                color = ZhixingTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "F3 · Scroll Lab",
                style = ZhixingTheme.typography.brandCaption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clipToBounds()
                .testTag("f3-scroll-viewport-host"),
        ) {
            ZhixingPageScrollContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("zhixing-scroll-container"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 18.dp,
                    vertical = 16.dp,
                ),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "整页刚性回弹",
                        style = ZhixingTheme.typography.screenTitle,
                        color = ZhixingTheme.colors.onSurface,
                    )
                    Text(
                        text = "正常滚动到边缘后继续拖动，整个内容视口会有限平移；松手后以弹簧回位，不缩放、不拉伸。",
                        style = ZhixingTheme.typography.body,
                        color = ZhixingTheme.colors.onSurfaceVariant,
                    )

                    ZhixingSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("手势规则", style = ZhixingTheme.typography.sectionTitle)
                            Text("① 顶部继续下拉：视口向下移动")
                            Text("② 底部继续上推：视口向上移动")
                            Text("③ 反向拖动：先释放视口位移，再滚动内容")
                            Text("④ 横向控件、输入框和滑块保留自己的手势")
                        }
                    }

                    Text(
                        text = "横向滚动控件（左右滑动）",
                        style = ZhixingTheme.typography.sectionTitle,
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(6) { index ->
                            FilterChip(
                                selected = selectedChip == index,
                                onClick = { selectedChip = index },
                                label = { Text("分类 " + (index + 1)) },
                            )
                        }
                    }

                    Text(
                        text = "输入与焦点（可调出 IME）",
                        style = ZhixingTheme.typography.sectionTitle,
                    )
                    ZhixingInput(
                        value = input,
                        onValueChange = { input = it },
                        label = "测试输入",
                        placeholder = "点击后输入文字",
                        supportingText = "输入框随页面内容整体滚动",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "滑块与开关（横向手势）",
                        style = ZhixingTheme.typography.sectionTitle,
                    )
                    ZhixingSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("连续滑块 " + (sliderValue * 100).toInt() + "%")
                            Slider(
                                value = sliderValue,
                                onValueChange = { sliderValue = it },
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("测试开关")
                                Spacer(Modifier.weight(1f))
                                Switch(
                                    checked = switchValue,
                                    onCheckedChange = { switchValue = it },
                                )
                            }
                        }
                    }

                    repeat(12) { index ->
                        ZhixingSurface(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            ) {
                                Text(
                                    text = "滚动项目 " + (index + 1),
                                    style = ZhixingTheme.typography.sectionTitle,
                                )
                                Text(
                                    text = "用于检查长内容滚动、顶底边界和反向手势接管。",
                                    style = ZhixingTheme.typography.body,
                                    color = ZhixingTheme.colors.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    ZhixingButton(
                        label = "内容末尾",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .testTag("f3-fixed-footer"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "固定操作层 · 不随视口平移",
                style = ZhixingTheme.typography.caption,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Y",
                style = ZhixingTheme.typography.caption,
                color = ZhixingTheme.colors.primary,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ZhixingScrollPrototypePreview() {
    ZhixingTheme {
        ZhixingScrollPrototypeScreen()
    }
}
