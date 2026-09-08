package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class OromoLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourceDirectory = projectFile("app/src/main/res/values-om")
    private val stringsFile = File(resourceDirectory, "strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun oromoLocalePreservesIdentityQualifierAndLtrDirection() {
        val locale = Locale.forLanguageTag("om")
        assertEquals("om", locale.toLanguageTag())
        assertEquals("om", locale.language)
        assertEquals("Oromoo", locale.getDisplayLanguage(locale))
        assertTrue(stringsFile.isFile)
        listOf("om-ET", "om-KE").forEach { regionalTag ->
            assertEquals("om", Locale.forLanguageTag(regionalTag).language)
        }

        val appBuild = projectFile("app/build.gradle.kts").readText()
        assertTrue(appBuild.contains("minSdk = 31"))
        assertTrue(appBuild.contains("targetSdk = 36"))
        assertTrue(appBuild.contains("generateLocaleConfig = true"))

        val competingDirectories = projectFile("app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it.name != "values-om" }
            .filter { it.name.startsWith("values-om-") || it.name.startsWith("values-b+om+") }
        assertTrue(competingDirectories.isEmpty())
        assertFalse(projectFile("app/src/main/res/values-om/mini_state_strings.xml").exists())

        assertEquals(
            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.getDirectionality(locale.getDisplayLanguage(locale).first()),
        )
    }

    @Test
    fun oromoProductStatesAndAudioOutputsRemainDistinct() {
        assertEquals("Puzru", resourceValue("app_name"))
        assertEquals("Puzru", resourceValue("product_title"))

        val runtimeStates = listOf("state_ready", "state_waiting", "state_active", "state_error")
            .map(::resourceValue)
        assertTrue(runtimeStates.all { it.isNotBlank() })
        assertEquals(runtimeStates.size, runtimeStates.toSet().size)
        assertEquals("Banaa", resourceValue("diagnostics_on"))
        assertEquals("Dhaamaa", resourceValue("diagnostics_off"))
        assertEquals("Hojii irra jira", resourceValue("state_active"))
        assertFalse(resourceValue("diagnostics_on") == resourceValue("state_active"))

        assertEquals("Dhageessisaa gurraa bilbilaa keessaa", resourceValue("diagnostics_route_earpiece"))
        assertEquals("Sagalee guddiftuu", resourceValue("diagnostics_route_speaker"))
        assertFalse(resourceValue("diagnostics_route_earpiece") == resourceValue("diagnostics_route_speaker"))
        assertTrue(resourceValue("settings_about_body").contains("bilbilaa keessaa kan gubbaa"))
        assertTrue(resourceValue("settings_about_body").contains("gurra biratti"))
        assertFalse(resourceValue("settings_about_body").contains("Sagalee guddiftuu", ignoreCase = true))

        assertEquals("Fooyya\\'aa %1\$s", resourceValue("settings_version"))
        val sessionEnded = resourceValue("diagnostics_error_session_ended")
        assertTrue(sessionEnded.startsWith("Tursi sagalee"))
        assertFalse(sessionEnded.startsWith("Kutaan sagalee"))
    }

    @Test
    fun oromoPrivacyAndRoutingClaimsRetainReviewedInvariants() {
        val privacy = resourceValue("settings_privacy_summary_body")
        assertEquals(3, privacy.split("\\n\\n").size)
        assertTrue(privacy.contains("Puzru"))
        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("fudhatama hin arganne"))
        listOf("Android", "sirni", "sirnichi", "bilbilichi", "Puzru").forEach {
            assertFalse("Rejected-request error invented actor: $it", rejected.contains(it, ignoreCase = true))
        }
        assertTrue(resourceValue("diagnostics_error_blocked_by_system").contains("Sirnichi"))

        assertFalse(strings.contains("name=\"settings_assistant_early_route\""))
        assertFalse(strings.contains("name=\"settings_assistant_early_route_description\""))
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
