package ci.us.zhixing.app.ui.foundation.scroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/**
 * Debug-only deep-link entry point for device validation.
 * This activity is excluded from release builds and does not participate in app navigation.
 */
internal class ZhixingScrollLabActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZhixingTheme {
                ZhixingScrollPrototypeScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
