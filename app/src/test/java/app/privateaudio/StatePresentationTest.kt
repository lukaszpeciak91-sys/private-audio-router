package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StatePresentationTest {
    @Test fun everyServiceStateHasOneMainMiniAndSymbolPresentation() {
        val presentations = PrivateAudioState.entries.associateWith(::statePresentation)

        assertEquals(R.string.status_main_ready, presentations.getValue(PrivateAudioState.READY).mainLabel)
        assertEquals(R.string.status_main_waiting, presentations.getValue(PrivateAudioState.WAITING).mainLabel)
        assertEquals(R.string.status_main_active, presentations.getValue(PrivateAudioState.ACTIVE).mainLabel)
        assertEquals(R.string.status_main_error, presentations.getValue(PrivateAudioState.ERROR).mainLabel)
        assertEquals(StatusSymbol.entries.toSet(), presentations.values.map { it.symbol }.toSet())
        presentations.values.forEach { assertNotEquals(it.mainLabel, 0) }
    }

    @Test fun readyHasNoNotificationAndEnabledStatesHaveCompleteNotificationCopy() {
        val ready = statePresentation(PrivateAudioState.READY)
        assertNull(ready.notificationTitle)
        assertNull(ready.notificationText)

        PrivateAudioState.entries.filterNot { it == PrivateAudioState.READY }.forEach { state ->
            val presentation = statePresentation(state)
            assertNotEquals("$state title", null, presentation.notificationTitle)
            assertNotEquals("$state text", null, presentation.notificationText)
        }
    }
}
