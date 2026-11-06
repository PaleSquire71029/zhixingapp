package ci.us.zhixing.app

import kotlin.math.abs

/**
 * Android 14 (API 34) accepts arbitrary preferred window refresh rates.
 * Older Android versions should only be given a rate exposed by the display.
 */
internal fun shouldRequestPreferredRefreshRate(
    apiLevel: Int,
    supportedRefreshRates: Iterable<Float>,
    targetHz: Float = 120f,
): Boolean {
    require(targetHz.isFinite() && targetHz > 0f) {
        "Target refresh rate must be a positive finite value."
    }
    if (apiLevel >= 34) return true
    return supportedRefreshRates.any { rate ->
        rate.isFinite() && abs(rate - targetHz) < 0.5f
    }
}
