package ci.us.zhixing.app.ui.foundation.scroll

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class ZhixingScrollPrototypeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun topOverscrollTranslatesViewportWithoutMovingFixedSiblings() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            val scrollState = rememberScrollState()
            ZhixingTheme {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.height(48.dp).fillMaxWidth().testTag("fixed-header"))
                    ZhixingPageScrollContainer(
                        modifier = Modifier.weight(1f).testTag("scroll-host"),
                        state = scrollState,
                    ) {
                        repeat(20) { index ->
                            Text(
                                text = "Scrollable item " + index,
                                modifier = Modifier.height(56.dp).fillMaxWidth(),
                            )
                        }
                    }
                    Box(Modifier.height(48.dp).fillMaxWidth().testTag("fixed-footer"))
                }
            }
        }

        val initialViewportTop = composeRule.onNodeWithTag("zhixing-rigid-viewport-layer")
            .fetchSemanticsNode().boundsInRoot.top
        val initialHeaderTop = composeRule.onNodeWithTag("fixed-header")
            .fetchSemanticsNode().boundsInRoot.top
        val initialFooterTop = composeRule.onNodeWithTag("fixed-footer")
            .fetchSemanticsNode().boundsInRoot.top

        composeRule.onNodeWithTag("scroll-host").performTouchInput {
            swipeDown(durationMillis = 180)
        }
        composeRule.mainClock.advanceTimeBy(16)

        val translatedViewportTop = composeRule.onNodeWithTag("zhixing-rigid-viewport-layer")
            .fetchSemanticsNode().boundsInRoot.top
        val translatedHeaderTop = composeRule.onNodeWithTag("fixed-header")
            .fetchSemanticsNode().boundsInRoot.top
        val translatedFooterTop = composeRule.onNodeWithTag("fixed-footer")
            .fetchSemanticsNode().boundsInRoot.top

        assertTrue(
            "The viewport should follow a top-edge pull",
            translatedViewportTop > initialViewportTop,
        )
        assertTrue("The fixed header must not translate", translatedHeaderTop == initialHeaderTop)
        assertTrue("The fixed footer must not translate", translatedFooterTop == initialFooterTop)

        composeRule.mainClock.advanceTimeBy(1_000)
        val settledViewportTop = composeRule.onNodeWithTag("zhixing-rigid-viewport-layer")
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue(
            "The viewport should spring back to rest",
            abs(settledViewportTop - initialViewportTop) < 1.5f,
        )
    }

    @Test
    fun horizontalChildKeepsHorizontalGestureOwnership() {
        val outerScrollState = ScrollState(0)
        val horizontalScrollState = ScrollState(0)

        composeRule.setContent {
            ZhixingTheme {
                ZhixingPageScrollContainer(
                    modifier = Modifier.fillMaxSize().testTag("horizontal-parent"),
                    state = outerScrollState,
                ) {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(horizontalScrollState)
                            .testTag("horizontal-child"),
                    ) {
                        repeat(10) { index ->
                            Text(
                                text = "Chip " + index,
                                modifier = Modifier.width(140.dp).padding(8.dp),
                            )
                        }
                    }
                    repeat(10) { index ->
                        Text("Page item " + index, modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        composeRule.onNodeWithTag("horizontal-child").performTouchInput {
            swipeLeft(durationMillis = 250)
        }

        assertTrue("Horizontal drag should scroll the inner row", horizontalScrollState.value > 0)
        assertTrue("Horizontal drag must not scroll the page", outerScrollState.value == 0)
    }

    @Test
    fun normalVerticalGestureStillScrollsContent() {
        val scrollState = ScrollState(0)
        composeRule.setContent {
            ZhixingTheme {
                ZhixingPageScrollContainer(
                    modifier = Modifier.fillMaxSize().testTag("vertical-parent"),
                    state = scrollState,
                ) {
                    repeat(24) { index ->
                        Text("Scrollable item " + index, modifier = Modifier.height(64.dp))
                    }
                }
            }
        }

        composeRule.onNodeWithTag("vertical-parent").performTouchInput {
            swipeUp(durationMillis = 350)
        }

        assertTrue("Normal vertical drag should scroll content", scrollState.value > 0)
    }
}
