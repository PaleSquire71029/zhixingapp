package ci.us.zhixing.app.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import ci.us.zhixing.app.R
import ci.us.zhixing.app.navigation.AppNavigationIcon

/**
 * Solid, rounded tab glyphs inspired by Apple's filled SF Symbols treatment.
 * All destinations stay filled; selection is expressed through tint and a
 * restrained scale response rather than switching to hollow line art.
 */
@Composable
internal fun ZhixingNavigationIcon(
    icon: AppNavigationIcon,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val resource = when (icon) {
        AppNavigationIcon.CALCULATOR -> R.drawable.zhixing_ic_calculate_fill
        AppNavigationIcon.AI -> R.drawable.zhixing_ic_smart_toy_fill
        AppNavigationIcon.TOOLS -> R.drawable.zhixing_ic_handyman_fill
        AppNavigationIcon.ME -> R.drawable.zhixing_ic_person_fill
        AppNavigationIcon.HISTORY -> R.drawable.zhixing_ic_history_fill
        AppNavigationIcon.MATERIAL -> R.drawable.zhixing_ic_tune_fill
    }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 0.96f,
        animationSpec = tween(durationMillis = 190),
        label = "zhixingSolidTabScale",
    )
    val tint = if (selected) Color(0xFF087BFA) else Color(0xFF667085)

    Image(
        painter = painterResource(resource),
        contentDescription = null,
        modifier = modifier.scale(scale).fillMaxSize(),
        colorFilter = ColorFilter.tint(tint),
    )
}
