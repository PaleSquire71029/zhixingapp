package ci.us.zhixing.app.ui.shell

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class ZhixingAppShellNavigationIntegrationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun allFourRootTabsRemainSelectableInTheirOriginalOrder() {
        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)

        composeRule.onNode(tab("计算")).assertIsSelected()
        composeRule.onNode(tab("知行 AI")).performClick()
        composeRule.onNode(tab("知行 AI")).assertIsSelected()
        composeRule.onNodeWithText("让计算与理解连在一起").assertIsDisplayed()

        composeRule.onNode(tab("工具")).performClick()
        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNodeWithText("常用工具独立呈现，计算逻辑在本地完成。").assertIsDisplayed()

        composeRule.onNode(tab("我的")).performClick()
        composeRule.onNode(tab("我的")).assertIsSelected()
        composeRule.onNodeWithText("本地空间 · 无需登录").assertIsDisplayed()

        composeRule.onNode(tab("计算")).performClick()
        composeRule.onNode(tab("计算")).assertIsSelected()
        composeRule.onNodeWithText("让计算回归清晰").assertIsDisplayed()
    }

    @Test
    fun androidBackClosesHistoryAndReturnsToTheMyRootTab() {
        composeRule.onNode(tab("我的")).performClick()
        composeRule.onNodeWithText("历史记录").performClick()
        composeRule.onNodeWithText("还没有计算记录").assertIsDisplayed()

        pressAndroidBack()

        composeRule.onNode(tab("我的")).assertIsSelected()
        composeRule.onNodeWithText("记录与外观").assertIsDisplayed()
        composeRule.onNodeWithText("材质设置").assertIsDisplayed()
    }

    @Test
    fun androidBackClosesUnitConverterAndReturnsToToolsRootTab() {
        composeRule.onNode(tab("工具")).performClick()
        composeRule.onNodeWithText("单位换算").performClick()
        composeRule.onNodeWithText("换算类别").assertIsDisplayed()

        pressAndroidBack()

        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNodeWithText("常用工具独立呈现，计算逻辑在本地完成。").assertIsDisplayed()
        composeRule.onNodeWithText("单位换算").assertIsDisplayed()
    }

    @Test
    fun savedSecondaryRouteAndRootTabRestoreAfterActivityRecreation() {
        composeRule.onNode(tab("我的")).performClick()
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("实时液态玻璃").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText("实时液态玻璃")).fetchSemanticsNodes().isNotEmpty()
        }

        // The restored secondary route is still active. Back must restore the
        // previously selected MY root tab, not silently fall back to Calculator.
        pressAndroidBack()

        composeRule.onNode(tab("我的")).assertIsSelected()
        composeRule.onNodeWithText("记录与外观").assertIsDisplayed()
        composeRule.onNodeWithText("材质设置").assertIsDisplayed()
    }

    @Test
    fun toolsDirectoryUsesTheRigidPageScrollContainer() {
        composeRule.onNode(tab("工具")).performClick()
        composeRule.onNodeWithTag("tools-page-scroll-container").assertIsDisplayed()
        composeRule.onNodeWithText("单位换算").assertIsDisplayed()
    }

    @Test
    fun myPageUsesRigidPageScrollContainerAndKeepsActions() {
        composeRule.onNode(tab("我的")).performClick()
        composeRule.onNodeWithTag("my-page-scroll-container").assertIsDisplayed()
        composeRule.onNodeWithText("最近使用").assertIsDisplayed()
        composeRule.onNodeWithText("历史记录").assertIsDisplayed()
        composeRule.onNodeWithText("材质设置").assertIsDisplayed()
    }

    @Test
    fun historyPageUsesRigidPageScrollContainerAndKeepsEmptyState() {
        composeRule.onNode(tab("我的")).performClick()
        composeRule.onNodeWithText("历史记录").performClick()
        composeRule.onNodeWithTag("history-page-scroll-container").assertIsDisplayed()
        composeRule.onNodeWithText("还没有计算记录").assertIsDisplayed()
        composeRule.onNodeWithText("这里只展示本地计算记录，不会混入 AI 对话。").assertIsDisplayed()
    }

    @Test
    fun materialSettingsUsesRigidScrollingAndKeepsOpticalControls() {
        composeRule.onNode(tab("我的")).performClick()
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithTag("material-settings-page-scroll-container").assertIsDisplayed()
        composeRule.onNodeWithText("恢复默认").assertIsDisplayed()
        composeRule.onNodeWithText("透明度").assertIsDisplayed()
    }

    @Test
    fun pageViewportAndDockAreDirectSiblingLayersInTheShellHost() {
        val host = composeRule.onNodeWithTag(
            "zhixing-shell-layer-host",
            useUnmergedTree = true,
        )
        val viewport = composeRule.onNodeWithTag(
            "zhixing-page-viewport",
            useUnmergedTree = true,
        ).fetchSemanticsNode()
        val dock = composeRule.onNodeWithTag(
            "zhixing-dock-overlay",
            useUnmergedTree = true,
        ).fetchSemanticsNode()

        val directChildIds = host.onChildren().fetchSemanticsNodes().map { it.id }

        assertEquals("Shell host should own exactly the page and Dock layers.", 2, directChildIds.size)
        assertTrue("Page viewport must be a direct Shell child.", viewport.id in directChildIds)
        assertTrue("Dock overlay must be a direct Shell child.", dock.id in directChildIds)
        assertEquals("Page viewport should occupy the full Shell host.", 
            host.fetchSemanticsNode().boundsInRoot,
            viewport.boundsInRoot,
        )
    }

    private fun pressAndroidBack() {
        composeRule.runOnUiThread {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
