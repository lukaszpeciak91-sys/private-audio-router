package app.privateaudio

import app.privateaudio.overlay.OverlayPosition
import app.privateaudio.overlay.clampOverlayPosition
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

    @Test fun backgroundDragUsesTouchSlopAndUpdatesExistingLayoutParams() {
        assertTrue(overlay.contains("ViewConfiguration.get(context).scaledTouchSlop"))
        assertTrue(overlay.contains("touchedControl != Control.NONE"))
        assertTrue(overlay.contains("hypot(dx, dy) > touchSlop"))
        assertTrue(overlay.contains("windowManager.updateViewLayout(it, layoutParams)"))
        assertFalse(overlay.contains("SharedPreferences"))
    }

    @Test fun controlsRemainTapOnlyAndIndependentFromDrag() {
        assertTrue(overlay.contains("!dragging && touchedControl != Control.NONE"))
        assertTrue(overlay.contains("touchedControl == controlAt(event.x)"))
        assertTrue(overlay.contains("Control.POWER -> togglePower()"))
        assertTrue(overlay.contains("Control.EXPAND -> expandMain()"))
        assertTrue(overlay.contains("Control.CLOSE -> closeOverlay()"))
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
