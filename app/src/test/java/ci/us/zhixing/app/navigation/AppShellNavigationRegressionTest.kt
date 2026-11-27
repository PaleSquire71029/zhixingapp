package ci.us.zhixing.app.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellNavigationRegressionTest {
    private val saverScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun everyRootRouteRoundTripsThroughTheLegacyIntegerSaver() {
        AppRootRoute.values().forEach { route ->
            val savedIndex = with(AppRootRoute.saver) { with(saverScope) { save(route) } }

            assertEquals(route.destinationIndex, savedIndex)
            assertEquals(route, AppRootRoute.saver.restore(checkNotNull(savedIndex)))
        }
    }

    @Test
    fun everySecondaryRouteRoundTripsThroughItsLegacyStringSaver() {
        AppSecondaryRoute.values().forEach { route ->
            val savedRoute = with(AppSecondaryRoute.saver) { with(saverScope) { save(route) } }

            assertEquals(route.route, savedRoute)
            assertEquals(route, AppSecondaryRoute.saver.restore(checkNotNull(savedRoute)))
        }

        val savedNull = with(AppSecondaryRoute.saver) { with(saverScope) { save(null) } }
        assertEquals("", savedNull)
        assertNull(AppSecondaryRoute.saver.restore(checkNotNull(savedNull)))
    }

    @Test
    fun selectingEachOfTheFourRootTabsUpdatesDockIndexAndClosesSecondaryPage() {
        val state = AppNavigationState(
            rootRouteState = mutableStateOf(AppRootRoute.CALCULATOR),
            secondaryRouteState = mutableStateOf(null),
        )

        AppRootRoute.values().forEach { route ->
            state.openSecondary(AppSecondaryRoute.HISTORY)
            assertTrue(state.canNavigateBack)

            state.selectRoot(route.destinationIndex)

            assertEquals(route, state.rootRoute)
            assertEquals(route.destinationIndex, state.selectedRootIndex)
            assertNull(state.secondaryRoute)
            assertFalse(state.canNavigateBack)
        }
    }

    @Test
    fun everySecondaryRouteReturnsToTheSameRootRoute() {
        AppRootRoute.values().forEach { root ->
            AppSecondaryRoute.values().forEach { secondary ->
                val state = AppNavigationState(
                    rootRouteState = mutableStateOf(root),
                    secondaryRouteState = mutableStateOf(null),
                )

                state.openSecondary(secondary)

                assertTrue(state.navigateBack())
                assertEquals(root, state.rootRoute)
                assertEquals(root.destinationIndex, state.selectedRootIndex)
                assertNull(state.secondaryRoute)
                assertFalse(state.navigateBack())
            }
        }
    }

    @Test
    fun invalidRestoredRootIndicesStillFallBackToCalculator() {
        assertEquals(AppRootRoute.CALCULATOR, AppRootRoute.saver.restore(-1))
        assertEquals(AppRootRoute.CALCULATOR, AppRootRoute.saver.restore(99))
    }
}
