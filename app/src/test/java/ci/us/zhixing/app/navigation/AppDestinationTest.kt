package ci.us.zhixing.app.navigation

import org.junit.Assert.assertEquals
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

}
