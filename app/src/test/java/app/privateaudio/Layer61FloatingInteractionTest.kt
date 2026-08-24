package app.privateaudio

import app.privateaudio.overlay.MiniControl
import app.privateaudio.overlay.MiniGestureArbitrator
import app.privateaudio.overlay.OverlayPosition
import app.privateaudio.overlay.clampOverlayPosition
import app.privateaudio.overlay.miniControlAt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer61FloatingInteractionTest {
    @Test fun positionIsClampedToEveryScreenEdge() {
        assertEquals(OverlayPosition(0, 0), clampOverlayPosition(-40, -20, 1080, 1920, 600, 124))
        assertEquals(OverlayPosition(480, 1796), clampOverlayPosition(900, 1900, 1080, 1920, 600, 124))
        assertEquals(OverlayPosition(240, 800), clampOverlayPosition(240, 800, 1080, 1920, 600, 124))
    }

    @Test fun everyMiniRegionCanStartADrag() {
        MiniControl.entries.forEach { control ->
            val gesture = MiniGestureArbitrator(8f)
            gesture.down(control)

            assertTrue("Expected drag from $control", gesture.move(9f, 0f))
            assertTrue(gesture.dragging)
        }
    }

    @Test fun movementAtOrBelowTouchSlopRemainsATapGesture() {
        val gesture = MiniGestureArbitrator(8f)
        gesture.down(MiniControl.POWER)

        assertFalse(gesture.move(8f, 0f))
        assertFalse(gesture.dragging)
        assertEquals(MiniControl.POWER, gesture.up(MiniControl.POWER))
    }

    @Test fun movementBeyondTouchSlopCancelsPendingControlPermanently() {
        val gesture = MiniGestureArbitrator(8f)
        gesture.down(MiniControl.POWER)

        assertTrue(gesture.move(9f, 0f))
        assertEquals(MiniControl.NONE, gesture.touchedControl)
        assertTrue(gesture.move(0f, 0f))
        assertEquals(MiniControl.NONE, gesture.up(MiniControl.POWER))
    }

    @Test fun upAfterDragCannotActivateAnyControl() {
        listOf(MiniControl.POWER, MiniControl.EXPAND, MiniControl.CLOSE).forEach { control ->
            val gesture = MiniGestureArbitrator(8f)
            gesture.down(control)
            gesture.move(0f, 9f)

            assertEquals("Unexpected activation for $control", MiniControl.NONE, gesture.up(control))
        }
    }

    @Test fun cancelCannotActivateAndClearsGestureState() {
        val gesture = MiniGestureArbitrator(8f)
        gesture.down(MiniControl.CLOSE)
        gesture.cancel()

        assertFalse(gesture.dragging)
        assertEquals(MiniControl.NONE, gesture.touchedControl)
        assertEquals(MiniControl.NONE, gesture.up(MiniControl.CLOSE))
    }

    @Test fun ordinaryControlTapsStillActivateOnlyTheIntendedControl() {
        listOf(MiniControl.POWER, MiniControl.EXPAND, MiniControl.CLOSE).forEach { control ->
            val gesture = MiniGestureArbitrator(8f)
            gesture.down(control)
            assertEquals(control, gesture.up(control))

            gesture.down(control)
            assertEquals(MiniControl.NONE, gesture.up(MiniControl.NONE))
        }
    }

    @Test fun productionTouchFlowUsesAndroidTouchSlopAndExistingWindowMovement() {
        assertTrue(overlay.contains("ViewConfiguration.get(context).scaledTouchSlop"))
        assertTrue(overlay.contains("hypot(dx, dy) > touchSlop"))
        assertTrue(overlay.contains("if (gesture.move(dx, dy))"))
        assertTrue(overlay.contains("windowManager.updateViewLayout(it, layoutParams)"))
        assertFalse(overlay.contains("SharedPreferences"))
    }

    @Test fun productionUpDispatchesOnlyTheArbitratedControl() {
        assertTrue(overlay.contains("when (gesture.up(controlAt(event.x)))"))
        assertTrue(overlay.contains("MiniControl.POWER ->"))
        assertTrue(overlay.contains("MiniControl.EXPAND ->"))
        assertTrue(overlay.contains("MiniControl.CLOSE ->"))
        assertTrue(overlay.contains("gesture.cancel()"))
    }

    @Test fun hitTestingMirrorsWithTheInternalRtlComposition() {
        val width = 100f
        val logicalCases = listOf(
            0f to MiniControl.NONE,
            39.99f to MiniControl.NONE,
            40f to MiniControl.POWER,
            59.99f to MiniControl.POWER,
            60f to MiniControl.EXPAND,
            79.99f to MiniControl.EXPAND,
            80f to MiniControl.CLOSE,
            100f to MiniControl.CLOSE,
        )
        logicalCases.forEach { (logicalX, expected) ->
            assertEquals("LTR at $logicalX", expected, miniControlAt(logicalX, width, rtl = false))
            assertEquals("RTL at $logicalX", expected, miniControlAt(width - logicalX, width, rtl = true))
        }

        val controlAt = overlay.kotlinDeclaration("private fun controlAt(")
        assertTrue(controlAt.contains("miniControlAt(x, width.toFloat(), isRtlLayout())"))
    }

    @Test fun mainBackgroundsOnlyAfterOverlayReportsSuccess() {
        assertTrue(main.contains("resultCode == OverlayService.SHOW_SUCCEEDED"))
        assertTrue(main.contains("moveTaskToBack(true)"))
        val firstShow = overlay.substringAfter("try {").substringBefore("} catch")
        assertTrue(firstShow.contains("windowManager.addView(surface, layoutParams)"))
        assertTrue(firstShow.indexOf("windowManager.addView(surface, layoutParams)") < firstShow.indexOf("resultReceiver?.send(SHOW_SUCCEEDED"))
        assertFalse(main.substringAfter("if (Settings.canDrawOverlays(this)) showOverlay()").substringBefore("override fun onStop").contains("moveTaskToBack"))
    }

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val main = File(root, "app/src/main/java/app/privateaudio/MainActivity.kt").readText()
    }
}
