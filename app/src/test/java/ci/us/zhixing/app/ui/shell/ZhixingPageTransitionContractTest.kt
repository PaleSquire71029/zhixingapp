package ci.us.zhixing.app.ui.shell

import org.junit.Assert.assertEquals
import org.junit.Test

class ZhixingPageTransitionContractTest {
    @Test
    fun preservesExistingSecondaryPageTransitionDurations() {
        assertEquals(230, ZhixingPageTransitionTokens.fadeInDurationMillis)
        assertEquals(280, ZhixingPageTransitionTokens.slideInDurationMillis)
        assertEquals(170, ZhixingPageTransitionTokens.fadeOutDurationMillis)
        assertEquals(220, ZhixingPageTransitionTokens.slideOutDurationMillis)
    }

    @Test
    fun preservesExistingSecondaryPageTransitionGeometryAndScale() {
        assertEquals(0.985f, ZhixingPageTransitionTokens.initialScale, 0.0001f)
        assertEquals(0.99f, ZhixingPageTransitionTokens.targetScale, 0.0001f)
        assertEquals(9, ZhixingPageTransitionTokens.slideInWidthDivisor)
        assertEquals(14, ZhixingPageTransitionTokens.slideOutWidthDivisor)
        assertEquals(
            "zhixingSecondaryPageTransition",
            ZhixingPageTransitionTokens.secondaryPageLabel,
        )
    }
}
