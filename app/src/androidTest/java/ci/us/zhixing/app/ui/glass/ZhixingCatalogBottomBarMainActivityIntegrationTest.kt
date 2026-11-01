package ci.us.zhixing.app.ui.glass

import android.os.Build
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
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

    @After
    fun restoreDefaultMaterialSettings() {
        runBlocking {
            GlassMaterialSettingsStore(composeRule.activity).resetToDefaults()
        }
    }

    @Test
    fun glassLabMigratesEverySwitchAndSliderWithoutChangingMaterialStateRules() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("实时液态玻璃").assertIsDisplayed()
        composeRule.onNodeWithText("Standard").performClick()
        composeRule.onNodeWithText("性能模式 · Standard").assertIsDisplayed()

        fun switchNode(label: String) = composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch) and
                hasContentDescription(label),
        )

        listOf("边缘高光", "环境反射", "触摸光效与形变").forEach { label ->
            val node = switchNode(label)
            node.assertIsDisplayed().assertIsEnabled()
            val wasOn =
                node.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                    ToggleableState.On
            node.performClick()
            if (wasOn) node.assertIsOff() else node.assertIsOn()
            node.performClick()
            if (wasOn) node.assertIsOn() else node.assertIsOff()
        }

        fun sliderNode(label: String) = composeRule.onNode(
            hasContentDescription(label),
        )

        val transparency = sliderNode("透明程度")
        transparency.assertIsDisplayed().assertIsEnabled()
        transparency.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
            setProgress(75f)
        }
        composeRule.onNodeWithText("透明程度 · 75%").assertIsDisplayed()

        val blur = sliderNode("背景模糊")
        blur.assertIsDisplayed()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            blur.assertIsEnabled()
            blur.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(7f)
            }
            composeRule.onNodeWithText("背景模糊 · 7.0 dp").assertIsDisplayed()
        } else {
            blur.assertIsNotEnabled()
        }

        val refraction = sliderNode("折射强度")
        refraction.assertIsDisplayed()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            refraction.assertIsEnabled()
            refraction.performSemanticsAction(SemanticsActions.SetProgress) { setProgress ->
                setProgress(3.5f)
            }
            composeRule.onNodeWithText("折射强度 · 3.5 dp").assertIsDisplayed()
        } else {
            refraction.assertIsNotEnabled()
        }
    }

    @Test
    fun refractionSettingUsesAccessibleLiquidSwitch() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("实时液态玻璃").assertIsDisplayed()

        val refractionSwitch = composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch) and
                hasContentDescription("边缘折射"),
        )
        refractionSwitch.assertIsDisplayed()
        refractionSwitch.assertIsOn()
        refractionSwitch.performClick()
        refractionSwitch.assertIsOff()
        composeRule.onNodeWithText("边缘折射 · 关闭").assertIsDisplayed()
    }

    @Test
    fun chromaticDispersionUsesLiquidSwitchAndRequiresRefraction() {
        composeRule.onNodeWithText("材质设置").performClick()
        composeRule.onNodeWithText("实时液态玻璃").assertIsDisplayed()

        fun switchNode(label: String) = composeRule.onNode(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Switch) and
                hasContentDescription(label),
        )

        val refractionSwitch = switchNode("边缘折射")
        val dispersionSwitch = switchNode("色散折射")

        // The tests share the app's persistent settings. Restore the dependency
        // after checking the disabled state so this test does not leave side effects.
        val refractionWasOn =
            refractionSwitch.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On

        if (!refractionWasOn) {
            refractionSwitch.performClick()
        }
        refractionSwitch.performClick()
        dispersionSwitch.assertIsNotEnabled()
        refractionSwitch.performClick()

        val dispersionWasOn =
            dispersionSwitch.fetchSemanticsNode().config[SemanticsProperties.ToggleableState] ==
                ToggleableState.On
        dispersionSwitch.assertIsEnabled()
        dispersionSwitch.performClick()
        if (dispersionWasOn) {
            dispersionSwitch.assertIsOff()
            composeRule.onNodeWithText("色散折射 · 关闭").assertIsDisplayed()
        } else {
            dispersionSwitch.assertIsOn()
            composeRule.onNodeWithText("色散折射 · 开启").assertIsDisplayed()
        }
        // Return persisted preferences to their starting values.
        dispersionSwitch.performClick()
        if (!refractionWasOn) {
            refractionSwitch.performClick()
        }
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
