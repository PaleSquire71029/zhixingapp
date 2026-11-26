package ci.us.zhixing.app.ui.foundation.scroll

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ZhixingRigidOverscrollPhysicsTest {
    private val maximum = 144f

    @Test
    fun contentWithoutScrollRangeDoesNotEnableOverscroll() {
        assertEquals(false, ZhixingRigidOverscrollPhysics.hasScrollableContent(0))
        assertEquals(false, ZhixingRigidOverscrollPhysics.hasScrollableContent(-1))
        assertEquals(true, ZhixingRigidOverscrollPhysics.hasScrollableContent(1))
    }

    @Test
    fun topEdgeUnconsumedScrollMovesViewportDown() {
        val offset = ZhixingRigidOverscrollPhysics.resist(0f, -40f, maximum)
        assertTrue(offset > 0f)
        assertTrue(offset < 40f)
    }

    @Test
    fun bottomEdgeUnconsumedScrollMovesViewportUp() {
        val offset = ZhixingRigidOverscrollPhysics.resist(0f, 40f, maximum)
        assertTrue(offset < 0f)
        assertTrue(offset > -40f)
    }

    @Test
    fun viewportTranslationNeverExceedsConfiguredLimit() {
        assertEquals(maximum, ZhixingRigidOverscrollPhysics.resist(0f, -10_000f, maximum), 0.001f)
        assertEquals(-maximum, ZhixingRigidOverscrollPhysics.resist(0f, 10_000f, maximum), 0.001f)
    }

    @Test
    fun resistanceWeakensAsViewportApproachesLimit() {
        val firstPull = abs(ZhixingRigidOverscrollPhysics.resist(0f, -10f, maximum))
        val nearLimitPull = abs(
            ZhixingRigidOverscrollPhysics.resist(maximum * 0.75f, -10f, maximum) -
                maximum * 0.75f,
        )
        assertTrue(firstPull > nearLimitPull)
        assertTrue(nearLimitPull > 0f)
    }

    @Test
    fun reverseGestureReleasesViewportBeforeContentScrolls() {
        val consumed = ZhixingRigidOverscrollPhysics.releaseConsumedDelta(30f, 12f)
        assertEquals(12f, consumed, 0.001f)
        assertEquals(18f, 30f - consumed, 0.001f)
    }

    @Test
    fun reverseGestureCannotReleasePastZero() {
        val consumed = ZhixingRigidOverscrollPhysics.releaseConsumedDelta(18f, 30f)
        assertEquals(18f, consumed, 0.001f)
        assertEquals(0f, 18f - consumed, 0.001f)
    }

    @Test
    fun flingProjectionUsesOppositeVisualDirectionAndRemainsBounded() {
        val offset = ZhixingRigidOverscrollPhysics.flingStartingOffset(-8_000f, maximum)
        assertTrue(offset > 0f)
        assertTrue(offset <= maximum)
    }
}
