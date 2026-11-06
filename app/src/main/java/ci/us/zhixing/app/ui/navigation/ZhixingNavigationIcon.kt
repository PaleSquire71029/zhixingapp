package ci.us.zhixing.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import ci.us.zhixing.app.R
import ci.us.zhixing.app.navigation.AppNavigationIcon
import ci.us.zhixing.app.ui.theme.ZhixingTheme

/**
 * A single Material Symbols Rounded family for navigation and My subpages.
 *
 * The 48px Google source geometry is converted to native VectorDrawable
 * resources, so icons are crisp, local, and independent of network access.
 * Outline-to-fill transitions are layered and animated with the tab state.
 */
@Composable
internal fun ZhixingNavigationIcon(
    icon: AppNavigationIcon,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val resources = when (icon) {
        AppNavigationIcon.CALCULATOR ->
            R.drawable.zhixing_ic_calculate_outline to R.drawable.zhixing_ic_calculate_fill
        AppNavigationIcon.AI ->
            R.drawable.zhixing_ic_smart_toy_outline to R.drawable.zhixing_ic_smart_toy_fill
        AppNavigationIcon.TOOLS ->
            R.drawable.zhixing_ic_handyman_outline to R.drawable.zhixing_ic_handyman_fill
        AppNavigationIcon.ME ->
            R.drawable.zhixing_ic_person_outline to R.drawable.zhixing_ic_person_fill
        AppNavigationIcon.HISTORY ->
            R.drawable.zhixing_ic_history_outline to R.drawable.zhixing_ic_history_outline
        AppNavigationIcon.MATERIAL ->
            R.drawable.zhixing_ic_tune_outline to R.drawable.zhixing_ic_tune_outline
    }

    val fillProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "zhixingNavigationIconFill",
    )
    val tint by animateColorAsState(
        targetValue = if (selected) {
            ZhixingTheme.colors.primary
        } else {
            ZhixingTheme.colors.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 150),
        label = "zhixingNavigationIconTint",
    )

    Box(modifier = modifier) {
        Image(
            painter = painterResource(resources.first),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alpha = 1f - fillProgress,
            colorFilter = ColorFilter.tint(tint),
        )
        Image(
            painter = painterResource(resources.second),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alpha = fillProgress,
            colorFilter = ColorFilter.tint(tint),
        )
    }
}
