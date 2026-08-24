package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class PrivateAudioStateTest {
    @Test
    fun controllerOffIsReadyAndOverridesStaleHistory() {
        assertEquals(PrivateAudioState.READY, project(enabled = false))
        assertEquals(
            PrivateAudioState.READY,
            project(
                enabled = false,
                failure = true,
                participating = true,
                inCommunication = true,
                earpiece = true,
            ),
        )
    }

    @Test
    fun enabledButInactiveConditionsAreWaiting() {
        assertEquals(PrivateAudioState.WAITING, project(enabled = true))
        // Ordinary media does not appear in the Layer 1.6 evidence consumed by this projection.
        assertEquals(PrivateAudioState.WAITING, project(enabled = true))
        assertEquals(
            PrivateAudioState.WAITING,
            project(enabled = true, participating = true, inCommunication = true),
        )
    }

    @Test
    fun onlyCurrentParticipatingCommunicationEarpieceEvidenceIsActive() {
        assertEquals(
            PrivateAudioState.ACTIVE,
            project(enabled = true, participating = true, inCommunication = true, earpiece = true),
        )
        assertEquals(
            PrivateAudioState.WAITING,
            project(enabled = true, participating = false, inCommunication = true, earpiece = true),
        )
        assertEquals(
            PrivateAudioState.WAITING,
            project(enabled = true, participating = true, inCommunication = true, earpiece = false),
        )
    }

    @Test
    fun historicalReversionCannotPoisonLaterCurrentActiveEvidence() {
        // Historical diagnostics are deliberately absent from the projection input.
        assertEquals(
            PrivateAudioState.ACTIVE,
            project(enabled = true, participating = true, inCommunication = true, earpiece = true),
        )
    }

    @Test
    fun normalCleanupAndASecondCycleProjectWaitingThenActive() {
        assertEquals(PrivateAudioState.WAITING, project(enabled = true))
        assertEquals(
            PrivateAudioState.ACTIVE,
            project(enabled = true, participating = true, inCommunication = true, earpiece = true),
        )
    }

    @Test
    fun currentProtectedFailuresAreErrorUntilPowerOff() {
        assertEquals(PrivateAudioState.ERROR, project(enabled = true, failure = true))
        assertEquals(
            PrivateAudioState.ERROR,
            project(
                enabled = true,
                failure = true,
                participating = true,
                inCommunication = true,
                earpiece = true,
            ),
        )
        assertEquals(PrivateAudioState.READY, project(enabled = false, failure = true))
    }

    @Test
    fun projectionSupportsObservableMultiSessionTransitions() {
        val states = listOf(
            project(enabled = false),
            project(enabled = true),
            project(enabled = true, participating = true, inCommunication = true, earpiece = true),
            project(enabled = true),
            project(enabled = true, participating = true, inCommunication = true, earpiece = true),
            project(enabled = false),
        )

        assertEquals(
            listOf(
                PrivateAudioState.READY,
                PrivateAudioState.WAITING,
                PrivateAudioState.ACTIVE,
                PrivateAudioState.WAITING,
                PrivateAudioState.ACTIVE,
                PrivateAudioState.READY,
            ),
            states,
        )
    }

    @Test
    fun serviceOwnsObservableProjectionWithoutRoutingApis() {
        assertEquals(setOf("READY", "WAITING", "ACTIVE", "ERROR"), enumValues<PrivateAudioState>().map { it.name }.toSet())
        assertFalse(stateSource.contains("setCommunicationDevice("))
        assertFalse(stateSource.contains("clearCommunicationDevice("))
        assertFalse(stateSource.contains("AudioTrack"))
        assertFalse(stateSource.contains("AudioManager"))
        assertFalse(serviceSource.contains("var privateAudioState"))
        assertEquals(1, serviceSource.occurrences("val privateAudioState: PrivateAudioState"))
        assertEquals(
            listOf(observerFile),
            productionSources.kotlinMemberCallSites("setCommunicationDevice").map(KotlinCallSite::file),
        )
    }

    private fun project(
        enabled: Boolean,
        failure: Boolean = false,
        participating: Boolean = false,
        inCommunication: Boolean = false,
        earpiece: Boolean = false,
    ) = projectPrivateAudioState(
        PrivateAudioStateEvidence(
            controllerEnabled = enabled,
            currentProtectedFailure = failure,
            currentCycleParticipating = participating,
            modeInCommunication = inCommunication,
            builtInEarpieceIsCurrent = earpiece,
        ),
    )

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val stateSource = File(
            projectRoot,
            "app/src/main/java/app/privateaudio/PrivateAudioState.kt",
        ).readText()
        val serviceSource = File(
            projectRoot,
            "app/src/main/java/app/privateaudio/PrivateAudioService.kt",
        ).readText()
        val observerFile = File(
            projectRoot,
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        )
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
    }
}
