package ci.us.zhixing.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationTest {
    @Test
    fun mainNavigationContainsFourUniqueDestinations() {
        val destinations = AppDestinations.all

        assertEquals(4, destinations.size)
        assertEquals(
            listOf("calculator", "ai", "tools", "history"),
            destinations.map { it.route },
        )
        assertEquals(destinations.size, destinations.map { it.route }.toSet().size)
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
