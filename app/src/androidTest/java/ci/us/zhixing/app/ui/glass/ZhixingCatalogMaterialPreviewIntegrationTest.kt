package ci.us.zhixing.app.ui.glass

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun performancePresetPreviewAndCommitReturnToTheLiveCatalogBottomBar() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("实时液态玻璃").assertIsDisplayed()

        composeRule.onNodeWithText("Off").performClick()
        composeRule.onNodeWithText("性能模式 · Off").assertIsDisplayed()

        composeRule.onNodeWithText("Standard").performClick()
        composeRule.onNodeWithText("性能模式 · Standard").assertIsDisplayed()

        composeRule.onNodeWithText("← 返回主界面").performClick()

        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)
        composeRule.onNode(tab("计算")).assertIsSelected()

        composeRule.onNode(tab("工具")).performClick()
        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNodeWithText("把复杂问题拆开处理").assertIsDisplayed()
    }

    @Test
    fun resettingMaterialInGlassLabReturnsToDefaultCustomPresetAndHome() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("Off").performClick()
        composeRule.onNodeWithText("性能模式 · Off").assertIsDisplayed()

        composeRule.onNodeWithText("重置材质参数").performClick()
        composeRule.onNodeWithText("性能模式 · 自定义").assertIsDisplayed()

        composeRule.onNodeWithText("← 返回主界面").performClick()
        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)
        composeRule.onNode(tab("计算")).assertIsSelected()
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
