package ci.us.zhixing.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Existing page and component spacing, now owned by the Flow token set. */
internal object ZhixingSpacingTokens {
    val pageHorizontal: Dp = 22.dp
    val pageVertical: Dp = 28.dp
    val section: Dp = 20.dp
    val headerGap: Dp = 12.dp
    val tight: Dp = 3.dp
    val badgeHorizontal: Dp = 13.dp
    val badgeVertical: Dp = 8.dp
    val cardPadding: Dp = 24.dp
    val cardContentGap: Dp = 14.dp
    val hairlineGap: Dp = 2.dp
    val groupGap: Dp = 10.dp
    val featureHorizontal: Dp = 16.dp
    val featureVertical: Dp = 13.dp
}

/** Backward-compatible spacing accessor for existing screens. */
internal object ZhixingSpacing {
    val pageHorizontal get() = ZhixingSpacingTokens.pageHorizontal
    val pageVertical get() = ZhixingSpacingTokens.pageVertical
    val section get() = ZhixingSpacingTokens.section
    val headerGap get() = ZhixingSpacingTokens.headerGap
    val tight get() = ZhixingSpacingTokens.tight
    val badgeHorizontal get() = ZhixingSpacingTokens.badgeHorizontal
    val badgeVertical get() = ZhixingSpacingTokens.badgeVertical
    val cardPadding get() = ZhixingSpacingTokens.cardPadding
    val cardContentGap get() = ZhixingSpacingTokens.cardContentGap
    val hairlineGap get() = ZhixingSpacingTokens.hairlineGap
    val groupGap get() = ZhixingSpacingTokens.groupGap
    val featureHorizontal get() = ZhixingSpacingTokens.featureHorizontal
    val featureVertical get() = ZhixingSpacingTokens.featureVertical
}
