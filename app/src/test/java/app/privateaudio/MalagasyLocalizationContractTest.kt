package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class MalagasyLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val strings = projectFile("app/src/main/res/values-mg/strings.xml").readText()

    @Test
    fun malagasyLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("mg")
        assertEquals("mg", locale.toLanguageTag())
        assertEquals("mg", locale.language)
        assertEquals("Malagasy", locale.getDisplayName(locale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(locale) else first.toString()
        })

        assertTrue(projectFile("app/src/main/res/values-mg/strings.xml").isFile)
        listOf("values-mg-rMG", "values-b+mg+Latn", "values-b+mg+Latn+MG").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("Vonona", resourceValue("state_ready"))
        assertEquals("Miandry", resourceValue("state_waiting"))
        assertEquals("Mavitrika", resourceValue("state_active"))
        assertEquals("Hadisoana", resourceValue("state_error"))
        assertEquals("Mandeha ny Private Audio", resourceValue("routing_notification_title"))
        assertFalse(resourceValue("state_active") == "Mandeha")

        assertEquals("Politika momba ny fiainana manokana", resourceValue("settings_privacy_policy"))
        assertFalse(resourceValue("settings_about_body").contains("feon’ny antso", ignoreCase = true))
        assertTrue(resourceValue("settings_about_body").contains("feo tohanana"))
        assertTrue(resourceValue("settings_about_body").contains("fanamafisam-peo anatiny eo amin’ny sofin’ny telefaoninao"))
        assertTrue(strings.contains("name=\"diagnostics_route_speaker\">Fanamafisam-peo</string>"))
        val privacy = resourceValue("settings_privacy_policy_body")
        listOf(
            "tsy mitaky kaonty na fidirana anaty kaonty", "tsy mangataka alalana amin’ny mikrô", "Tsy misambotra na mandrakitra feo avy amin’ny mikrô",
            "tsy mandrakitra na mitahiry ny resakao na ny votoatin’ny feony", "metadata recording-session dia fampahalalana ara-teknika momba ny rafitra feo",
            "tsy misambotra ny feon’ny mikrô mifandraika amin’ireo session ireo", "vokarina sy karakaraina ao amin’ny fitaovanao", "Ny tatitra dia tsy voatahiry na alefa ho azy",
            "rehefa misafidy mazava tsara ny Hitehirizana ny tatitra fitiliana ianao", "kinova amin’izao fotoana izao dia tsy mangataka ny alalana Internet an’ny Android",
            "tsy manana backend an’i Private Audio na lalan’ny fandefasana amin’ny tambajotra", "tsy ahitana serivisy na SDK ho an’ny analytics, doka, na tatitra momba ny crash",
            "Tsy mandefa tatitra fitiliana any amin’ny mpamorona na amin’ny mpizara Private Audio", "cloud backup sy ny famindrana avy amin’ny fitaovana iray mankany amin’ny fitaovana iray hafa",
        ).forEach { guard -> assertTrue("Missing privacy guard: $guard", privacy.contains(guard)) }
        assertFalse(projectFile("app/src/main/res/values-mg/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
