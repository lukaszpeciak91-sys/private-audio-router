package app.privateaudio

import app.privateaudio.localization.AppLanguagePreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguagePreferencesTest {
    @Test
    fun legacyAndroidLanguageAliasesBecomeModernLogicalTags() {
        mapOf(
            "ji" to "yi", "ji-IL" to "yi-IL",
            "iw" to "he", "iw-IL" to "he-IL",
            "in" to "id", "in-ID" to "id-ID",
            "yi" to "yi", "he" to "he", "id" to "id", "pl-PL" to "pl-PL",
        ).forEach { (input, expected) ->
            assertEquals(input, expected, AppLanguagePreferences.canonicalLanguageTag(input))
        }
    }
}
