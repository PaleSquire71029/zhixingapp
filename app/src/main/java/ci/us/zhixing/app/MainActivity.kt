package ci.us.zhixing.app

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ci.us.zhixing.app.ui.glass.GlassMaterialSettingsStore
import ci.us.zhixing.app.ui.shell.ZhixingAppRoot

/**
 * Android entry point.
 *
 * Window policy stays here; navigation, root UI state, Backdrop ownership and
 * page composition are delegated to the Zhixing Flow shell.
 */
class MainActivity : ComponentActivity() {
    private val glassMaterialSettingsStore by lazy {
        GlassMaterialSettingsStore(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Target SDK 36 uses edge-to-edge system bars. Keep icons dark on the
        // light Zhixing canvas and let Compose content respect safe drawing insets.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        setContent {
            ZhixingAppRoot(materialSettingsStore = glassMaterialSettingsStore)
        }
    }
}
