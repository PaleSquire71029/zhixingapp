package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.glass.backdrop106.components.LiquidBottomTab
import ci.us.zhixing.app.ui.glass.backdrop106.components.LiquidBottomTabs
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * Zhixing adapter for the pinned Backdrop Catalog 1.0.6 liquid tabs.
 *
 * Navigation state remains owned by the caller. The adapter only maps the
 * existing four destination model to the upstream tab component and keeps
 * Zhixing labels, glyphs, colors, and typography.
 *
 * This component is intentionally isolated in P2.2; MainActivity continues
 * to use the current production bottom bar until the later integration step.
 */
@Composable
internal fun ZhixingCatalogBottomBar(
    backdrop: Backdrop,
    destinations: List<AppDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(destinations.isNotEmpty()) {
        "At least one navigation destination is required."
    }
    require(selectedIndex in destinations.indices) {
        "Selected destination index is out of range."
    }

    LiquidBottomTabs(
        selectedTabIndex = { selectedIndex },
        onTabSelected = onDestinationSelected,
        backdrop = backdrop,
        tabsCount = destinations.size,
        modifier = modifier,
    ) {
        destinations.forEachIndexed { index, destination ->
            key(destination.route) {
                ZhixingCatalogBottomTab(
                    destination = destination,
                    selected = index == selectedIndex,
                    onClick = { onDestinationSelected(index) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.ZhixingCatalogBottomTab(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    LiquidBottomTab(
        onClick = onClick,
        modifier = Modifier.semantics {
            this.selected = selected
        },
    ) {
        Text(
            text = destination.symbol,
            style = ZhixingTheme.type.navigationSymbol,
            color = if (selected) {
                ZhixingTheme.colors.primary
            } else {
                ZhixingTheme.colors.onSurfaceVariant
            },
            maxLines = 1,
        )
        Text(
            text = destination.label,
            style = ZhixingTheme.type.supporting,
            color = if (selected) {
                ZhixingTheme.colors.primary
            } else {
                ZhixingTheme.colors.onSurfaceVariant
            },
            maxLines = 1,
        )
    }
}
