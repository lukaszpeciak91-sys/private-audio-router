package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FakePhonePreArmContractTest {
    @Test fun preferenceDefaultsOffPersistsAndBaseLabelIsEnglishOnly() {
        assertTrue(service.contains("mutableStateOf(false)"))
        assertTrue(service.contains(".getBoolean(FAKE_PHONE_PRE_ARM_KEY, false)"))
        assertTrue(service.contains(".putBoolean(FAKE_PHONE_PRE_ARM_KEY, enabled)"))
        assertTrue(baseStrings.contains("<string name=\"settings_fake_phone_pre_arm\">Fake Phone pre-arm</string>"))
        val localizedCopies = File(root, "app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .sumOf { directory -> directory.walkTopDown().filter { it.isFile }.count { it.readText().contains("settings_fake_phone_pre_arm") } }
        assertEquals(0, localizedCopies)
    }

    @Test fun offPathRemainsCleanWaitingAndPreArmIsSeparateFromPublicActiveEvidence() {
        val enable = observer.method("fun enableController()")
        assertTrue(enable.indexOf("handlePlaybackConfigurations") < enable.indexOf("maybeStartFakePhonePreArm"))
        assertTrue(observer.method("private fun maybeStartFakePhonePreArm()")
            .contains("!fakePhonePreArm.featureEnabled"))
        assertFalse(service.method("get() {").contains("fakePhonePreArm"))
        assertTrue(service.contains("currentExperiment.state == ExperimentState.REQUEST_ATTEMPTED"))
    }

    @Test fun preArmUsesProtectedIngredientsOnceAndHealthyHandoffDoesNotDuplicateThem() {
        val start = observer.method("private fun maybeStartFakePhonePreArm()")
        assertInOrder(start, "startSilentCommunicationTrack()", "requestCommunicationMode()", "requestCommunicationDevice(earpiece)")
        assertTrue(start.contains("isTelephonyOrSystemPriorityMode"))
        assertTrue(start.contains("TYPE_BUILTIN_EARPIECE"))
        val promote = observer.method("private fun promoteFakePhonePreArm(")
        assertFalse(promote.contains("startSilentCommunicationTrack"))
        assertFalse(promote.contains("requestCommunicationDevice"))
        assertTrue(promote.contains("attempts = listOf(attempt)"))
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
    }

    @Test fun waitingPowerServiceSafetyAndOptionalFailureCleanupAreExplicit() {
        val setter = observer.method("fun updateFakePhonePreArmEnabled(")
        assertTrue(setter.contains("cleanupFakePhonePreArm(\"Preference disabled\")"))
        assertTrue(observer.method("fun disableController()").contains("cleanupFakePhonePreArm(\"Power OFF\")"))
        assertTrue(observer.method("fun stop(").contains("cleanupFakePhonePreArm(reason)"))
        val cleanup = observer.method("private fun cleanupFakePhonePreArm(")
        assertInOrder(cleanup, "cancelPendingObservation", "clearCommunicationDevice", "MODE_NORMAL", "stopSilentCommunicationTrack")
        assertTrue(cleanup.contains("ExperimentState.ARMED"))
        assertFalse(startFailureBlock().contains("ExperimentState.BLOCKED"))
    }

    @Test fun activeToggleDefersAndSonificationIsRecordedNeutrally() {
        val setter = observer.method("fun updateFakePhonePreArmEnabled(")
        assertTrue(setter.contains("if (!controllerEnabled || experiment.requestAttempted) return"))
        assertTrue(observer.contains("ASSISTANT/SONIFICATION observed during pre-arm"))
        assertFalse(observer.contains("ChatGPT ping"))
        assertFalse(observer.contains("Gemini ping"))
        assertTrue(observer.contains("Audible result: Requires physical confirmation"))
    }

    private fun startFailureBlock() = observer.method("private fun maybeStartFakePhonePreArm()")
    private fun String.occurrences(needle: String) = windowed(needle.length).count { it == needle }
    private fun String.method(signature: String): String {
        val start = indexOf(signature).also { check(it >= 0) { "Missing $signature" } }
        val opening = indexOf('{', start)
        var depth = 0
        for (index in opening until length) when (this[index]) {
            '{' -> depth++
            '}' -> if (--depth == 0) return substring(start, index + 1)
        }
        error("Unterminated $signature")
    }
    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment -> previous = source.indexOf(fragment).also { assertTrue(it > previous) } }
    }
    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val baseStrings = source("app/src/main/res/values/strings.xml")
        val productionSources = File(root, "app/src/main/java").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
