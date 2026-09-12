package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionUxDecisionTest {
    @Test fun notificationDecisionIsContextualOneTimeAndOptional() {
        assertEquals(NotificationPowerDecision.START, notificationPowerDecision(32, false, false))
        assertEquals(NotificationPowerDecision.START, notificationPowerDecision(33, true, false))
        assertEquals(NotificationPowerDecision.EXPLAIN, notificationPowerDecision(33, false, false))
        assertEquals(NotificationPowerDecision.START, notificationPowerDecision(33, false, true))
    }

    @Test fun overlayDecisionExplainsOnlyTheFirstMissingAccessAttempt() {
        assertEquals(OverlayMiniDecision.SHOW, overlayMiniDecision(true, false))
        assertEquals(OverlayMiniDecision.EXPLAIN, overlayMiniDecision(false, false))
        assertEquals(OverlayMiniDecision.OPEN_SETTINGS, overlayMiniDecision(false, true))
    }
}
