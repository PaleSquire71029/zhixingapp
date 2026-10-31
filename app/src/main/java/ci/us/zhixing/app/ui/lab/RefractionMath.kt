package ci.us.zhixing.app.ui.lab

internal fun clampRefractionStrength(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0f, 8f) else 0f
