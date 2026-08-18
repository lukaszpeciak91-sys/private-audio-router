package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer7BProximityPreferenceContractTest {
    @Test
    fun featurePreferenceExtendsTheSingleEligibilityDecision() {
        assertFalse(proximityEligible(false, true, PrivateAudioState.ACTIVE, MODE, EARPIECE, true))
        assertTrue(proximityEligible(true, true, PrivateAudioState.ACTIVE, MODE, EARPIECE, true))
        listOf("Built-in speaker", "Bluetooth", "Wired/USB", null).forEach { route ->
            assertFalse(proximityEligible(true, true, PrivateAudioState.ACTIVE, MODE, route, true))
        }
        assertEquals(1, service.method("private fun syncProximityBehavior(").occurrences("proximityEligible("))
    }

    @Test
    fun serviceOwnsDefaultPersistenceAndImmediateIdempotentSynchronization() {
        assertTrue(service.contains("var isProximityFeatureEnabled by mutableStateOf(true)"))
        assertTrue(service.contains(".getBoolean(PROXIMITY_FEATURE_KEY, true)"))
        val setter = service.method("fun setProximityFeatureEnabled(")
        assertTrue(setter.contains("if (enabled == isProximityFeatureEnabled) return"))
        assertInOrder(setter, "isProximityFeatureEnabled = enabled", ".putBoolean(", "syncProximityBehavior(")
        assertTrue(setter.contains("\"Preference disabled\""))
        assertFalse(main.contains("getSharedPreferences"))
        assertFalse(settings.contains("SharedPreferences"))
        assertFalse(helper.contains("SharedPreferences"))
    }

    @Test
    fun preferenceChangesOnlyProximityAndPreserveProtectedAudioContracts() {
        val setter = service.method("fun setProximityFeatureEnabled(")
        listOf("PrivateAudioState", "setCommunicationDevice(", "clearCommunicationDevice(",
            "AudioManager.mode", "MODE_IN_COMMUNICATION", "AudioTrack", "observer.enableController",
            "observer.disableController").forEach { assertFalse(it, setter.contains(it)) }
        assertTrue(service.method("private fun syncProximityBehavior(").contains("isProximityFeatureEnabled"))
        assertTrue(service.contains("Feature enabled: ${'$'}isProximityFeatureEnabled"))
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertEquals(1, observer.occurrences("clearCommunicationDevice("))
        assertEquals(2, observer.occurrences("audioManager.mode ="))
    }

    @Test
    fun uiDelegatesAccessibleSwitchWithoutOwningMechanicsOrFloatingChanges() {
        assertTrue(main.contains("connectedService?.setProximityFeatureEnabled(it)"))
        assertTrue(settings.contains("role = Role.Switch"))
        assertTrue(settings.contains(".toggleable("))
        assertTrue(settings.contains("onCheckedChange = null"))
        assertTrue(settings.contains("SettingsLayout.rowHeight"))
        assertFalse(main.contains("ProximityScreenController"))
        assertFalse(overlay.contains("ProximityScreenController"))
        assertFalse(overlay.contains("setProximityFeatureEnabled"))
        assertTrue(overlay.contains("ACTION_HIDE"))
        assertTrue(overlay.contains("ACTION_EXPAND"))
    }

    private fun String.occurrences(needle: String) = windowed(needle.length).count { it == needle }
    private fun String.method(signature: String): String {
        val start = indexOf(signature).also { check(it >= 0) { "Missing $signature" } }
        val opening = indexOf('{', start)
        var depth = 0
        for (index in opening until length) when (this[index]) {
            '{' -> depth++
            '}' -> if (--depth == 0) return substring(start, index + 1)
        }
        error("Unterminated $signature")
    }
    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { previous = source.indexOf(it).also { index -> assertTrue(index > previous) } }
    }
    private companion object {
        const val MODE = "MODE_IN_COMMUNICATION"
        const val EARPIECE = "Built-in earpiece"
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val helper = source("app/src/main/java/app/privateaudio/ProximityScreenController.kt")
        val main = source("app/src/main/java/app/privateaudio/MainActivity.kt")
        val settings = source("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt")
        val overlay = source("app/src/main/java/app/privateaudio/overlay/OverlayService.kt")
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val productionSources = File(root, "app/src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.toList()
    }
}
