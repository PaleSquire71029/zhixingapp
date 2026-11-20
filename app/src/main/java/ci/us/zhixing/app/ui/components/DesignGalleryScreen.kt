package ci.us.zhixing.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.ui.theme.ZhixingTheme

internal object ZhixingComponentLayoutTokens {
    val wideBreakpoint: Dp = 600.dp
}

internal fun isWideGalleryLayout(width: Dp): Boolean =
    width >= ZhixingComponentLayoutTokens.wideBreakpoint

/**
 * Standalone component playground. It is intentionally not registered in
 * MainActivity or app navigation during F2.
 */
@Composable
internal fun DesignGalleryScreen(
    modifier: Modifier = Modifier,
) {
    var selectedSegment by rememberSaveable { mutableIntStateOf(0) }
    var selectedRow by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ZhixingTheme.colors.background),
    ) {
        val wide = isWideGalleryLayout(maxWidth)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ZhixingTheme.layout.pageHorizontalPadding)
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Zhixing Flow",
                    style = ZhixingTheme.typography.screenTitle,
                    color = ZhixingTheme.colors.onBackground,
                )
                Text(
                    text = "组件目录 · 交互状态与尺寸预览",
                    style = ZhixingTheme.typography.supporting,
                    color = ZhixingTheme.colors.onSurfaceVariant,
                )
            }

            if (wide) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("design-gallery-wide"),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ButtonGallery()
                        InputGallery(value = input, onValueChange = { input = it })
                        ResultGallery()
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        SegmentGallery(
                            selectedIndex = selectedSegment,
                            onSelectedIndexChange = { selectedSegment = it },
                        )
                        ListGallery(
                            selectedIndex = selectedRow,
                            onSelectedIndexChange = { selectedRow = it },
                        )
                        SheetGallery()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("design-gallery-narrow"),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ButtonGallery()
                    InputGallery(value = input, onValueChange = { input = it })
                    SegmentGallery(
                        selectedIndex = selectedSegment,
                        onSelectedIndexChange = { selectedSegment = it },
                    )
                    ListGallery(
                        selectedIndex = selectedRow,
                        onSelectedIndexChange = { selectedRow = it },
                    )
                    ResultGallery()
                    SheetGallery()
                }
            }
        }
    }
}

@Composable
private fun ButtonGallery() {
    GallerySection(title = "Button · 按钮状态") {
        ZhixingButton(label = "主要操作", onClick = {}, modifier = Modifier.fillMaxWidth())
        ZhixingButton(
            label = "已选中",
            onClick = {},
            selected = true,
            variant = ZhixingButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingButton(
            label = "不可用",
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingButton(
            label = "正在加载",
            onClick = {},
            loading = true,
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingButton(
            label = "键盘焦点示例",
            onClick = {},
            variant = ZhixingButtonVariant.TERTIARY,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun InputGallery(
    value: String,
    onValueChange: (String) -> Unit,
) {
    GallerySection(title = "Input · 输入状态") {
        ZhixingInput(
            value = value,
            onValueChange = onValueChange,
            label = "普通输入",
            placeholder = "请输入内容",
            supportingText = "辅助说明保持简洁",
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingInput(
            value = "invalid@",
            onValueChange = {},
            label = "错误输入",
            errorMessage = "格式不正确",
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingInput(
            value = "不可编辑",
            onValueChange = {},
            label = "禁用输入",
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SegmentGallery(
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
) {
    GallerySection(title = "Segmented · 分段选择") {
        ZhixingSegmentedControl(
            options = listOf("标准", "科学", "函数"),
            selectedIndex = selectedIndex,
            onSelectedIndexChange = onSelectedIndexChange,
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingSegmentedControl(
            options = listOf("启用", "禁用"),
            selectedIndex = 0,
            onSelectedIndexChange = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ListGallery(
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
) {
    GallerySection(title = "ListRow · 列表行") {
        ZhixingListRow(
            title = "普通列表项",
            supportingText = "标题与辅助信息",
            onClick = { onSelectedIndexChange(0) },
            selected = selectedIndex == 0,
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingListRow(
            title = "已选中列表项",
            supportingText = "选择状态会提供语义反馈",
            onClick = { onSelectedIndexChange(1) },
            selected = selectedIndex == 1,
            modifier = Modifier.fillMaxWidth(),
        )
        ZhixingListRow(
            title = "禁用列表项",
            supportingText = "当前不可交互",
            enabled = false,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ResultGallery() {
    GallerySection(title = "ResultPanel · 结果面板") {
        ZhixingResultPanel(
            title = "计算结果",
            value = "x = 2",
            supportingText = "结果在本地计算",
        )
        ZhixingResultPanel(
            title = "输入错误",
            value = "无法计算",
            supportingText = "请检查表达式格式",
            isError = true,
        )
    }
}

@Composable
private fun SheetGallery() {
    GallerySection(title = "Sheet · 内容面板") {
        ZhixingSheet(title = "操作面板") {
            Text(
                text = "这里展示内容容器；显示、隐藏与拖拽由上层页面负责。",
                style = ZhixingTheme.typography.body,
                color = ZhixingTheme.colors.onSurfaceVariant,
            )
            ZhixingButton(label = "确认", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun GallerySection(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    ZhixingSurface(
        modifier = Modifier.fillMaxWidth(),
        containerColor = ZhixingTheme.colors.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = ZhixingTheme.typography.sectionTitle,
                color = ZhixingTheme.colors.onSurface,
            )
            content()
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DesignGalleryPhonePreview() {
    ZhixingTheme {
        DesignGalleryScreen()
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 1024)
@Composable
private fun DesignGalleryWidePreview() {
    ZhixingTheme {
        DesignGalleryScreen()
    }
}
