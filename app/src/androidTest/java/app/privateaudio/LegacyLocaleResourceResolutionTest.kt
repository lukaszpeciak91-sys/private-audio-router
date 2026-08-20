package app.privateaudio

import android.app.LocaleConfig
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LegacyLocaleResourceResolutionTest {
    @Test
    fun modernLocaleTagsResolveLegacyQualifiedResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertLocalizedSettings(context, modernTag = "id", expected = "Setelan")
        assertLocalizedSettings(context, modernTag = "he", expected = "הגדרות")
        assertLocalizedSettings(context, modernTag = "yi", expected = "סעטינגס")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val discoveredTags = LocaleConfig(context).supportedLocales
                ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
                .orEmpty()
            assertEquals(true, discoveredTags.containsAll(listOf("id", "he", "yi")))
        }
    }

    private fun assertLocalizedSettings(context: Context, modernTag: String, expected: String) {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(modernTag))
        }
        val resolved = context.createConfigurationContext(configuration).getString(R.string.settings)

        assertEquals(expected, resolved)
        assertNotEquals("Settings", resolved)
    }
}
