package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.navigation.AppDestination
import ci.us.zhixing.app.ui.theme.ZhixingTheme
import com.kyant.backdrop.Backdrop

/**
 * Four-destination content for the floating glass navigation shell.
 *
 * Selection and click handling are controlled by the caller. This component
 * intentionally has no position, system-inset, gesture, or animation policy;
 * those are added in later migration steps.
 */
@Composable
internal fun ZhixingLiquidBottomBar(
    backdrop: Backdrop,
    destinations: List<AppDestination>,
    selectedIndex: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(destinations.isNotEmpty()) { "At least one navigation destination is required." }
    require(selectedIndex in destinations.indices) { "Selected destination index is out of range." }

    ZhixingLiquidBottomBarSurface(
        backdrop = backdrop,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            destinations.forEachIndexed { index, destination ->
                val isSelected = index == selectedIndex
                val itemShape = RoundedCornerShape(24.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(itemShape)
                        .background(
                            if (isSelected) {
                                ZhixingTheme.colors.primaryContainer.copy(alpha = 0.82f)
                            } else {
                                Color.Transparent
                            },
                        )
                        .selectable(
                            selected = isSelected,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onDestinationSelected(index) },
                        )
                        .padding(horizontal = 2.dp, vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = destination.symbol,
                        style = ZhixingTheme.type.navigationSymbol,
                        color = if (isSelected) {
                            ZhixingTheme.colors.primary
                        } else {
                            ZhixingTheme.colors.onSurfaceVariant
                        },
                    )
                    Text(
                        text = destination.label,
                        style = ZhixingTheme.type.supporting,
                        color = if (isSelected) {
                            ZhixingTheme.colors.primary
                        } else {
                            ZhixingTheme.colors.onSurfaceVariant
                        },
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
