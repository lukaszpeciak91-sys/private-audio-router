package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class SundaneseLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val strings = projectFile("app/src/main/res/values-su/strings.xml").readText()

    @Test
    fun sundaneseLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("su")
        assertEquals("su", locale.toLanguageTag())
        assertEquals("su", locale.language)
        assertEquals("Basa Sunda", locale.getDisplayLanguage(locale))

        assertTrue(projectFile("app/src/main/res/values-su/strings.xml").isFile)
        listOf("values-su-rID", "values-b+su+Latn", "values-b+su+Latn+ID").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("Siap", resourceValue("state_ready"))
        assertEquals("Ngadagoan", resourceValue("state_waiting"))
        assertEquals("Aktip", resourceValue("state_active"))
        assertEquals("Kasalahan", resourceValue("state_error"))
        assertEquals("Hurung", resourceValue("diagnostics_on"))
        assertEquals("Pareum", resourceValue("diagnostics_off"))
        assertFalse(resourceValue("state_active") == resourceValue("diagnostics_on"))

        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("Mini", resourceValue("floating"))
        assertEquals("Kaluaran audio", resourceValue("diagnostics_audio_route"))
        assertEquals("Pangarahan audio", resourceValue("diagnostics_routing"))
        assertEquals("Spéker ceuli bawaan telepon", resourceValue("diagnostics_route_earpiece"))
        assertEquals("Spéker", resourceValue("diagnostics_route_speaker"))
        assertFalse(resourceValue("diagnostics_route_earpiece") == resourceValue("diagnostics_route_speaker"))

        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, privacy.windowed(4).count { it == "\\n\\n" })
        assertTrue(privacy.contains("henteu ngumpulkeun, ngarékam, atawa ngirimkeun"))
        assertTrue(privacy.contains("henteu ménta aksés mikropon"))
        assertTrue(privacy.contains("henteu ménta idin Internét Android"))
        assertTrue(privacy.contains("ngan niténan kaayaan téknis jeung metadata"))
        assertTrue(privacy.contains("ngan disimpen lamun anjeun milih"))
        assertTrue(privacy.contains("Nyadangkeun data aplikasi Android ditumpurkeun"))

        assertFalse(strings.contains("settings_assistant_early_route"))
        assertFalse(projectFile("app/src/main/res/values-su/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
