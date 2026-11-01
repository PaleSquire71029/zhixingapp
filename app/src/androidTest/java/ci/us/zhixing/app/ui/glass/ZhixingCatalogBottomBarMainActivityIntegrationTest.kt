package ci.us.zhixing.app.ui.glass

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZhixingCatalogBottomBarMainActivityIntegrationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityShowsFourCatalogTabsAndRetainsDestinationNavigation() {
        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)
        composeRule.onNode(tab("计算")).assertIsSelected()

        composeRule.onNode(tab("知行AI")).performClick()

        composeRule.onNode(tab("知行AI")).assertIsSelected()
        composeRule.onNodeWithText("让计算与理解连在一起").assertIsDisplayed()

        composeRule.onNode(tab("工具")).performClick()

        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNodeWithText("把复杂问题拆开处理").assertIsDisplayed()
    }

    @Test
    fun homepageGlassContainersKeepTheirContentAccessible() {
        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)
        composeRule.onAllNodes(hasText("基础界面已建立 · 功能按小步迁移")).assertCountEquals(1)
        composeRule.onAllNodes(hasText("原生 Android 工程")).assertCountEquals(1)
        composeRule.onAllNodes(hasText("知行浅色设计基线")).assertCountEquals(1)
        composeRule.onAllNodes(hasText("逐步迁移")).assertCountEquals(1)
    }

    @Test
    fun glassSettingsEntryUsesLiquidButtonAndOpensGlassLab() {
        composeRule.onNodeWithText("材质设置")
            .assertIsEnabled()
            .performClick()

        composeRule.onNodeWithText("实时液态玻璃").assertIsDisplayed()
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
