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

    @Test fun permissionCopyFollowsTheAtomicStagedLocalizationRollout() {
        val keys = PermissionUxLocalizationRollout.keys
        val completedDirectories = PermissionUxLocalizationRollout.completedLocaleDirectories
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

        val localeDirectoryNames = localeDirectories.map { it.name }.toSet()
        assertTrue(
            "completed Permission UX locales must be supported resource directories: " +
                (completedDirectories - localeDirectoryNames),
            localeDirectoryNames.containsAll(completedDirectories),
        )
        val actualCompletedDirectories = mutableSetOf<String>()
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
            assertTrue(
                "${directory.name}: Permission UX must be an atomic all-eight-or-none bundle; found $presentKeys",
                presentKeys.isEmpty() || presentKeys == keys,
            )

            if (presentKeys == keys) actualCompletedDirectories += directory.name

            if (directory.name in completedDirectories) {
                keys.forEach { key ->
                    val matching = permissionNodes.filter {
                        it.attributes.getNamedItem("name").nodeValue == key
                    }
                    assertEquals("${directory.name}: $key count", 1, matching.size)
                    assertTrue("${directory.name}: $key must not be blank", matching.single().textContent.isNotBlank())
                    assertEquals(
                        "${directory.name}: $key placeholders",
                        placeholders(defaultNodes.single { it.attributes.getNamedItem("name").nodeValue == key }.textContent),
                        placeholders(matching.single().textContent),
                    )
                }
            } else {
                assertTrue(
                    "${directory.name}: pending locale must use intentional English fallback",
                    presentKeys.isEmpty(),
                )
            }
        }
        assertEquals("Permission UX completed locale classification", completedDirectories, actualCompletedDirectories)
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
