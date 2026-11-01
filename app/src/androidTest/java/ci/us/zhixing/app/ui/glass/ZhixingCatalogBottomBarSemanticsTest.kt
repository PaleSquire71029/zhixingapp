package ci.us.zhixing.app.ui.glass

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZhixingCatalogBottomBarSemanticsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun exposesFourTabsWithOriginalLabelsAndCallerSelectedState() {
        composeRule.setContent {
            var selectedIndex by remember { mutableIntStateOf(1) }

            ZhixingTheme {
                BackdropHost(
                    modifier = Modifier.fillMaxSize(),
                ) { backdrop ->
                    ZhixingCatalogBottomBar(
                        backdrop = backdrop,
                        destinations = AppDestinations.all,
                        selectedIndex = selectedIndex,
                        onDestinationSelected = { selectedIndex = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                    )
                }
            }
        }

        composeRule.onAllNodes(hasTabRole()).assertCountEquals(4)

        listOf("计算", "知行AI", "工具", "历史").forEach { label ->
            composeRule.onNode(tab(label)).assertIsDisplayed()
        }

        composeRule.onNode(tab("知行AI")).assertIsSelected()
        composeRule.onNode(tab("计算")).assertIsNotSelected()
        composeRule.onNode(tab("工具")).assertIsNotSelected()
        composeRule.onNode(tab("历史")).assertIsNotSelected()
    }

    @Test
    fun clickingDestinationCallsTheExternalNavigationCallback() {
        composeRule.setContent {
            var selectedIndex by remember { mutableIntStateOf(0) }

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
                                .height(72.dp),
                        )
                        Text(
                            text = "Selected index: $selectedIndex",
                            modifier = Modifier.testTag("selected-index"),
                        )
                    }
                }
            }
        }

        composeRule.onNode(tab("工具")).performClick()

        composeRule.onNodeWithTag("selected-index").assertTextEquals("Selected index: 2")
        composeRule.onNode(tab("工具")).assertIsSelected()
        composeRule.onNode(tab("计算")).assertIsNotSelected()
    }

    @Test
    fun selectedSemanticsFollowChangesOwnedByTheCaller() {
        composeRule.setContent {
            var selectedIndex by remember { mutableIntStateOf(0) }

            ZhixingTheme {
                BackdropHost(
                    modifier = Modifier.fillMaxSize(),
                ) { backdrop ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(onClick = { selectedIndex = 3 }) {
                            Text("Select history externally")
                        }
                        ZhixingCatalogBottomBar(
                            backdrop = backdrop,
                            destinations = AppDestinations.all,
                            selectedIndex = selectedIndex,
                            onDestinationSelected = { selectedIndex = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp),
                        )
                    }
                }
            }
        }

        composeRule.onNode(tab("计算")).assertIsSelected()
        composeRule.onNode(tab("历史")).assertIsNotSelected()

        composeRule.onNodeWithText("Select history externally").performClick()

        composeRule.onNode(tab("历史")).assertIsSelected()
        composeRule.onNode(tab("计算")).assertIsNotSelected()
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
