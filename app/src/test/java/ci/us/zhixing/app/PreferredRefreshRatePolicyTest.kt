package ci.us.zhixing.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferredRefreshRatePolicyTest {
    @Test
    fun android14AndNewerCanRequest120EvenIfModeListDoesNotAdvertiseIt() {
        assertTrue(shouldRequestPreferredRefreshRate(34, listOf(60f, 90f)))
        assertTrue(shouldRequestPreferredRefreshRate(35, emptyList()))
    }

    @Test
    fun olderAndroidOnlyRequests120WhenDisplayReportsThatMode() {
        assertTrue(shouldRequestPreferredRefreshRate(33, listOf(60f, 120f, 144f)))
        assertFalse(shouldRequestPreferredRefreshRate(33, listOf(60f, 90f, 144f)))
        assertFalse(shouldRequestPreferredRefreshRate(30, emptyList()))
    }

    @Test
    fun near120ModeIsAcceptedWithinHalfHertzTolerance() {
        assertTrue(shouldRequestPreferredRefreshRate(33, listOf(119.8f)))
        assertFalse(shouldRequestPreferredRefreshRate(33, listOf(119.4f)))
    }
}
