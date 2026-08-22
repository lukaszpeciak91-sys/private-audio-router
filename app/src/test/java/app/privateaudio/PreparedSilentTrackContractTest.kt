package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PreparedSilentTrackContractTest {
    private val source = projectFile("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt").readText()

    @Test
    fun cleanWaitingPreparesExactlyOneInitializedStoppedTrackWithoutRoutingInfluence() {
        val enable = source.method("fun enableController()")
        val prepare = source.method("private fun prepareSilentCommunicationTrack()")
        assertEquals(1, enable.occurrences("prepareSilentCommunicationTrack()"))
        assertTrue(prepare.contains("track.playState == AudioTrack.PLAYSTATE_PLAYING"))
        assertTrue(prepare.contains("STATE_INITIALIZED"))
        assertTrue(prepare.contains("ShortArray"))
        assertTrue(prepare.contains("AudioTrack.WRITE_NON_BLOCKING"))
        assertEquals(1, prepare.occurrences("track.write("))
        assertTrue(prepare.contains("prefill failed open"))
        assertFalse(prepare.contains("track.play()"))
        assertFalse(prepare.contains("silentWriterThread"))
        assertFalse(prepare.contains("requestCommunicationMode"))
        assertFalse(prepare.contains("requestCommunicationDevice"))
        assertFalse(prepare.contains("ExperimentState.REQUEST_ATTEMPTED"))
        assertTrue(prepare.contains("audioManager.mode != AudioManager.MODE_NORMAL"))
    }

    @Test
    fun genuineTriggerReusesPreparedTrackAndPreservesPoc5OrderingAndFallback() {
        val start = source.method("private fun startSilentCommunicationTrack(): Boolean")
        val probe = source.method("private fun startProtectedPoc5Probe(")
        assertTrue(start.contains("preparedSilentTrack && silentTrack?.state == AudioTrack.STATE_INITIALIZED"))
        assertTrue(start.contains("val fallbackUsed = silentTrack == null"))
        assertTrue(start.contains("createSilentCommunicationTrack() ?: return false"))
        assertInOrder(start, "track.play()", "PLAYSTATE_PLAYING", "startSilenceWriter(track, silence)")
        assertInOrder(probe, "startSilentCommunicationTrack()", "requestCommunicationMode()", "performRoutingAttempt(earpiece")
        assertEquals(1, source.occurrences("audioManager.setCommunicationDevice(earpiece)"))
    }

    @Test
    fun lifecycleAndCycleCleanupReleaseOrRefreshPreparedOwnership() {
        assertTrue(source.method("fun disableController()").contains("releasePreparedSilentTrack"))
        assertTrue(source.method("fun stop(reason: String)").contains("releasePreparedSilentTrack"))
        assertInOrder(source.method("private fun returnToWaiting()"), "returned to clean waiting", "prepareSilentCommunicationTrack()")
        assertTrue(source.method("private fun startFakePhoneMicroRoute(").contains("releasePreparedSilentTrack"))
        assertTrue(source.method("private fun releasePreparedSilentTrack(").contains("no route or mode cleanup required"))
        assertTrue(source.contains("private const val ASSISTANT_SESSION_LINGER_MS = 7_000L"))
        assertTrue(source.contains("private const val END_CONFIRMATION_DELAY_MS = 1_500L"))
    }

    @Test
    fun reportCarriesConcisePreparationAndStartupTimingEvidence() {
        listOf(
            "Prepared silent track created:", "Preparation completed timestamp:",
            "Prepared track state:", "active playback observed while WAITING:",
            "Routing trigger timestamp:", "Prepared track reused:",
            "play() invocation timestamp:", "PLAYSTATE_PLAYING timestamp:",
            "Trigger to PLAYSTATE_PLAYING elapsed ms:", "Fallback AudioTrack creation used:",
            "Trigger to play() invocation elapsed ms:", "play() call duration ms:",
            "PLAYSTATE_PLAYING to mode observed elapsed ms:",
            "Mode observed to device request return elapsed ms:",
            "Device request start to earpiece first observed elapsed ms:",
            "Trigger to earpiece first observed elapsed ms:",
        ).forEach { assertTrue("Missing diagnostic field: $it", source.contains(it)) }
    }

    @Test
    fun startupTimingIsMonotonicGenerationScopedAndFakePhoneCannotReuseIt() {
        assertTrue(source.contains("SystemClock.elapsedRealtimeNanos()"))
        val matching = source.method("private fun matchingStartupTiming()")
        assertTrue(matching.contains("it.generation == cycleGeneration"))
        assertTrue(matching.contains("experiment.startupTimingGeneration == it.generation"))
        assertTrue(matching.contains("experiment.triggerOrigin != null"))
        assertTrue(source.contains("unknown / not applicable"))
        val fakePhone = source.method("private fun startFakePhoneMicroRoute(")
        assertFalse(fakePhone.contains("startupTiming ="))
    }

    private fun assertInOrder(text: String, vararg parts: String) {
        var prior = -1
        parts.forEach { part ->
            val next = text.indexOf(part)
            assertTrue("Missing $part", next >= 0)
            assertTrue("Out of order: $part", next > prior)
            prior = next
        }
    }

    private fun String.method(signature: String): String {
        val start = indexOf(signature)
        check(start >= 0)
        val opening = indexOf('{', start)
        var depth = 0
        for (index in opening until length) {
            when (this[index]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return substring(start, index + 1)
            }
        }
        error("Unterminated method")
    }

    private fun String.occurrences(needle: String) = windowed(needle.length).count { it == needle }

    private fun projectFile(path: String): File {
        var directory = File(System.getProperty("user.dir")).absoluteFile
        while (true) {
            File(directory, path).takeIf(File::isFile)?.let { return it }
            directory = directory.parentFile ?: error("Cannot find $path")
        }
    }
}
