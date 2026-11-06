package ci.us.zhixing.app.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ci.us.zhixing.app.R
import ci.us.zhixing.app.navigation.AppNavigationIcon

/**
 * Rounded Material Symbols glyphs for navigation and the My subpages.
 * The glyph artwork comes from Google's Material Symbols Rounded family;
 * selection is expressed through a restrained scale and tint transition.
 */
@Composable
internal fun ZhixingNavigationIcon(
    icon: AppNavigationIcon,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.07f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "navigationIconScale",
    )
    val tint = if (selected) Color(0xFF477D9B) else Color(0xFF788995)
    val resource = when (icon) {
        AppNavigationIcon.CALCULATOR -> R.drawable.ic_nav_calculate
        AppNavigationIcon.AI -> R.drawable.ic_nav_smart_toy
        AppNavigationIcon.TOOLS -> R.drawable.ic_nav_handyman
        AppNavigationIcon.ME -> R.drawable.ic_nav_person
        AppNavigationIcon.HISTORY -> R.drawable.ic_nav_history
        AppNavigationIcon.MATERIAL -> R.drawable.ic_nav_tune
    }

    Image(
        painter = painterResource(resource),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier
            .then(Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
            })
            .alpha(if (selected) 1f else 0.88f)
            .then(Modifier),
    )
}
