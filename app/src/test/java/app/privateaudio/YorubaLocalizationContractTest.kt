package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class YorubaLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val strings = projectFile("app/src/main/res/values-yo/strings.xml").readText()

    @Test
    fun yorubaLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("yo")
        assertEquals("yo", locale.toLanguageTag())
        assertEquals("yo", locale.language)
        assertEquals("Yorùbá", locale.getDisplayLanguage(locale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(locale) else first.toString()
        })

        assertTrue(projectFile("app/src/main/res/values-yo/strings.xml").isFile)
        listOf("values-yo-rNG", "values-b+yo+Latn", "values-b+yo+Latn+NG").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("Ṣetán", resourceValue("state_ready"))
        assertEquals("N dúró", resourceValue("state_waiting"))
        assertEquals("N ṣiṣẹ́ lọ́wọ́", resourceValue("state_active"))
        assertEquals("Àṣìṣe", resourceValue("state_error"))
        assertEquals("Private Audio ti wa ni titan", resourceValue("routing_notification_title"))
        assertFalse(resourceValue("state_active") == resourceValue("diagnostics_on"))

        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertTrue(resourceValue("settings_about_body").contains("agbohùnsókè etí inú foonu"))
        assertEquals("Agbohùnsókè", resourceValue("diagnostics_route_speaker"))
        assertTrue(strings.contains("Kò béèrè àṣẹ láti lo gbohùngbohùn"))
        assertTrue(strings.contains("nígbà tí o bá yàn láti fi í pamọ́ nìkan"))
        assertFalse(projectFile("app/src/main/res/values-yo/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
