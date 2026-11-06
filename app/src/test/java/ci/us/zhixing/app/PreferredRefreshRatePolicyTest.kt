package ci.us.zhixing.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferredRefreshRatePolicyTest {
    @Test
    fun android14AndNewerCanRequest120EvenIfModeListDoesNotAdvertiseIt() {
        assertEquals(120f, resolvePreferredRefreshRate(34, listOf(60f, 90f))!!, 0f)
        assertEquals(120f, resolvePreferredRefreshRate(35, emptyList())!!, 0f)
    }

    @Test
    fun olderAndroidOnlyUsesAnAdvertisedModeNear120() {
        assertEquals(120f, resolvePreferredRefreshRate(33, listOf(60f, 120f, 144f))!!, 0f)
        assertNull(resolvePreferredRefreshRate(33, listOf(60f, 90f, 144f)))
        assertNull(resolvePreferredRefreshRate(30, emptyList()))
    }

    @Test
    fun olderAndroidUsesTheExactReportedModeValue() {
        assertEquals(119.8f, resolvePreferredRefreshRate(33, listOf(119.8f))!!, 0f)
        assertEquals(119.88f, resolvePreferredRefreshRate(33, listOf(119.88f))!!, 0f)
        assertNull(resolvePreferredRefreshRate(33, listOf(119.4f)))
    }

    @Test
    fun ignoresInvalidDisplayModes() {
        assertEquals(120f, resolvePreferredRefreshRate(33, listOf(Float.NaN, 120f, Float.POSITIVE_INFINITY))!!, 0f)
        assertNull(resolvePreferredRefreshRate(33, listOf(Float.NaN, 60f)))
    }

    @Test
    fun rejectsInvalidTargets() {
        assertTrue(runCatching {
            resolvePreferredRefreshRate(34, emptyList(), Float.NaN)
        }.isFailure)
    }
}
