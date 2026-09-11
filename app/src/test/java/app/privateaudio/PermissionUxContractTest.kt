package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class PermissionUxContractTest {
    @Test fun activityKeepsPermissionUxOutsideProtectedServices() {
        assertTrue(main.contains("ActivityResultContracts.RequestPermission()"))
        assertTrue(main.contains(") { armRouting() }"))
        assertTrue(main.contains("PermissionExplanation.NOTIFICATION -> {"))
        assertTrue(main.contains("permissionUxPreferences.notificationExplanationResolved = true"))
        assertTrue(main.contains("PermissionExplanation.OVERLAY -> {"))
        assertTrue(main.contains("permissionUxPreferences.overlayExplanationResolved = true"))
        assertTrue(main.contains("Uri.parse(\"package:\$packageName\")"))
        assertTrue(main.contains("if (Settings.canDrawOverlays(this)) showOverlay()"))
        assertFalse(service.contains("PermissionUx"))
        assertFalse(overlay.contains("PermissionUx"))
        assertTrue(overlay.contains("if (!Settings.canDrawOverlays(this)) return"))
    }

    @Test fun preferenceOwnerContainsExactlyTwoFalseDefaultBooleanFlags() {
        assertEquals(2, preferences.windowed("getBoolean(".length).count { it == "getBoolean(" })
        assertEquals(2, preferences.windowed(", false)".length).count { it == ", false)" })
        listOf("notification_explanation_resolved", "overlay_explanation_resolved").forEach {
            assertTrue(preferences.contains(it))
        }
        listOf("routing", "timestamp", "counter", "analytics").forEach { assertFalse(preferences.contains(it)) }
    }

    @Test fun styledScrollablePanelsHaveStableInteractionContracts() {
        assertTrue(settings.contains("PermissionExplanationPanel"))
        assertFalse(settings.contains("AlertDialog"))
        listOf("panel", "primary", "secondary", "backdrop").forEach {
            assertTrue(settings.contains("\${prefix}_permission_$it"))
        }
        assertTrue(settings.contains("WindowInsets.safeDrawing"))
        assertTrue(settings.contains("verticalScroll(rememberScrollState())"))
        assertTrue(settings.contains("onDismissRequest = onSecondary"))
        assertTrue(settings.contains(".clickable(onClick = onSecondary)"))
        assertTrue(settings.contains("heightIn(min = 48.dp)"))
    }

    @Test fun everyProductLocaleDefinesAllPermissionKeysExactlyOnce() {
        val keys = listOf(
            "permission_notification_title", "permission_notification_body",
            "permission_notification_allow", "permission_notification_continue",
            "permission_overlay_title", "permission_overlay_body",
            "permission_overlay_open_settings", "permission_overlay_not_now",
        )
        resourceDirectories.forEach { directory ->
            val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(File(directory, "strings.xml")).getElementsByTagName("string")
            val names = (0 until nodes.length).map { nodes.item(it).attributes.getNamedItem("name").nodeValue }
            keys.forEach { assertEquals("${directory.name}: $it", 1, names.count { name -> name == it }) }
        }
    }

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val main = source("app/src/main/java/app/privateaudio/MainActivity.kt")
        val settings = source("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt")
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val overlay = source("app/src/main/java/app/privateaudio/overlay/OverlayService.kt")
        val preferences = source("app/src/main/java/app/privateaudio/PermissionUxPreferences.kt")
        val resourceDirectories = File(root, "app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values") && it.name != "values-night" }
    }
}
