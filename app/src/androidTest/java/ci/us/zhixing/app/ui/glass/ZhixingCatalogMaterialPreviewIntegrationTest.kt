package ci.us.zhixing.app.ui.glass

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class ZhixingCatalogMaterialPreviewIntegrationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @After
    fun restoreDefaultMaterialSettings() {
        runBlocking {
            GlassMaterialSettingsStore(composeRule.activity).resetToDefaults()
        }
    }

    @Test
    fun performancePresetSelectionReturnsToTheLiveCatalogBottomBar() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("光学效果").assertIsDisplayed()

        composeRule.onAllNodesWithText("关闭").onLast().performClick()
        composeRule.onAllNodesWithText("关闭").assertCountEquals(2)

        composeRule.onAllNodesWithText("标准").onLast().performClick()
        composeRule.onAllNodesWithText("标准").assertCountEquals(2)

        composeRule.onNodeWithText("‹  返回").performClick()

        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)
        composeRule.onNode(tab("计算")).assertIsSelected()

        composeRule.onNode(tab("工具")).performClick()
        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNodeWithText("把复杂问题拆开处理").assertIsDisplayed()
    }

    @Test
    fun resettingMaterialInGlassLabReturnsToDefaultCustomPresetAndHome() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onAllNodesWithText("关闭").onLast().performClick()
        composeRule.onNodeWithText("关闭").assertIsDisplayed()

        composeRule.onNodeWithText("恢复默认").performClick()
        composeRule.onNodeWithText("自定义").assertIsDisplayed()

        composeRule.onNodeWithText("‹  返回").performClick()
        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)
        composeRule.onNode(tab("计算")).assertIsSelected()
    }


    @Test
    fun horizontalSliderDragChangesValueWithoutNeedingToScrollThePage() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("68%").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("透明程度").performTouchInput {
            val start = Offset(size.width * 0.35f, size.height / 2f)
            val end = Offset(size.width * 0.75f, size.height / 2f)
            swipe(start, end, durationMillis = 500)
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("68%").assertDoesNotExist()
    }

    @Test
    fun verticalSwipeStartedOnSliderStillScrollsTheSettingsPage() {
        composeRule.onNodeWithText("材质设置").performClick()
        val title = composeRule.onNodeWithText("材质设置")
        val originalTop = title.fetchSemanticsNode().boundsInRoot.top

        composeRule.onNodeWithContentDescription("透明程度").performTouchInput {
            val start = Offset(size.width * 0.68f, size.height / 2f)
            val end = Offset(start.x, start.y - 120f)
            swipe(start, end, durationMillis = 500)
        }

        composeRule.waitForIdle()
        val scrolledTop = title.fetchSemanticsNode().boundsInRoot.top
        assertTrue("A vertical gesture on the slider should scroll the settings page.", scrolledTop < originalTop)
        composeRule.onNodeWithText("68%").assertIsDisplayed()
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
