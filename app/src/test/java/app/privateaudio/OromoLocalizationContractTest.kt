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
        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))

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
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        val paragraphs = privacy.split("\\n\\n")
        assertEquals(5, paragraphs.size)
        listOf("herrega yookiin seenuu hin barbaadu", "Maayikiroofonii argachuuf hayyama hin gaafatu", "Sagalee maayikiroofonii hin qabatu yookiin hin waraabu", "haasawa kee yookiin qabiyyee sagalee isaa hin waraabu yookiin hin kuusu")
            .forEach { guard -> assertTrue("Missing microphone/conversation guard: $guard", paragraphs[0].contains(guard)) }
        listOf("API Android uummataaf banaa ta\\'een", "meetadaataa taphachiisuu fi meetadaataa galmee sagalee", "sagalee maayikiroofonii galmee sanaan walqabatu hin qabatu")
            .forEach { guard -> assertTrue("Missing public-metadata guard: $guard", paragraphs[1].contains(guard)) }
        listOf("kuusaa dhuunfaa appii Private Audio", "meeshaa kee irratti uumamee adeemsifama", "Gabaasni ofumaan hin kuufamu yookiin hin ergamu", "karaa Android bakka itti kuufamu filattu qofa", "Private Audio miti")
            .forEach { guard -> assertTrue("Missing local/export guard: $guard", paragraphs[2].contains(guard)) }
        listOf("hayyama Intarneetii Android hin gaafatu", "backend Private Audio yookiin karaa dabarsa networkii hin qabu", "kuusaa qorannoo gama sarvarii hin qabu")
            .forEach { guard -> assertTrue("Missing network guard: $guard", paragraphs[3].contains(guard)) }
        listOf("Kuusni duubaa duumessaa fi dabarsaan meeshaa irraa gara meeshaatti", "haalli qorannoo yeroo gabaabaa gabaasa kuufame miti", "bakka yookiin tajaajila ati filattee")
            .forEach { guard -> assertTrue("Missing retention guard: $guard", paragraphs[4].contains(guard)) }
        assertFalse(privacy.contains("gosa Android", ignoreCase = true))

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("fudhatama hin arganne"))
        listOf("Android", "sirni", "sirnichi", "bilbilichi", "Private Audio").forEach {
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
