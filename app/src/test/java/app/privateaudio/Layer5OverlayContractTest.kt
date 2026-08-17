package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer5OverlayContractTest {
    @Test
    fun permissionIsCheckedBeforeAndAfterTheSystemPermissionScreen() {
        assertTrue(manifest.contains("android.permission.SYSTEM_ALERT_WINDOW"))
        assertTrue(mainSource.contains("Settings.canDrawOverlays(this)"))
        assertTrue(mainSource.contains("Settings.ACTION_MANAGE_OVERLAY_PERMISSION"))
        assertTrue(mainSource.contains("Uri.parse(\"package:\$packageName\")"))
        assertTrue(mainSource.contains("if (overlayPermissionRequestPending)"))
        assertTrue(mainSource.contains("if (Settings.canDrawOverlays(this)) showOverlay()"))
        assertFalse(mainSource.contains("overlayPermissionRequestPending = true\n            showOverlay()"))
    }

    @Test
    fun overlayServiceOwnsOneFailClosedApplicationOverlay() {
        assertTrue(manifest.contains("android:name=\".overlay.OverlayService\""))
        assertTrue(overlaySource.contains("WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY"))
        assertTrue(overlaySource.contains("if (overlayView != null || !Settings.canDrawOverlays(this)) return"))
        assertTrue(overlaySource.contains("return START_NOT_STICKY"))
        assertFalse(overlaySource.contains("SharedPreferences"))
        assertFalse(overlaySource.contains("onTaskRemoved"))
    }

    @Test
    fun showHideAndBothClosePathsHaveTheRequiredBoundaries() {
        assertTrue(screenSource.contains("onClick = onFloatingClick"))
        assertTrue(overlaySource.contains("ACTION_SHOW -> showOverlay()"))
        assertTrue(overlaySource.contains("ACTION_HIDE ->"))
        assertTrue(overlaySource.contains("override fun onDestroy()"))
        assertTrue(overlaySource.contains("overlayView?.let(windowManager::removeView)"))

        val overlayClose = overlaySource.substringAfter("private fun closeOverlay() {").substringBefore("}")
        assertTrue(overlayClose.contains("hideOverlay()"))
        assertTrue(overlayClose.contains("stopSelf()"))
        assertFalse(overlayClose.contains("disarm"))
        assertFalse(overlayClose.contains("finish"))

        val mainClose = mainSource.substringAfter("onCloseClick = {").substringBefore("},")
        assertTrue(mainClose.contains("hideOverlay()"))
        assertTrue(mainClose.contains("disarmAndStopStartedLifetime()"))
        assertTrue(mainClose.contains("finishAndRemoveTask()"))
    }

    @Test
    fun overlayContainsNoRoutingOrStateProjectionLogic() {
        listOf(
            "AudioManager",
            "setCommunicationDevice(",
            "MODE_IN_COMMUNICATION",
            "playbackConfigurations",
            "AudioDiagnosticObserver",
        ).forEach { forbidden -> assertFalse(forbidden, overlaySource.contains(forbidden)) }

        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
    }

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
        val manifest = projectFile("app/src/main/AndroidManifest.xml").readText()
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val screenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val overlaySource = projectFile("app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
