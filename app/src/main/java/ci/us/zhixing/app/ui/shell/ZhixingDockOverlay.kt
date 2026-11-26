package ci.us.zhixing.app.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.glass.GlassMaterialState
import ci.us.zhixing.app.ui.glass.ZhixingCatalogBottomBar
import ci.us.zhixing.app.ui.glass.ZhixingLiquidBottomBarPlacement
import com.kyant.backdrop.Backdrop

/**
 * Fixed navigation overlay owned by the App Shell, never by a page viewport.
 *
 * This is deliberately a structural extraction: the existing Backdrop catalog
 * component, 68dp height, material settings, IME / navigation-bar insets and
 * destination callback are preserved without changing glass rendering.
 */
@Composable
internal fun BoxScope.ZhixingDockOverlay(
    backdrop: Backdrop,
    material: GlassMaterialState,
    destinations: List<AppDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier.align(Alignment.BottomCenter),
) {
    Box(modifier = modifier) {
        ZhixingLiquidBottomBarPlacement {
            ZhixingCatalogBottomBar(
                backdrop = backdrop,
                material = material,
                destinations = destinations,
                selectedIndex = selectedIndex,
                onDestinationSelected = onDestinationSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
            )
        }
    }
}
