package ci.us.zhixing.app.ui.glass

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class BackdropHostInteractionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun sharedBackdropSupportsMultipleGlassSurfacesAndForegroundClicks() {
        composeRule.setContent {
            var tapCount by remember { mutableIntStateOf(0) }

            BackdropHost(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("backdrop-host"),
            ) { sharedBackdrop ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ZhixingGlassSurface(
                        backdrop = sharedBackdrop,
                        material = GlassMaterialState(
                            transparencyPercent = 38,
                            blurRadiusDp = 0f,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .testTag("glass-surface-one")
                            .clickable { tapCount += 1 },
                    ) {
                        Text(
                            text = "First glass surface",
                            modifier = Modifier.testTag("glass-content-one"),
                        )
                    }

                    ZhixingGlassSurface(
                        backdrop = sharedBackdrop,
                        material = GlassMaterialState(
                            transparencyPercent = 52,
                            blurRadiusDp = 0f,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .testTag("glass-surface-two")
                            .clickable { tapCount += 1 },
                    ) {
                        Text(
                            text = "Second glass surface",
                            modifier = Modifier.testTag("glass-content-two"),
                        )
                    }

                    Text(
                        text = "Tap count: $tapCount",
                        modifier = Modifier.testTag("tap-count"),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("backdrop-host").assertIsDisplayed()
        composeRule.onNodeWithTag("glass-content-one").assertIsDisplayed()
        composeRule.onNodeWithTag("glass-content-two").assertIsDisplayed()
        composeRule.onNodeWithTag("tap-count").assertTextEquals("Tap count: 0")

        composeRule.onNodeWithTag("glass-surface-one").performClick()
        composeRule.onNodeWithTag("tap-count").assertTextEquals("Tap count: 1")

        composeRule.onNodeWithTag("glass-surface-two").performClick()
        composeRule.onNodeWithTag("tap-count").assertTextEquals("Tap count: 2")
    }
}
