package ci.us.zhixing.app.ui.glass

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GlassMaterialSettingsStoreTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun createStore(): GlassMaterialSettingsStore {
        val settingsFile = File(temporaryFolder.root, "glass-material.preferences_pb")
        return GlassMaterialSettingsStore(
            PreferenceDataStoreFactory.create(
                produceFile = { settingsFile },
            ),
        )
    }

    @Test
    fun missingValuesResolveToHomeDefaults() = runBlocking {
        val store = createStore()

        assertEquals(DEFAULT_HOME_GLASS_MATERIAL, store.material.first())
    }

    @Test
    fun saveWritesTransparencyBlurAndEnabledTogether() = runBlocking {
        val store = createStore()
        val expected = GlassMaterialState(
            transparencyPercent = 43,
            blurRadiusDp = 17.5f,
            enabled = false,
        )

        store.save(expected)

        assertEquals(expected, store.material.first())
    }

    @Test
    fun resetRemovesStoredValuesAndRestoresDefaults() = runBlocking {
        val store = createStore()
        store.save(
            GlassMaterialState(
                transparencyPercent = 21,
                blurRadiusDp = 4f,
                enabled = false,
            ),
        )

        store.resetToDefaults()

        assertEquals(DEFAULT_HOME_GLASS_MATERIAL, store.material.first())
    }

    @Test
    fun eachSavedValueIsIndependent() = runBlocking {
        val store = createStore()
        store.save(
            GlassMaterialState(
                transparencyPercent = 34,
                blurRadiusDp = 0f,
                enabled = true,
            ),
        )

        val state = store.material.first()

        assertEquals(34, state.transparencyPercent)
        assertEquals(0f, state.blurRadiusDp, 0f)
        assertEquals(true, state.enabled)
    }
}
