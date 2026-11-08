package ci.us.zhixing.app.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Positions floating bottom-bar content above system navigation and the IME.
 *
 * Call from any BoxScope whose bottom edge should anchor the bar; this can be
 * a full-screen overlay or a Scaffold bottom-bar container. Navigation-bar
 * insets are consumed first; IME padding then lifts the content above an
 * on-screen keyboard without adding the navigation inset twice. Horizontal
 * and bottom spacing remain separate from system inset handling.
 *
 * This helper only defines placement. It does not draw a surface, handle
 * navigation, or replace the Activity's existing bottom bar.
 */
@Composable
internal fun BoxScope.ZhixingLiquidBottomBarPlacement(
    modifier: Modifier = Modifier,
    horizontalMargin: Dp = 16.dp,
    bottomSpacing: Dp = 10.dp,
    liftForIme: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .navigationBarsPadding()
            .then(if (liftForIme) Modifier.imePadding() else Modifier)
            .padding(horizontal = horizontalMargin)
            .padding(bottom = bottomSpacing),
        content = content,
    )
}
