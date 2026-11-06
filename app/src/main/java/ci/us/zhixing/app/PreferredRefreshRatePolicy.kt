package ci.us.zhixing.app

import kotlin.math.abs

/**
 * Returns the refresh rate that can safely be requested from this platform.
 *
 * Android 14 (API 34) accepts an arbitrary preferred window refresh rate.
 * Older releases require the requested value to be one of the display's
 * advertised modes, so return the actual mode value rather than the target.
 */
internal fun resolvePreferredRefreshRate(
    apiLevel: Int,
    supportedRefreshRates: Iterable<Float>,
    targetHz: Float = 120f,
): Float? {
    require(targetHz.isFinite() && targetHz > 0f) {
        "Target refresh rate must be a positive finite value."
    }
    if (apiLevel >= 34) return targetHz
    return supportedRefreshRates
        .filter { it.isFinite() }
        .minByOrNull { rate -> abs(rate - targetHz) }
        ?.takeIf { rate -> abs(rate - targetHz) < 0.5f }
}
