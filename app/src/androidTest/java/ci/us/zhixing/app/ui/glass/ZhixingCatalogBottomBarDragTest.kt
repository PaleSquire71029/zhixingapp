package ci.us.zhixing.app.ui.glass

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZhixingCatalogBottomBarDragTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun draggingRightAcrossTwoTabsSelectsTheDestinationUnderTheLens() {
        setBottomBar(initialIndex = 0)

        dragAcrossBar(
            startFraction = 0.125f,
            endFractions = listOf(0.625f),
        )

        assertSelectedIndex(2)
        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNode(tab("计算")).assertIsNotSelected()
    }

    @Test
    fun draggingLeftAcrossTwoTabsSelectsTheDestinationUnderTheLens() {
        setBottomBar(initialIndex = 3)

        dragAcrossBar(
            startFraction = 0.875f,
            endFractions = listOf(0.375f),
        )

        assertSelectedIndex(1)
        composeRule.onNode(tab("知行AI")).assertIsSelected()
        composeRule.onNode(tab("历史")).assertIsNotSelected()
    }

    @Test
    fun rapidDirectionReversalSettlesOnTheFinalDragPosition() {
        setBottomBar(initialIndex = 1)

        dragAcrossBar(
            startFraction = 0.375f,
            endFractions = listOf(0.75f, 0.625f),
            delayMillis = 24L,
        )

        assertSelectedIndex(2)
        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNode(tab("知行AI")).assertIsNotSelected()
    }

    @Test
    fun draggingPastTheLeadingEdgeKeepsTheFirstDestinationSelected() {
        setBottomBar(initialIndex = 0)

        dragAcrossBar(
            startFraction = 0.13f,
            endFractions = listOf(0f),
        )

        assertSelectedIndex(0)
        composeRule.onNode(tab("计算")).assertIsSelected()
        composeRule.onNode(tab("知行AI")).assertIsNotSelected()
    }

    private fun setBottomBar(initialIndex: Int) {
        composeRule.setContent {
            var selectedIndex by remember { mutableIntStateOf(initialIndex) }

            ZhixingTheme {
                BackdropHost(
                    modifier = Modifier.fillMaxSize(),
                ) { backdrop ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        ZhixingCatalogBottomBar(
                            backdrop = backdrop,
                            destinations = AppDestinations.all,
                            selectedIndex = selectedIndex,
                            onDestinationSelected = { selectedIndex = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .testTag(BOTTOM_BAR_TAG),
                        )
                        Text(
                            text = "Selected index: $selectedIndex",
                            modifier = Modifier.testTag(SELECTED_INDEX_TAG),
                        )
                    }
                }
            }
        }
    }

    private fun dragAcrossBar(
        startFraction: Float,
        endFractions: List<Float>,
        delayMillis: Long = 96L,
    ) {
        composeRule.onNodeWithTag(BOTTOM_BAR_TAG).performTouchInput {
            val y = center.y
            down(Offset(size.width * startFraction, y))
            endFractions.forEach { fraction ->
                moveTo(
                    Offset(size.width * fraction, y),
                    delayMillis = delayMillis,
                )
            }
            up()
        }
        composeRule.waitForIdle()
    }

    private fun assertSelectedIndex(expected: Int) {
        composeRule
            .onNodeWithTag(SELECTED_INDEX_TAG)
            .assertTextEquals("Selected index: $expected")
    }

    private fun tab(label: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab) and
            hasText(label, substring = true)

    private companion object {
        const val BOTTOM_BAR_TAG = "catalog-bottom-bar"
        const val SELECTED_INDEX_TAG = "selected-index"
    }
}
