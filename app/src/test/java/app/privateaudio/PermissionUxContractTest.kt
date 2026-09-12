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
        assertTrue(main.contains("Uri.parse(\"package:\$packageName\")"))
        assertTrue(main.contains("if (Settings.canDrawOverlays(this)) showOverlay()"))
        assertFalse(main.contains("overlayExplanationResolved"))
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

    @Test fun preferenceOwnerRetainsOnlyTheNotificationExplanationFlag() {
        assertEquals(1, preferences.windowed("getBoolean(".length).count { it == "getBoolean(" })
        assertEquals(1, preferences.windowed(", false)".length).count { it == ", false)" })
        assertTrue(preferences.contains("notification_explanation_resolved"))
        assertFalse(preferences.contains("overlay_explanation_resolved"))
        listOf("routing", "timestamp", "counter", "analytics").forEach { assertFalse(preferences.contains(it)) }
    }

    @Test fun everyExplicitMissingAccessAttemptUsesTheExplanationPanel() {
        val miniRequest = main.kotlinDeclaration("private fun showOverlayOrRequestPermission()")
        assertTrue(miniRequest.contains("Settings.canDrawOverlays(this)"))
        assertTrue(miniRequest.contains("OverlayMiniDecision.EXPLAIN -> permissionExplanation"))
        assertFalse(miniRequest.contains("openOverlaySettings()"))

        val primary = main.kotlinDeclaration(
            "private fun handleExplanationPrimary(explanation: PermissionExplanation)",
        )
        assertTrue(primary.contains("PermissionExplanation.OVERLAY -> {"))
        assertTrue(primary.contains("openOverlaySettings()"))

        val secondary = main.kotlinDeclaration(
            "private fun handleExplanationSecondary(explanation: PermissionExplanation)",
        )
        assertTrue(secondary.contains("PermissionExplanation.OVERLAY -> Unit"))
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

    @Test fun permissionCopyIsLocalizedForEverySupportedLocale() {
        val keys = permissionUxKeys
        val defaultNodes = stringNodes(File(resources, "values/strings.xml"))
        val defaultOverlayBody = defaultNodes.single {
            it.attributes.getNamedItem("name").nodeValue == "permission_overlay_body"
        }.textContent
        assertTrue("default overlay body must not be blank", defaultOverlayBody.isNotBlank())
        listOf(
            "Display over other apps",
            "next screen",
            "select Puzru",
            "turn on this access",
            "access is optional",
            "does not let Puzru read or control other apps",
            "Audio routing works without Mini",
        ).forEach { requiredMeaning ->
            assertTrue(
                "default overlay body must communicate: $requiredMeaning",
                defaultOverlayBody.contains(requiredMeaning, ignoreCase = true),
            )
        }
        keys.forEach { key ->
            val matching = defaultNodes.filter { it.attributes.getNamedItem("name").nodeValue == key }
            assertEquals("default: $key", 1, matching.size)
            assertTrue("$key must remain localizable", matching.single().attributes.getNamedItem("translatable") == null)
            assertTrue("$key must not suppress missing translations", matching.single().attributes.getNamedItem("tools:ignore") == null)
        }

        localeDirectories.forEach { directory ->
            val nodes = directory.listFiles().orEmpty()
                .filter { it.isFile && it.extension == "xml" }
                .flatMap(::stringNodes)
            val permissionNodes = nodes.filter {
                it.attributes.getNamedItem("name")?.nodeValue in keys
            }
            val presentKeys = permissionNodes.map {
                it.attributes.getNamedItem("name").nodeValue
            }.toSet()
            assertEquals("${directory.name}: Permission UX keys", keys, presentKeys)
            keys.forEach { key ->
                val matching = permissionNodes.filter {
                    it.attributes.getNamedItem("name").nodeValue == key
                }
                assertEquals("${directory.name}: $key count", 1, matching.size)
                assertTrue("${directory.name}: $key must not be blank", matching.single().textContent.isNotBlank())
                if (key == "permission_overlay_body") {
                    assertTrue("${directory.name}: overlay instruction must name Puzru", matching.single().textContent.contains("Puzru"))
                    assertFalse("${directory.name}: overlay body must be localized", matching.single().textContent == defaultOverlayBody)
                }
                assertEquals(
                    "${directory.name}: $key placeholders",
                    placeholders(defaultNodes.single { it.attributes.getNamedItem("name").nodeValue == key }.textContent),
                    placeholders(matching.single().textContent),
                )
            }
        }
    }

    private fun stringNodes(file: File) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
    }.newDocumentBuilder()
        .parse(file).getElementsByTagName("string").let { nodes ->
            (0 until nodes.length).map(nodes::item)
        }

    private fun placeholders(value: String) =
        Regex("%(?:\\d+\\$)?[a-zA-Z]").findAll(value).map { it.value }.toList()

    private companion object {
        val permissionUxKeys = setOf(
            "permission_notification_title",
            "permission_notification_body",
            "permission_notification_allow",
            "permission_notification_continue",
            "permission_overlay_title",
            "permission_overlay_body",
            "permission_overlay_open_settings",
            "permission_overlay_not_now",
        )
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
