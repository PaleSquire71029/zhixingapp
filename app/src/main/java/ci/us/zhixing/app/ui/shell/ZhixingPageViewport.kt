package ci.us.zhixing.app.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The page-content viewport layer inside the App Shell.
 *
 * The fixed ZhixingDockOverlay is intentionally rendered as a sibling of this
 * viewport. Keep this component layout-neutral: it does not add clipping,
 * scrolling, insets, padding or graphics transforms.
 */
@Composable
internal fun ZhixingPageViewport(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        content = content,
    )
}
