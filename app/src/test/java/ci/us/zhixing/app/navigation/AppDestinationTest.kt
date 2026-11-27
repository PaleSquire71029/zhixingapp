package ci.us.zhixing.app.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationTest {
    @Test
    fun mainNavigationContainsFourUniqueDestinationsInConfirmedOrder() {
        val destinations = AppDestinations.all

        assertEquals(4, destinations.size)
        assertEquals(
            listOf("calculator", "ai", "tools", "my"),
            destinations.map { it.route },
        )
        assertEquals(
            listOf("计算", "知行 AI", "工具", "我的"),
            destinations.map { it.label },
        )
        assertEquals(destinations.size, destinations.map { it.route }.toSet().size)
    }

    @Test
    fun primaryDestinationsUseTheirMatchingGeometricIcons() {
        assertEquals(
            listOf(
                AppNavigationIcon.CALCULATOR,
                AppNavigationIcon.AI,
                AppNavigationIcon.TOOLS,
                AppNavigationIcon.ME,
            ),
            AppDestinations.all.map { it.icon },
        )
    }

    @Test
    fun everyDestinationHasVisibleChineseLabelAndDescription() {
        AppDestinations.all.forEach { destination ->
            assertTrue(destination.label.isNotBlank())
            assertTrue(destination.title.isNotBlank())
            assertTrue(destination.description.isNotBlank())
        }
    }
    @Test
    fun typedRootRoutesMatchLegacyTabOrderAndRouteNames() {
        val routes = AppRootRoute.values().sortedBy { it.destinationIndex }

        assertEquals(
            listOf("calculator", "ai", "tools", "my"),
            routes.map { it.route },
        )
        assertEquals(listOf(0, 1, 2, 3), routes.map { it.destinationIndex })
        assertEquals(AppRootRoute.CALCULATOR, AppRootRoute.fromIndex(0))
        assertEquals(AppRootRoute.MY, AppRootRoute.fromIndex(3))
    }

    @Test
    fun typedSecondaryRoutesKeepLegacyRouteAndSaveableKeys() {
        assertEquals("my-material", AppSecondaryRoute.MATERIAL.saveableStateKey)
        assertEquals("my-history", AppSecondaryRoute.HISTORY.saveableStateKey)
        assertEquals("unit-converter", AppSecondaryRoute.UNIT_CONVERTER.saveableStateKey)

        assertEquals(AppSecondaryRoute.MATERIAL, AppSecondaryRoute.fromRoute("material"))
        assertEquals(AppSecondaryRoute.HISTORY, AppSecondaryRoute.fromRoute("history"))
        assertEquals(
            AppSecondaryRoute.UNIT_CONVERTER,
            AppSecondaryRoute.fromRoute("unit-converter"),
        )
        assertNull(AppSecondaryRoute.fromRoute("unknown"))
    }

    @Test
    fun invalidLegacyTabIndexFallsBackToCalculator() {
        assertEquals(AppRootRoute.CALCULATOR, AppRootRoute.fromIndex(-1))
        assertEquals(AppRootRoute.CALCULATOR, AppRootRoute.fromIndex(99))
    }

    @Test
    fun appNavigationStateOpensSecondaryAndBackReturnsToCurrentRoot() {
        val state = AppNavigationState(
            rootRouteState = mutableStateOf(AppRootRoute.TOOLS),
            secondaryRouteState = mutableStateOf(null),
        )

        state.openSecondary(AppSecondaryRoute.UNIT_CONVERTER)
        assertTrue(state.canNavigateBack)
        assertEquals(AppRootRoute.TOOLS, state.rootRoute)
        assertEquals(AppSecondaryRoute.UNIT_CONVERTER, state.secondaryRoute)

        assertTrue(state.navigateBack())
        assertFalse(state.canNavigateBack)
        assertEquals(AppRootRoute.TOOLS, state.rootRoute)
        assertEquals(null, state.secondaryRoute)
    }

    @Test
    fun selectingRootTabClosesSecondaryAndUpdatesDockIndex() {
        val state = AppNavigationState(
            rootRouteState = mutableStateOf(AppRootRoute.CALCULATOR),
            secondaryRouteState = mutableStateOf(AppSecondaryRoute.MATERIAL),
        )

        state.selectRoot(AppRootRoute.AI.destinationIndex)

        assertEquals(AppRootRoute.AI, state.rootRoute)
        assertEquals(1, state.selectedRootIndex)
        assertEquals(null, state.secondaryRoute)
        assertFalse(state.canNavigateBack)
    }

    @Test
    fun backWithoutSecondaryPageDoesNothing() {
        val state = AppNavigationState(
            rootRouteState = mutableStateOf(AppRootRoute.CALCULATOR),
            secondaryRouteState = mutableStateOf(null),
        )

        assertFalse(state.navigateBack())
        assertEquals(AppRootRoute.CALCULATOR, state.rootRoute)
    }

    @Test
    fun everyRootTabCanBeSelectedAndClosesAnyOpenSecondaryPage() {
        val state = AppNavigationState(
            rootRouteState = mutableStateOf(AppRootRoute.CALCULATOR),
            secondaryRouteState = mutableStateOf(null),
        )

        AppRootRoute.values().forEach { destination ->
            state.openSecondary(AppSecondaryRoute.HISTORY)
            state.selectRoot(destination.destinationIndex)

            assertEquals(destination, state.rootRoute)
            assertEquals(destination.destinationIndex, state.selectedRootIndex)
            assertNull(state.secondaryRoute)
            assertFalse(state.canNavigateBack)
        }
    }

    @Test
    fun everySecondaryRouteCanOpenAndReturnToTheSameRootTab() {
        AppSecondaryRoute.values().forEach { destination ->
            val state = AppNavigationState(
                rootRouteState = mutableStateOf(AppRootRoute.MY),
                secondaryRouteState = mutableStateOf(null),
            )

            state.openSecondary(destination)
            assertEquals(destination, state.secondaryRoute)
            assertTrue(state.canNavigateBack)
            assertEquals(AppRootRoute.MY, state.rootRoute)

            assertTrue(state.navigateBack())
            assertNull(state.secondaryRoute)
            assertFalse(state.canNavigateBack)
            assertEquals(AppRootRoute.MY, state.rootRoute)
        }
    }

    @Test
    fun rootRouteSaverKeepsTheLegacyIntegerPayloadForAllTabs() {
        val scope = object : SaverScope {
            override fun canBeSaved(value: Any): Boolean = true
        }

        AppRootRoute.values().forEach { destination ->
            val payload = with(scope) { AppRootRoute.saver.run { scope.save(destination) } }

            assertEquals(destination.destinationIndex, payload)
            assertEquals(destination, AppRootRoute.saver.restore(checkNotNull(payload)))
        }
    }

    @Test
    fun secondaryRouteSaverKeepsLegacyStringsAndEmptyNullPayload() {
        val scope = object : SaverScope {
            override fun canBeSaved(value: Any): Boolean = true
        }

        AppSecondaryRoute.values().forEach { destination ->
            val payload = with(scope) { AppSecondaryRoute.saver.run { scope.save(destination) } }

            assertEquals(destination.route, payload)
            assertEquals(destination, AppSecondaryRoute.saver.restore(checkNotNull(payload)))
        }

        val emptyPayload = with(scope) { AppSecondaryRoute.saver.run { scope.save(null) } }
        assertEquals("", emptyPayload)
        assertNull(AppSecondaryRoute.saver.restore(checkNotNull(emptyPayload)))
    }

}
