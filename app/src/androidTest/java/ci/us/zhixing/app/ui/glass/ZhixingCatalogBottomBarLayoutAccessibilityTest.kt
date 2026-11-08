package ci.us.zhixing.app.ui.glass

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.navigation.AppDestinations
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZhixingCatalogBottomBarLayoutAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rtlLayoutPlacesFirstDestinationOnTheRight() {
        setBottomBar(
            layoutDirection = LayoutDirection.Rtl,
            widthDp = 320.dp,
        )

        val calculatorBounds = composeRule
            .onNode(tab("计算"))
            .fetchSemanticsNode()
            .boundsInRoot
        val historyBounds = composeRule
            .onNode(tab("历史"))
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "RTL should place the first destination to the right of the last one.",
            calculatorBounds.center.x > historyBounds.center.x,
        )
        listOf("计算", "知行AI", "工具", "历史").forEach { label ->
            composeRule.onNode(tab(label)).assertIsDisplayed()
        }
    }

    @Test
    fun narrowWidthKeepsEveryDestinationVisibleAndWithUsableBounds() {
        setBottomBar(
            layoutDirection = LayoutDirection.Ltr,
            widthDp = 240.dp,
        )

        listOf("计算", "知行AI", "工具", "历史").forEach { label ->
            val bounds = composeRule
                .onNode(tab(label))
                .fetchSemanticsNode()
                .boundsInRoot

            composeRule.onNode(tab(label)).assertIsDisplayed()
            assertTrue(
                "Tab $label should retain a non-empty visible touch target.",
                bounds.width > 0f && bounds.height > 0f,
            )
        }
    }

    @Test
    fun hiddenSamplingLayerDoesNotDuplicateAccessibleTabs() {
        setBottomBar(
            layoutDirection = LayoutDirection.Ltr,
            widthDp = 320.dp,
        )

        composeRule
            .onAllNodes(hasTabRole(), useUnmergedTree = true)
            .assertCountEquals(AppDestinations.all.size)
    }

    private fun setBottomBar(
        layoutDirection: LayoutDirection,
        widthDp: androidx.compose.ui.unit.Dp,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides layoutDirection,
            ) {
                ZhixingTheme {
                    BackdropHost(
                        modifier = Modifier.fillMaxSize(),
                    ) { backdrop ->
                        Column {
                            ZhixingCatalogBottomBar(
                                backdrop = backdrop,
                                destinations = AppDestinations.all,
                                selectedIndex = 0,
                                onDestinationSelected = {},
                                modifier = Modifier
                                    .width(widthDp)
                                    .height(72.dp)
                                    .testTag("catalog-bottom-bar"),
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun hasTabRole() =
        SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun tab(label: String) =
        hasTabRole() and hasText(label, substring = true)
}
