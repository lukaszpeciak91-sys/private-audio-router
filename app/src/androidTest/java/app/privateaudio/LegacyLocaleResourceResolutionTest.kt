package app.privateaudio

import android.app.LocaleConfig
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
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
        assertLocalizedSettings(context, modernTag = "pa-Guru-IN", expected = "ਸੈਟਿੰਗਾਂ")
        assertLocalizedSettings(context, modernTag = "pa-Arab-PK", expected = "سیٹنگاں")

        assertEquals(
            android.view.View.LAYOUT_DIRECTION_LTR,
            localizedContext(context, "pa-Guru-IN").resources.configuration.layoutDirection,
        )
        assertEquals(
            android.view.View.LAYOUT_DIRECTION_RTL,
            localizedContext(context, "pa-Arab-PK").resources.configuration.layoutDirection,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val discoveredTags = LocaleConfig(context).supportedLocales
                ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
                .orEmpty()
            assertTrue(discoveredTags.containsAll(listOf("id", "he", "yi", "pa-Guru-IN", "pa-Arab-PK")))
        }
    }

    private fun assertLocalizedSettings(context: Context, modernTag: String, expected: String) {
        val resolved = localizedContext(context, modernTag).getString(R.string.settings)

        assertEquals(expected, resolved)
        assertNotEquals("Settings", resolved)
    }

    private fun localizedContext(context: Context, modernTag: String): Context {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(modernTag))
        }
        return context.createConfigurationContext(configuration)
    }
}
