package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class IgboLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val strings = projectFile("app/src/main/res/values-ig/strings.xml").readText()

    @Test
    fun igboLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("ig")
        assertEquals("ig", locale.toLanguageTag())
        assertEquals("ig", locale.language)
        assertEquals("Igbo", locale.getDisplayLanguage(locale))

        assertTrue(projectFile("app/src/main/res/values-ig/strings.xml").isFile)
        listOf("values-ig-rNG", "values-b+ig+Latn", "values-b+ig+Latn+NG").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("Dị njikere", resourceValue("state_ready"))
        assertEquals("Na-eche", resourceValue("state_waiting"))
        assertEquals("Na-arụ ọrụ", resourceValue("state_active"))
        assertEquals("Njehie", resourceValue("state_error"))
        assertFalse(resourceValue("state_active") == resourceValue("diagnostics_on"))

        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("Ntọala", resourceValue("settings"))
        assertEquals("Nke ndabara", resourceValue("settings_system_default"))
        assertEquals("Nke dị elu", resourceValue("settings_advanced"))
        assertEquals("Amụma Nzuzo", resourceValue("settings_privacy_policy"))
        assertEquals("Mmepụta ọdịyo", resourceValue("diagnostics_audio_route"))
        assertEquals("Agbanyere", resourceValue("diagnostics_on"))
        assertEquals("Agbanyụrụ", resourceValue("diagnostics_off"))

        assertTrue(resourceValue("settings_about_body").contains("ọdịyo olu akwadoro"))
        assertTrue(resourceValue("settings_about_body").contains("igwe nnata ụda dị n’ime ekwentị"))
        assertFalse(resourceValue("settings_about_body").contains("oku"))
        assertEquals("Igwe nnata ụda ntị", resourceValue("diagnostics_route_earpiece"))
        assertEquals("Spika", resourceValue("diagnostics_route_speaker"))
        assertFalse(resourceValue("diagnostics_route_earpiece") == resourceValue("diagnostics_route_speaker"))

        assertTrue(strings.contains("anaghị anakọta, edekọ, ma ọ bụ zipụ"))
        assertTrue(strings.contains("Ngwa ahụ anaghị enweta ọdịnaya mkparịta ụka gị"))
        assertTrue(strings.contains("ọrụ na-akọ mgbe ngwa kwụsịrị na mberede"))
        assertTrue(strings.contains("naanị mgbe ị họrọ ichekwa ya"))
        assertFalse(projectFile("app/src/main/res/values-ig/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
