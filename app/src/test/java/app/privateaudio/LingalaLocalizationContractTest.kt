package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer
import java.util.Locale

class LingalaLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourceDirectory = projectFile("app/src/main/res/values-ln")
    private val stringsFile = File(resourceDirectory, "strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun lingalaUsesOneGenericLatinLtrResourceTreeForRegionalIdentities() {
        val generic = Locale.forLanguageTag("ln")
        assertEquals("ln", generic.language)
        assertEquals("ln", generic.toLanguageTag())

        listOf("ln-CD", "ln-CG", "ln-AO", "ln-CF").forEach { tag ->
            val regional = Locale.forLanguageTag(tag)
            assertEquals(tag, regional.toLanguageTag())
            assertEquals("ln", regional.language)
        }
        val likelyIdentity = Locale.forLanguageTag("ln-Latn-CD")
        assertEquals("ln", likelyIdentity.language)
        assertEquals("Latn", likelyIdentity.script)
        assertEquals("CD", likelyIdentity.country)
        assertEquals("values-ln", resourceDirectory.name)
        assertTrue(stringsFile.isFile)

        val competingDirectories = projectFile("app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it.name != "values-ln" }
            .filter { it.name.startsWith("values-ln-") || it.name.startsWith("values-b+ln+") }
        assertTrue(competingDirectories.isEmpty())
        assertFalse(projectFile("app/src/main/res/values-ln/mini_state_strings.xml").exists())

        val appBuild = projectFile("app/build.gradle.kts").readText()
        assertTrue(appBuild.contains("generateLocaleConfig = true"))
        assertEquals(strings, Normalizer.normalize(strings, Normalizer.Form.NFC))
        assertTrue(strings.any { it == 'ɛ' })
        assertTrue(strings.any { it == 'ɔ' })
        assertFalse(Regex("[\\u0400-\\u052f\\u0590-\\u08ff]").containsMatchIn(strings))
        assertEquals(
            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.getDirectionality(generic.getDisplayLanguage(generic).first()),
        )
    }

    @Test
    fun productStatesAndProtectedAudioConceptsRemainDistinct() {
        assertEquals("Puzru", resourceValue("app_name"))
        assertEquals("Puzru", resourceValue("product_title"))
        assertTrue(resourceValue("product_subtitle").contains("AI"))

        val states = listOf("state_ready", "state_waiting", "state_active", "state_error")
            .map(::resourceValue)
        assertTrue(states.all { it.isNotBlank() })
        assertEquals(states.size, states.toSet().size)
        assertFalse(resourceValue("diagnostics_on") == resourceValue("state_active"))

        val earpiece = resourceValue("diagnostics_route_earpiece")
        val loudspeaker = resourceValue("diagnostics_route_speaker")
        assertTrue(earpiece.contains("ya kati ya telefone", ignoreCase = true))
        assertTrue(loudspeaker.contains("haut-parleur", ignoreCase = true))
        assertFalse(earpiece == loudspeaker)
        assertTrue(resourceValue("settings_about_body").contains("écouteur ya kati ya telefone"))

        val routing = resourceValue("diagnostics_routing")
        assertTrue(routing.contains("kotambwisa", ignoreCase = true))
        assertFalse(routing.contains("kotinda", ignoreCase = true))
        val communicationAudio = resourceValue("diagnostics_error_audio_preparation")
        assertTrue(communicationAudio.contains("audio ya communication"))
        assertFalse(communicationAudio.contains("mongongo ya bosololi"))
        assertTrue(resourceValue("diagnostics_error_session_ended").startsWith("Session ya audio"))
    }

    @Test
    fun privacyStructureClaimSetAndRoutingActorsRemainProtected() {
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        val paragraphs = privacy.split("\\n\\n")
        assertEquals(5, paragraphs.size)
        listOf("esɛngaka kɔnti to kokɔta te", "esɛngaka ndingisa ya kosalela mikrofɔ te", "ekangaka mongongo ya mikrofɔ te", "ekangaka to ebombaka masolo na yo to makambo na yango ya mongongo te")
            .forEach { guard -> assertTrue("Missing microphone/conversation guard: $guard", paragraphs[0].contains(guard)) }
        listOf("ba API ya Android ya bato nyonso", "metadata ya kobɛta mongongo mpe ya ba session ya enregistrement", "ekangaka mongongo ya mikrofɔ oyo ezali na ba session yango te")
            .forEach { guard -> assertTrue("Missing public-metadata guard: $guard", paragraphs[1].contains(guard)) }
        listOf("esika ya Puzru oyo ezali kaka mpo na aplikasio", "ebimisamaka mpe traitement na yango esalemaka na aparɛyi na yo", "Balapɔrɔ ebombamaka to etindamaka yango moko te", "oponi esika na nzela ya Android", "Puzru te")
            .forEach { guard -> assertTrue("Missing local/export guard: $guard", paragraphs[2].contains(guard)) }
        listOf("esɛngaka ndingisa ya Internet ya Android te", "backend ya Puzru to nzela ya kotinda bansango na réseau te", "ebombaka bansango ya diagnostic na serveur te")
            .forEach { guard -> assertTrue("Missing network guard: $guard", paragraphs[3].contains(guard)) }
        listOf("Backup ya ba données ya aplikasio ya Android mpe transfert na aparɛyi mosusu elongolami", "ezalela ya diagnostic oyo ezali mpo na mwa ntango ezali lapɔrɔ oyo ebombami te", "esika to service oyo oponaki")
            .forEach { guard -> assertTrue("Missing retention guard: $guard", paragraphs[4].contains(guard)) }
        assertFalse(privacy.contains("esalelamaka kaka na aparɛyi"))

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("endimamaki te"))
        listOf("Android", "système", "telefone", "Puzru").forEach { actor ->
            assertFalse("Rejected request invented actor: $actor", rejected.contains(actor, ignoreCase = true))
        }
        assertTrue(resourceValue("diagnostics_error_blocked_by_system").contains("Système"))

        assertFalse(strings.contains("name=\"settings_assistant_early_route\""))
        assertFalse(strings.contains("name=\"settings_assistant_early_route_description\""))
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
