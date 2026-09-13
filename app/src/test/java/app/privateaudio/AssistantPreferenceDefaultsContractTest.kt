package app.privateaudio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssistantPreferenceDefaultsContractTest {
    @Test
    fun productionFallbacksDefaultBothAssistantExperimentsOn() {
        assertTrue(service.contains("getBoolean(ASSISTANT_EARLY_ROUTE_KEY, true)"))
        assertTrue(service.contains("getBoolean(ASSISTANT_SESSION_CONTINUITY_KEY, true)"))
        assertTrue(mainActivity.contains("connectedService?.isAssistantEarlyRouteEnabled ?: true"))
        assertTrue(mainActivity.contains("connectedService?.isAssistantSessionContinuityEnabled ?: true"))
        assertTrue(screen.contains("assistantEarlyRouteEnabled: Boolean = true"))
        assertTrue(screen.contains("assistantSessionContinuityEnabled: Boolean = true"))
        assertTrue(settings.contains("assistantEarlyRouteEnabled: Boolean,"))
        assertTrue(settings.contains("assistantSessionContinuityEnabled: Boolean = true"))
    }

    @Test
    fun keysAndExplicitFalsePersistenceRemainSupported() {
        assertTrue(service.contains("ASSISTANT_EARLY_ROUTE_KEY = \"assistant_early_route_enabled\""))
        assertTrue(service.contains("ASSISTANT_SESSION_CONTINUITY_KEY = \"assistant_session_continuity_enabled\""))
        assertTrue(service.contains("putBoolean(ASSISTANT_EARLY_ROUTE_KEY, enabled)"))
        assertTrue(service.contains("putBoolean(ASSISTANT_SESSION_CONTINUITY_KEY, enabled)"))
        assertFalse(service.contains("putBoolean(ASSISTANT_EARLY_ROUTE_KEY, true)"))
        assertFalse(service.contains("putBoolean(ASSISTANT_SESSION_CONTINUITY_KEY, true)"))
    }

    @Test
    fun startupReadsDefaultsWithoutWritingThem() {
        val startup = service.kotlinDeclaration("override fun onCreate()")
        assertTrue(startup.contains("getBoolean(ASSISTANT_EARLY_ROUTE_KEY, true)"))
        assertTrue(startup.contains("getBoolean(ASSISTANT_SESSION_CONTINUITY_KEY, true)"))
        assertFalse(startup.contains("putBoolean("))
        assertFalse(startup.contains(".edit()"))
    }

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val mainActivity = source("app/src/main/java/app/privateaudio/MainActivity.kt")
        val screen = source("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt")
        val settings = source("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt")
    }
}
