package app.privateaudio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer7AProximityContractTest {
    @Test
    fun onlyActiveCommunicationEarpieceStateIsEligible() {
        listOf(PrivateAudioState.READY, PrivateAudioState.WAITING, PrivateAudioState.ERROR).forEach {
            assertFalse(proximityEligible(true, true, it, MODE, EARPIECE, true))
        }
        assertTrue(proximityEligible(true, true, PrivateAudioState.ACTIVE, MODE, EARPIECE, true))
        assertFalse(proximityEligible(true, true, PrivateAudioState.ACTIVE, MODE, EARPIECE, false))
        assertFalse(proximityEligible(true, false, PrivateAudioState.ACTIVE, MODE, EARPIECE, true))
        assertFalse(proximityEligible(true, true, PrivateAudioState.ACTIVE, "MODE_NORMAL", EARPIECE, true))
        assertFalse(proximityEligible(true, true, PrivateAudioState.ACTIVE, MODE, "Built-in speaker", true))
        assertFalse(proximityEligible(true, true, PrivateAudioState.ACTIVE, MODE, null, true))
        assertTrue(helper.contains("route == \"Built-in earpiece\""))
        assertFalse(helper.contains("Built-in earpiece”"))
    }

    @Test
    fun helperOwnsOneNonReferenceCountedPublicWakeLockAndIsIdempotent() {
        assertTrue(helper.contains("PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK"))
        assertTrue(helper.contains("isWakeLockLevelSupported"))
        assertTrue(helper.contains("setReferenceCounted(false)"))
        assertTrue(helper.method("fun acquire(").contains("if (!operationsAvailable || held) return"))
        assertTrue(helper.method("fun release(").contains("if (!held) return"))
        assertFalse(helper.contains("RELEASE_FLAG_WAIT_FOR_NO_PROXIMITY"))
        assertFalse(helper.contains("SensorManager"))
        assertFalse(helper.contains("ChatGPT"))
        assertFalse(helper.contains("Gemini"))
        assertTrue(helper.method("fun acquire(").contains("runCatching { lock.acquire() }"))
        assertTrue(helper.method("fun release(").contains("runCatching { lock.release() }"))
        assertTrue(helper.substringBefore("internal fun proximityEligible").contains("runCatching"))
        assertFalse(service.contains("runCatching { proximityController"))
    }

    @Test
    fun serviceSynchronizesFromObserverAndHasExplicitFailSafeReleases() {
        assertTrue(service.contains("onEvidenceChanged = ::syncProximityBehavior"))
        assertTrue(service.method("private fun syncProximityBehavior(").contains("proximityEligible("))
        assertTrue(service.method("private fun syncProximityBehavior(").contains("proximityController.acquire"))
        assertTrue(service.method("private fun syncProximityBehavior(").contains("proximityController.release"))
        assertTrue(service.method("fun disarmAndStopStartedLifetime()").contains("proximityController.release(\"Power OFF\""))
        assertTrue(service.method("override fun onDestroy()").contains("proximityController.release(\"Service destroyed\""))
        assertInOrder(
            service.method("fun disarmAndStopStartedLifetime()"),
            "isPrivateAudioEnabled = false",
            "proximityController.release",
            "observer.disableController()",
        )
        assertInOrder(
            service.method("override fun onDestroy()"),
            "isPrivateAudioEnabled = false",
            "proximityController.release",
            "observer.stop",
        )
        assertTrue(observer.method("fun snapshot(").contains("onEvidenceChanged(reason)"))
    }

    @Test
    fun laterCyclesRemainObserverOwnedAndUiDoesNotOwnProximity() {
        assertTrue(observer.method("private fun returnToWaiting()").contains("cycleGeneration++"))
        assertTrue(observer.method("private fun returnToWaiting()").contains("EarpieceExperiment(state = ExperimentState.ARMED"))
        assertFalse(main.contains("ProximityScreenController"))
        assertFalse(overlay.contains("ProximityScreenController"))
        assertFalse(main.contains("PROXIMITY_SCREEN_OFF_WAKE_LOCK"))
        assertFalse(overlay.contains("PROXIMITY_SCREEN_OFF_WAKE_LOCK"))
    }

    private fun String.method(signature: String): String {
        val start = indexOf(signature)
        check(start >= 0) { "Missing method: $signature" }
        val opening = indexOf('{', start)
        var depth = 0
        for (index in opening until length) {
            when (this[index]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return substring(start, index + 1)
            }
        }
        error("Unterminated method: $signature")
    }

    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment ->
            val current = source.indexOf(fragment)
            assertTrue("Missing expected source fragment: $fragment", current >= 0)
            assertTrue("Expected source fragments in order", current > previous)
            previous = current
        }
    }

    private companion object {
        const val MODE = "MODE_IN_COMMUNICATION"
        const val EARPIECE = "Built-in earpiece"
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val helper = source("app/src/main/java/app/privateaudio/ProximityScreenController.kt")
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val main = source("app/src/main/java/app/privateaudio/MainActivity.kt")
        val overlay = source("app/src/main/java/app/privateaudio/overlay/OverlayService.kt")
    }
}
