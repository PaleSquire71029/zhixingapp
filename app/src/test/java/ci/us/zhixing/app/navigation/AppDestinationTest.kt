package ci.us.zhixing.app.navigation

import org.junit.Assert.assertEquals
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
}
