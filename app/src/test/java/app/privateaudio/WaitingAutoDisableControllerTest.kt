package app.privateaudio

import app.privateaudio.WaitingAutoDisableController.Companion.WAITING_AUTO_DISABLE_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class WaitingAutoDisableControllerTest {
    @Test
    fun initialWaitingSchedulesExactlyOneThirtyMinuteDeadline() {
        val fixture = Fixture()

        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        assertEquals(1, fixture.scheduler.activeCount)
        assertEquals(WAITING_AUTO_DISABLE_MILLIS, fixture.scheduler.nextDelay)
    }

    @Test
    fun repeatedWaitingEvidenceDoesNotRestartOrExtendDeadline() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)
        val first = fixture.scheduler.tasks.single()

        fixture.scheduler.advanceBy(10_000)
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        assertEquals(1, fixture.scheduler.tasks.size)
        assertEquals(first.dueAt, fixture.scheduler.nextDueAt)
    }

    @Test
    fun leavingWaitingForActiveCancelsDeadline() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        fixture.controller.onStateChanged(PrivateAudioState.ACTIVE)

        assertEquals(0, fixture.scheduler.activeCount)
    }

    @Test
    fun activeToWaitingStartsAFreshFullDeadline() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)
        fixture.scheduler.advanceBy(20_000)
        fixture.controller.onStateChanged(PrivateAudioState.ACTIVE)
        fixture.scheduler.advanceBy(30_000)

        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        assertEquals(fixture.scheduler.now + WAITING_AUTO_DISABLE_MILLIS, fixture.scheduler.nextDueAt)
    }

    @Test
    fun everyWaitingPeriodAcrossMultipleCyclesGetsThirtyMinutes() {
        val fixture = Fixture()
        repeat(3) {
            fixture.controller.onStateChanged(PrivateAudioState.WAITING)
            assertEquals(fixture.scheduler.now + WAITING_AUTO_DISABLE_MILLIS, fixture.scheduler.nextDueAt)
            fixture.scheduler.advanceBy(1_000)
            fixture.controller.onStateChanged(PrivateAudioState.ACTIVE)
            assertEquals(0, fixture.scheduler.activeCount)
        }
    }

    @Test
    fun explicitCancellationRemovesPendingWork() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        fixture.controller.cancel()

        assertEquals(0, fixture.scheduler.activeCount)
    }

    @Test
    fun destructionCancellationRemovesPendingWork() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        fixture.controller.cancel()
        fixture.scheduler.advanceBy(WAITING_AUTO_DISABLE_MILLIS)

        assertEquals(0, fixture.timeouts)
    }

    @Test
    fun staleWorkCannotDisableActiveOrANewerWaitingSession() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)
        val stale = fixture.scheduler.tasks.single()
        fixture.controller.onStateChanged(PrivateAudioState.ACTIVE)
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        fixture.scheduler.runEvenIfCancelled(stale)

        assertEquals(0, fixture.timeouts)
        assertEquals(1, fixture.scheduler.activeCount)
    }

    @Test
    fun expiryInvokesAuthoritativeTimeoutActionOnce() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)

        fixture.scheduler.advanceBy(WAITING_AUTO_DISABLE_MILLIS)

        assertEquals(1, fixture.timeouts)
        assertEquals(0, fixture.scheduler.activeCount)
    }

    @Test
    fun activeCanContinuePastThirtyMinutesWithoutTimeout() {
        val fixture = Fixture()
        fixture.controller.onStateChanged(PrivateAudioState.WAITING)
        fixture.controller.onStateChanged(PrivateAudioState.ACTIVE)

        fixture.scheduler.advanceBy(WAITING_AUTO_DISABLE_MILLIS * 2)

        assertEquals(0, fixture.timeouts)
    }

    @Test
    fun serviceOwnsTheDeadlineAndReusesItsNormalDisarmPath() {
        val service = File("src/main/java/app/privateaudio/PrivateAudioService.kt").readText()

        assertEquals(1, service.split("onTimeout = ::disarmAndStopStartedLifetime").size - 1)
        assertEquals(2, service.split("waitingAutoDisableController.cancel()").size - 1)
        assertEquals(1, service.split("onEvidenceChanged = ::syncStateOwnedBehavior").size - 1)
    }

    private class Fixture {
        val scheduler = FakeDelayScheduler()
        var timeouts = 0
        val controller = WaitingAutoDisableController(scheduler) { timeouts += 1 }
    }

    private class FakeDelayScheduler : DelayScheduler {
        data class Task(val dueAt: Long, val action: () -> Unit, var cancelled: Boolean = false)

        var now = 0L
        val tasks = mutableListOf<Task>()
        val activeCount get() = tasks.count { !it.cancelled && it.dueAt > now }
        val nextDueAt get() = tasks.filter { !it.cancelled && it.dueAt > now }.minOf { it.dueAt }
        val nextDelay get() = nextDueAt - now

        override fun schedule(delayMillis: Long, action: () -> Unit): CancellableDelay {
            val task = Task(now + delayMillis, action)
            tasks += task
            return CancellableDelay { task.cancelled = true }
        }

        fun advanceBy(millis: Long) {
            now += millis
            tasks.filter { !it.cancelled && it.dueAt <= now }.forEach {
                it.cancelled = true
                it.action()
            }
        }

        fun runEvenIfCancelled(task: Task) = task.action()
    }
}
