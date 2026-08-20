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
        assertLocalizedSettings(context, modernTag = "ml", expected = "ക്രമീകരണം")
        listOf("am", "am-ET").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ቅንብሮች")
        }
        assertLocalizedSettings(context, modernTag = "pa-Guru-IN", expected = "ਸੈਟਿੰਗਾਂ")
        assertLocalizedSettings(context, modernTag = "pa-Arab-PK", expected = "سیٹنگاں")
        listOf("ps", "ps-AF", "ps-PK").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "امستنې")
        }
        listOf("ha", "ha-NG", "ha-NE", "ha-GH").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Saituna")
        }
        listOf("zu", "zu-ZA").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Izilungiselelo")
        }
        listOf("so", "so-SO", "so-DJ", "so-ET", "so-KE").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Dejinta")
        }
        listOf("ne", "ne-NP", "ne-Deva-NP").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "सेटिङहरू")
        }
        listOf("or", "or-IN", "or-Orya-IN").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ସେଟିଂସ")
        }
        listOf("my", "my-MM", "my-Mymr-MM").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ဆက်တင်များ")
        }
        listOf("uz", "uz-UZ", "uz-Latn-UZ").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Sozlamalar")
        }
        listOf("km", "km-KH", "km-Khmr-KH").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ការកំណត់")
        listOf("as", "as-IN", "as-Beng-IN").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ছেটিং")
        }

        assertEquals(
            android.view.View.LAYOUT_DIRECTION_LTR,
            localizedContext(context, "ml").resources.configuration.layoutDirection,
        )
        listOf("am", "am-ET").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        assertEquals(
            android.view.View.LAYOUT_DIRECTION_LTR,
            localizedContext(context, "pa-Guru-IN").resources.configuration.layoutDirection,
        )
        assertEquals(
            android.view.View.LAYOUT_DIRECTION_RTL,
            localizedContext(context, "pa-Arab-PK").resources.configuration.layoutDirection,
        )
        listOf("ps", "ps-AF", "ps-PK").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_RTL,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("ha", "ha-NG", "ha-NE", "ha-GH").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("zu", "zu-ZA").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("so", "so-SO", "so-DJ", "so-ET", "so-KE").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("ne", "ne-NP", "ne-Deva-NP").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("or", "or-IN", "or-Orya-IN").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("my", "my-MM", "my-Mymr-MM").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("uz", "uz-UZ", "uz-Latn-UZ").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("km", "km-KH", "km-Khmr-KH").forEach { tag ->
        listOf("as", "as-IN", "as-Beng-IN").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val discoveredTags = LocaleConfig(context).supportedLocales
                ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
                .orEmpty()
            assertTrue(discoveredTags.containsAll(listOf("id", "he", "yi", "ml", "pa-Guru-IN", "pa-Arab-PK", "ps", "ha", "am", "zu", "so", "ne", "or", "my", "uz", "km")))
            assertTrue(discoveredTags.containsAll(listOf("id", "he", "yi", "ml", "pa-Guru-IN", "pa-Arab-PK", "ps", "ha", "am", "zu", "so", "ne", "or", "my", "uz", "as")))
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
