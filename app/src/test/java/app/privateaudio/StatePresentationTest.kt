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

        assertNotification(
            PrivateAudioState.WAITING,
            R.string.routing_notification_waiting_title,
            R.string.routing_notification_waiting_text,
        )
        assertNotification(
            PrivateAudioState.ACTIVE,
            R.string.routing_notification_active_title,
            R.string.routing_notification_active_text,
        )
        assertNotification(
            PrivateAudioState.ERROR,
            R.string.routing_notification_error_title,
            R.string.routing_notification_error_text,
        )
    }

    private fun assertNotification(state: PrivateAudioState, title: Int, text: Int) {
        val presentation = statePresentation(state)
        assertEquals(title, presentation.notificationTitle)
        assertEquals(text, presentation.notificationText)
    }
}
