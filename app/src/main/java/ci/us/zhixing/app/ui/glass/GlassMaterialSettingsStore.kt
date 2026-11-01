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

/**
 * Defaults for the production home navigation material.
 *
 * Kept separate from GlassMaterialState's model defaults because the Glass Lab
 * preview has its own historical default transparency, while the home capsule
 * was introduced with a 68% transparency setting.
 */
internal val DEFAULT_HOME_GLASS_MATERIAL = GlassMaterialState(
    transparencyPercent = 68,
    blurRadiusDp = 12f,
    enabled = true,
)

/**
 * Persists the production glass material values using Preferences DataStore.
 *
 * The repository is deliberately small: callers observe [material], save a
 * complete validated state, or remove the stored keys to restore defaults.
 * Slider drags should update Compose preview state continuously and call
 * [save] when the user releases the slider, avoiding a disk write for every
 * pointer movement.
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
        }
    }

    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.remove(TransparencyPercentKey)
            preferences.remove(BlurRadiusDpKey)
            preferences.remove(GlassEnabledKey)
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

    return GlassMaterialState(
        transparencyPercent = transparency,
        blurRadiusDp = blur,
        enabled = enabled,
    )
}
