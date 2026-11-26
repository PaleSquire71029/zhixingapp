package ci.us.zhixing.app.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable

/**
 * Owns root-tab and secondary-page navigation state for the Zhixing Flow shell.
 *
 * The two Saveable states intentionally remain separate and keep their legacy
 * Savers, preserving the previous saved-instance-state values and slot order.
 */
@Stable
internal class AppNavigationState internal constructor(
    private val rootRouteState: MutableState<AppRootRoute>,
    private val secondaryRouteState: MutableState<AppSecondaryRoute?>,
) {
    val rootRoute: AppRootRoute
        get() = rootRouteState.value

    val secondaryRoute: AppSecondaryRoute?
        get() = secondaryRouteState.value

    val selectedRootIndex: Int
        get() = rootRoute.destinationIndex

    val canNavigateBack: Boolean
        get() = secondaryRoute != null

    fun selectRoot(index: Int) {
        rootRouteState.value = AppRootRoute.fromIndex(index)
        secondaryRouteState.value = null
    }

    fun openSecondary(route: AppSecondaryRoute) {
        secondaryRouteState.value = route
    }

    fun closeSecondary() {
        secondaryRouteState.value = null
    }

    /** Returns true only when a secondary page was actually closed. */
    fun navigateBack(): Boolean {
        if (!canNavigateBack) return false
        closeSecondary()
        return true
    }
}
