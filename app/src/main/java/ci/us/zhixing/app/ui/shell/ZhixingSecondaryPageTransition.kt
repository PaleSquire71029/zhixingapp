package ci.us.zhixing.app.ui.shell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ci.us.zhixing.app.navigation.AppSecondaryRoute

/**
 * Preserves the established secondary-page transition while giving the Shell
 * a single owner for its timing and motion values.
 */
internal object ZhixingPageTransitionTokens {
    const val fadeInDurationMillis = 230
    const val slideInDurationMillis = 280
    const val fadeOutDurationMillis = 170
    const val slideOutDurationMillis = 220
    const val initialScale = 0.985f
    const val targetScale = 0.99f
    const val slideInWidthDivisor = 9
    const val slideOutWidthDivisor = 14
    const val secondaryPageLabel = "zhixingSecondaryPageTransition"
}

@Composable
internal fun ZhixingSecondaryPageTransition(
    targetState: AppSecondaryRoute?,
    modifier: Modifier = Modifier,
    content: @Composable (AppSecondaryRoute?) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (
                fadeIn(tween(ZhixingPageTransitionTokens.fadeInDurationMillis)) +
                    slideInHorizontally(
                        tween(ZhixingPageTransitionTokens.slideInDurationMillis),
                    ) { width -> width / ZhixingPageTransitionTokens.slideInWidthDivisor } +
                    scaleIn(
                        initialScale = ZhixingPageTransitionTokens.initialScale,
                        animationSpec = tween(ZhixingPageTransitionTokens.slideInDurationMillis),
                    )
                ) togetherWith (
                fadeOut(tween(ZhixingPageTransitionTokens.fadeOutDurationMillis)) +
                    slideOutHorizontally(
                        tween(ZhixingPageTransitionTokens.slideOutDurationMillis),
                    ) { width -> -width / ZhixingPageTransitionTokens.slideOutWidthDivisor } +
                    scaleOut(
                        targetScale = ZhixingPageTransitionTokens.targetScale,
                        animationSpec = tween(ZhixingPageTransitionTokens.slideOutDurationMillis),
                    )
                )
        },
        label = ZhixingPageTransitionTokens.secondaryPageLabel,
    ) { pageRoute ->
        content(pageRoute)
    }
}
