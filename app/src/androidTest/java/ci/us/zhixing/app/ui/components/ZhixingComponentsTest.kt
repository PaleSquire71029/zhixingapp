package ci.us.zhixing.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZhixingComponentsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun buttonExposesClickDisabledAndLoadingStates() {
        composeRule.setContent {
            var clickCount by remember { mutableIntStateOf(0) }
            ZhixingTheme {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ZhixingButton(
                        label = "提交",
                        onClick = { clickCount += 1 },
                        modifier = Modifier.testTag("primary-button"),
                    )
                    androidx.compose.material3.Text(
                        text = "点击 $clickCount",
                        modifier = Modifier.testTag("click-count"),
                    )
                    ZhixingButton(
                        label = "不可用",
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.testTag("disabled-button"),
                    )
                    ZhixingButton(
                        label = "正在保存",
                        onClick = {},
                        loading = true,
                        modifier = Modifier.testTag("loading-button"),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("primary-button").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("click-count").assertTextEquals("点击 1")
        composeRule.onNodeWithTag("disabled-button").assertIsNotEnabled()
        composeRule.onNodeWithTag("loading-button").assertIsNotEnabled()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                "加载中",
            ),
        ).assertExists()
    }

    @Test
    fun segmentedControlReportsSelectionAndUpdatesOnClick() {
        composeRule.setContent {
            var selectedIndex by remember { mutableIntStateOf(0) }
            ZhixingTheme {
                ZhixingSegmentedControl(
                    options = listOf("标准", "科学", "函数"),
                    selectedIndex = selectedIndex,
                    onSelectedIndexChange = { selectedIndex = it },
                    modifier = Modifier.testTag("segments"),
                )
            }
        }

        composeRule.onNodeWithTag("zhixing-segment-0").assertIsSelected()
        composeRule.onNodeWithTag("zhixing-segment-1").assertIsNotSelected()
        composeRule.onNodeWithTag("zhixing-segment-1").performClick()
        composeRule.onNodeWithTag("zhixing-segment-1").assertIsSelected()
        composeRule.onNodeWithTag("zhixing-segment-0").assertIsNotSelected()
    }

    @Test
    fun listRowReportsSelectedStateAndDisabledAction() {
        composeRule.setContent {
            var selected by remember { mutableIntStateOf(0) }
            ZhixingTheme {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ZhixingListRow(
                        title = "历史记录",
                        selected = selected == 0,
                        onClick = { selected = 1 },
                        modifier = Modifier.testTag("history-row"),
                    )
                    ZhixingListRow(
                        title = "不可用",
                        enabled = false,
                        onClick = {},
                        modifier = Modifier.testTag("disabled-row"),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("history-row").assertIsSelected().performClick()
        composeRule.onNodeWithTag("history-row").assertIsNotSelected()
        composeRule.onNodeWithTag("disabled-row").assertIsNotEnabled()
    }


    @Test
    fun buttonAnnouncesKeyboardFocusState() {
        composeRule.setContent {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(focusRequester) {
                focusRequester.requestFocus()
            }
            ZhixingTheme {
                ZhixingButton(
                    label = "键盘焦点",
                    onClick = {},
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .testTag("focused-button"),
                )
            }
        }

        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                "已聚焦",
            ),
        ).assertExists()
    }

    @Test
    fun inputAndResultExposeVisibleErrorFeedback() {
        composeRule.setContent {
            ZhixingTheme {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZhixingInput(
                        value = "invalid@",
                        onValueChange = {},
                        label = "邮箱",
                        errorMessage = "格式不正确",
                        modifier = Modifier.testTag("error-input"),
                    )
                    ZhixingResultPanel(
                        title = "计算失败",
                        value = "无法计算",
                        supportingText = "请检查表达式",
                        isError = true,
                        modifier = Modifier.testTag("error-result"),
                    )
                }
            }
        }

        composeRule.onNodeWithText("格式不正确").assertIsDisplayed()
        composeRule.onNode(
            SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                "错误：格式不正确",
            ),
        ).assertExists()
        composeRule.onNodeWithText("无法计算").assertIsDisplayed()
        composeRule.onNodeWithTag("error-result").assertIsDisplayed()
    }
}
