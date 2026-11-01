package ci.us.zhixing.app.ui.glass

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.glassMaterialDataStore by preferencesDataStore(
    name = "glass_material_settings",
)

private val TransparencyPercentKey = intPreferencesKey("transparency_percent")
private val BlurRadiusDpKey = floatPreferencesKey("blur_radius_dp")
private val GlassEnabledKey = booleanPreferencesKey("glass_enabled")
private val RefractionEnabledKey = booleanPreferencesKey("refraction_enabled")
private val RefractionStrengthDpKey = floatPreferencesKey("refraction_strength_dp")
private val ChromaticDispersionEnabledKey = booleanPreferencesKey("chromatic_dispersion_enabled")
private val EdgeHighlightEnabledKey = booleanPreferencesKey("edge_highlight_enabled")
private val EnvironmentReflectionEnabledKey = booleanPreferencesKey("environment_reflection_enabled")
private val TouchFeedbackEnabledKey = booleanPreferencesKey("touch_feedback_enabled")

/**
 * Defaults for the production home navigation material.
 *
 * The home capsule uses a slightly different transparency default from the
 * original Glass Lab preview. All nine material properties are stored
 * independently so Off mode does not erase the user's optical settings.
 */
internal val DEFAULT_HOME_GLASS_MATERIAL = GlassMaterialState(
    transparencyPercent = 68,
    blurRadiusDp = 12f,
    enabled = true,
    refractionEnabled = true,
    refractionStrengthDp = 1.8f,
    chromaticDispersionEnabled = GlassMaterialState.DEFAULT_CHROMATIC_DISPERSION_ENABLED,
    edgeHighlightEnabled = true,
    environmentReflectionEnabled = GlassMaterialState.DEFAULT_ENVIRONMENT_REFLECTION_ENABLED,
    touchFeedbackEnabled = GlassMaterialState.DEFAULT_TOUCH_FEEDBACK_ENABLED,
)

/**
 * Preferences DataStore repository for the production shared glass material.
 *
 * Slider drags should update in-memory preview state; callers persist after
 * release so pointer movement does not trigger a disk write per frame.
 */
internal class GlassMaterialSettingsStore(
    private val dataStore: DataStore<Preferences>,
) {
    constructor(context: Context) : this(context.applicationContext.glassMaterialDataStore)

    val material: Flow<GlassMaterialState> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> preferences.toGlassMaterialState() }

    suspend fun save(state: GlassMaterialState) {
        dataStore.edit { preferences ->
            preferences[TransparencyPercentKey] = state.transparencyPercent
            preferences[BlurRadiusDpKey] = state.blurRadiusDp
            preferences[GlassEnabledKey] = state.enabled
            preferences[RefractionEnabledKey] = state.refractionEnabled
            preferences[RefractionStrengthDpKey] = state.refractionStrengthDp
            preferences[ChromaticDispersionEnabledKey] = state.chromaticDispersionEnabled
            preferences[EdgeHighlightEnabledKey] = state.edgeHighlightEnabled
            preferences[EnvironmentReflectionEnabledKey] = state.environmentReflectionEnabled
            preferences[TouchFeedbackEnabledKey] = state.touchFeedbackEnabled
        }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.remove(TransparencyPercentKey)
            preferences.remove(BlurRadiusDpKey)
            preferences.remove(GlassEnabledKey)
            preferences.remove(RefractionEnabledKey)
            preferences.remove(RefractionStrengthDpKey)
            preferences.remove(ChromaticDispersionEnabledKey)
            preferences.remove(EdgeHighlightEnabledKey)
            preferences.remove(EnvironmentReflectionEnabledKey)
            preferences.remove(TouchFeedbackEnabledKey)
        }
    }
}

private fun Preferences.toGlassMaterialState(): GlassMaterialState {
    val transparency = (this[TransparencyPercentKey]
        ?: DEFAULT_HOME_GLASS_MATERIAL.transparencyPercent)
        .coerceIn(
            GlassMaterialState.MIN_TRANSPARENCY_PERCENT,
            GlassMaterialState.MAX_TRANSPARENCY_PERCENT,
        )

    val storedBlur = this[BlurRadiusDpKey]
    val blur = if (storedBlur != null && storedBlur.isFinite()) {
        storedBlur.coerceIn(
            GlassMaterialState.MIN_BLUR_RADIUS_DP,
            GlassMaterialState.MAX_BLUR_RADIUS_DP,
        )
    } else {
        DEFAULT_HOME_GLASS_MATERIAL.blurRadiusDp
    }

    val enabled = this[GlassEnabledKey] ?: DEFAULT_HOME_GLASS_MATERIAL.enabled
    val refractionEnabled = this[RefractionEnabledKey]
        ?: DEFAULT_HOME_GLASS_MATERIAL.refractionEnabled
    val storedRefractionStrength = this[RefractionStrengthDpKey]
    val refractionStrength = if (
        storedRefractionStrength != null && storedRefractionStrength.isFinite()
    ) {
        storedRefractionStrength.coerceIn(
            GlassMaterialState.MIN_REFRACTION_STRENGTH_DP,
            GlassMaterialState.MAX_REFRACTION_STRENGTH_DP,
        )
    } else {
        DEFAULT_HOME_GLASS_MATERIAL.refractionStrengthDp
    }

    val chromaticDispersionEnabled = this[ChromaticDispersionEnabledKey]
        ?: DEFAULT_HOME_GLASS_MATERIAL.chromaticDispersionEnabled
    val edgeHighlightEnabled = this[EdgeHighlightEnabledKey]
        ?: DEFAULT_HOME_GLASS_MATERIAL.edgeHighlightEnabled
    val environmentReflectionEnabled = this[EnvironmentReflectionEnabledKey]
        ?: DEFAULT_HOME_GLASS_MATERIAL.environmentReflectionEnabled
    val touchFeedbackEnabled = this[TouchFeedbackEnabledKey]
        ?: DEFAULT_HOME_GLASS_MATERIAL.touchFeedbackEnabled

    return GlassMaterialState(
        transparencyPercent = transparency,
        blurRadiusDp = blur,
        enabled = enabled,
        refractionEnabled = refractionEnabled,
        refractionStrengthDp = refractionStrength,
        chromaticDispersionEnabled = chromaticDispersionEnabled,
        edgeHighlightEnabled = edgeHighlightEnabled,
        environmentReflectionEnabled = environmentReflectionEnabled,
        touchFeedbackEnabled = touchFeedbackEnabled,
    )
}
