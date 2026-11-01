package ci.us.zhixing.app.ui.glass

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.MainActivity
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZhixingCatalogBottomBarInsetsOrientationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomBarMovesAboveImeAndRemainsAccessibleWhileTyping() {
        val insets = ImeInsetsSnapshot()

        composeRule.setContent {
            var input by remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current
            val imeBottom = androidx.compose.foundation.layout.WindowInsets.ime
                .getBottom(LocalDensity.current)
            SideEffect {
                insets.imeBottomPx = imeBottom
            }

            ZhixingTheme {
                BackdropHost(
                    modifier = Modifier.fillMaxSize(),
                ) { backdrop ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BasicTextField(
                                value = input,
                                onValueChange = { input = it },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { keyboardController?.hide() },
                                ),
                                modifier = Modifier.testTag(IME_INPUT_TAG),
                            )
                        }

                        ZhixingLiquidBottomBarPlacement {
                            ZhixingCatalogBottomBar(
                                backdrop = backdrop,
                                destinations = AppDestinations.all,
                                selectedIndex = 0,
                                onDestinationSelected = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(68.dp)
                                    .testTag(BOTTOM_BAR_TAG),
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag(IME_INPUT_TAG).performClick()
        composeRule.onNodeWithTag(IME_INPUT_TAG).performTextInput("123")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            insets.imeBottomPx > 0
        }

        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val barBounds = composeRule
            .onNodeWithTag(BOTTOM_BAR_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
        val imeTop = rootBounds.bottom - insets.imeBottomPx

        assertTrue(
            "Catalog bottom bar should be lifted above the visible IME.",
            barBounds.bottom <= imeTop + 4f,
        )
        composeRule.onAllNodes(tabRole()).assertCountEquals(4)
        composeRule.onNodeWithTag(BOTTOM_BAR_TAG).assertIsDisplayed()

        composeRule.onNodeWithTag(IME_INPUT_TAG).performImeAction()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            insets.imeBottomPx == 0
        }
        composeRule.onAllNodes(tabRole()).assertCountEquals(4)
        composeRule.onNodeWithTag(BOTTOM_BAR_TAG).assertIsDisplayed()
    }

    @Test
    fun fourDestinationsRemainAvailableAfterPortraitLandscapeRotation() {
        composeRule.onAllNodes(tabRole()).assertCountEquals(4)

        composeRule.activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.activity.resources.configuration.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
        }

        composeRule.onAllNodes(tabRole()).assertCountEquals(4)
        listOf("计算", "知行AI", "工具", "历史").forEach { label ->
            composeRule.onNode(tab(label)).assertIsDisplayed()
        }

        composeRule.activity.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.activity.resources.configuration.orientation ==
                Configuration.ORIENTATION_PORTRAIT
        }

        composeRule.onAllNodes(tabRole()).assertCountEquals(4)
        listOf("计算", "知行AI", "工具", "历史").forEach { label ->
            composeRule.onNode(tab(label)).assertIsDisplayed()
        }
    }

    private fun tabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        tabRole() and hasText(label, substring = true)

    private class ImeInsetsSnapshot {
        @Volatile
        var imeBottomPx: Int = 0
    }

    private companion object {
        const val IME_INPUT_TAG = "ime-insets-test-input"
        const val BOTTOM_BAR_TAG = "ime-insets-test-bottom-bar"
    }
}
