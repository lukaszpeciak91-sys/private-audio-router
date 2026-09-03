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

        val privacy = resourceValue("settings_privacy_policy_body")
        listOf(
            "achọghị akaụntụ ma ọ bụ nbanye", "ọ naghịkwa arịọ ikike igwe okwu", "Ọ naghị ejide ma ọ bụ dekọọ ọdịyo igwe okwu",
            "dekọọ ma ọ bụ chekwaa mkparịta ụka unu ma ọ bụ ọdịnaya ọdịyo ha", "Metadata nke oge ndekọ bụ ozi teknụzụ gbasara usoro ọdịyo",
            "adịghị ejide ọdịyo igwe okwu metụtara oge ndị ahụ", "na-emepụta ma na-ahazi ozi nchọpụta nsogbu n’ime ngwaọrụ gị", "A naghị echekwa ma ọ bụ zipu akụkọ n’onwe ya",
            "naanị mgbe ị họrọpụtara kpọmkwem Chekwaa akụkọ nchọpụta nsogbu", "Ụdị dị ugbu a anaghị arịọ ikike Internet nke Android", "enweghị backend ma ọ bụ ụzọ nnyefe netwọk nke Private Audio",
            "SDK maka analytics, mgbasa ozi, ma ọ bụ ịkọ mmebi ngwa", "Ọ naghị eziga ugbu a akụkọ nchọpụta nsogbu n’aka onye mmepe ma ọ bụ na sava Private Audio",
            "nkwado ndabere igwe ojii Android na mbufe site n’otu ngwaọrụ gaa n’ọzọ",
        ).forEach { guard -> assertTrue("Missing privacy guard: $guard", privacy.contains(guard)) }
        assertFalse(projectFile("app/src/main/res/values-ig/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
