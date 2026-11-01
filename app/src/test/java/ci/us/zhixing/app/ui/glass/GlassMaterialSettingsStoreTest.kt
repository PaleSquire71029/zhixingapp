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
    fun savePersistsAllFiveMaterialProperties() = runBlocking {
        val store = createStore()
        val expected = GlassMaterialState(
            transparencyPercent = 43,
            blurRadiusDp = 17.5f,
            enabled = false,
            refractionEnabled = false,
            refractionStrengthDp = 6.5f,
        )

        store.save(expected)

        assertEquals(expected, store.material.first())
    }

    @Test
    fun resetRemovesStoredValuesAndRestoresAllDefaults() = runBlocking {
        val store = createStore()
        store.save(
            GlassMaterialState(
                transparencyPercent = 21,
                blurRadiusDp = 4f,
                enabled = false,
                refractionEnabled = false,
                refractionStrengthDp = 7f,
            ),
        )

        store.resetToDefaults()

        assertEquals(DEFAULT_HOME_GLASS_MATERIAL, store.material.first())
    }

    @Test
    fun materialPropertiesRemainIndependent() = runBlocking {
        val store = createStore()
        val expected = GlassMaterialState(
            transparencyPercent = 34,
            blurRadiusDp = 0f,
            enabled = true,
            refractionEnabled = false,
            refractionStrengthDp = 0f,
        )

        store.save(expected)

        assertEquals(expected, store.material.first())
    }

    @Test
    fun savedRefractionStrengthRoundTripsWithoutPrecisionLoss() = runBlocking {
        val store = createStore()
        store.save(
            GlassMaterialState(
                transparencyPercent = 51,
                blurRadiusDp = 9f,
                enabled = true,
                refractionEnabled = true,
                refractionStrengthDp = 1.2f,
            ),
        )

        assertEquals(1.2f, store.material.first().refractionStrengthDp, 0f)
    }
}
