package app.privateaudio

import app.privateaudio.localization.AppLanguagePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppLanguagePreferencesTest {
    @Test
    fun legacyAndroidLanguageAliasesBecomeModernLogicalTags() {
        mapOf(
            "ji" to "yi", "ji-IL" to "yi-IL",
            "iw" to "he", "iw-IL" to "he-IL",
            "in" to "id", "in-ID" to "id-ID",
            "yi" to "yi", "he" to "he", "id" to "id", "pl-PL" to "pl-PL",
            "sr-Latn" to "sr-Latn",
            "az-Arab-IR" to "az-Arab-IR",
            "zh-Hant-TW" to "zh-Hant-TW",
            "pt-BR" to "pt-BR",
        ).forEach { (input, expected) ->
            assertEquals(input, expected, AppLanguagePreferences.canonicalLanguageTag(input))
        }
    }

    @Test
    fun pickerNamesExposeIntentionalScriptAndRegionScopeWithoutVerboseGenericNames() {
        val polish = AppLanguagePreferences.pickerDisplayName(Locale.forLanguageTag("pl"), 1)
        val kurmanjiLatin = AppLanguagePreferences.pickerDisplayName(Locale.forLanguageTag("ku-Latn"), 1)
        val cantoneseHongKong = AppLanguagePreferences.pickerDisplayName(Locale.forLanguageTag("yue-Hant-HK"), 1)
        val portuguesePortugal = AppLanguagePreferences.pickerDisplayName(Locale.forLanguageTag("pt-PT"), 2)

        assertEquals(Locale.forLanguageTag("pl").getDisplayLanguage(Locale.forLanguageTag("pl")).titlecased(Locale.forLanguageTag("pl")), polish)
        assertFalse(polish.contains('('))
        assertTrue(kurmanjiLatin.contains(Locale.forLanguageTag("ku-Latn").getDisplayScript(Locale.forLanguageTag("ku-Latn"))))
        assertEquals(Locale.forLanguageTag("yue-Hant-HK").getDisplayName(Locale.forLanguageTag("yue-Hant-HK")).titlecased(Locale.forLanguageTag("yue-Hant-HK")), cantoneseHongKong)
        assertEquals(Locale.forLanguageTag("pt-PT").getDisplayName(Locale.forLanguageTag("pt-PT")).titlecased(Locale.forLanguageTag("pt-PT")), portuguesePortugal)
    }

    private fun String.titlecased(locale: Locale): String = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(locale) else it.toString()
    }
}
