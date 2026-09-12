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

    @Test fun notificationApiBoundaryIsNarrowAndOlderDevicesArmDirectly() {
        val handlePowerOn = main.kotlinDeclaration("private fun handlePowerOn()")
        assertTrue(handlePowerOn.contains("Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU"))
        assertTrue(handlePowerOn.contains("armRouting()"))
        assertTrue(handlePowerOn.contains("return"))
        assertTrue(handlePowerOn.contains("handlePowerOnApi33()"))
        assertFalse(handlePowerOn.contains("POST_NOTIFICATIONS"))

        val api33Power = main.kotlinDeclaration("private fun handlePowerOnApi33()")
        val api33Request = main.kotlinDeclaration("private fun launchNotificationPermissionApi33()")
        listOf(api33Power, api33Request).forEach { helper ->
            assertTrue(helper.contains("Manifest.permission.POST_NOTIFICATIONS"))
        }
        assertTrue(main.contains("@RequiresApi(Build.VERSION_CODES.TIRAMISU)\n    private fun handlePowerOnApi33()"))
        assertTrue(main.contains("@RequiresApi(Build.VERSION_CODES.TIRAMISU)\n    private fun launchNotificationPermissionApi33()"))

        val primaryAction = main.kotlinDeclaration(
            "private fun handleExplanationPrimary(explanation: PermissionExplanation)",
        )
        assertTrue(primaryAction.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU"))
        assertTrue(primaryAction.contains("launchNotificationPermissionApi33()"))
        assertTrue(primaryAction.contains("else {\n                    armRouting()"))
        assertFalse(primaryAction.contains("POST_NOTIFICATIONS"))

        assertFalse(main.contains("@SuppressLint(\"InlinedApi\")"))
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

    @Test fun permissionCopyUsesOnlyLocalizableDefaultEnglishUntilControlledRollout() {
        val keys = listOf(
            "permission_notification_title", "permission_notification_body",
            "permission_notification_allow", "permission_notification_continue",
            "permission_overlay_title", "permission_overlay_body",
            "permission_overlay_open_settings", "permission_overlay_not_now",
        )
        val defaultNodes = stringNodes(File(resources, "values/strings.xml"))
        keys.forEach { key ->
            val matching = defaultNodes.filter { it.attributes.getNamedItem("name").nodeValue == key }
            assertEquals("default: $key", 1, matching.size)
            assertTrue("$key must remain localizable", matching.single().attributes.getNamedItem("translatable") == null)
            assertEquals(
                "$key must have only the temporary element-scoped lint suppression",
                "MissingTranslation",
                matching.single().attributes.getNamedItem("tools:ignore")?.nodeValue,
            )
        }
        localeDirectories.forEach { directory ->
            val names = stringNodes(File(directory, "strings.xml"))
                .map { it.attributes.getNamedItem("name").nodeValue }
            keys.forEach { assertFalse("${directory.name}: copied permission key $it", names.contains(it)) }
        }
    }

    private fun stringNodes(file: File) = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        .parse(file).getElementsByTagName("string").let { nodes ->
            (0 until nodes.length).map(nodes::item)
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
        val resources = File(root, "app/src/main/res")
        val localeDirectories = resources.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") && it.name != "values-night" }
    }
}
