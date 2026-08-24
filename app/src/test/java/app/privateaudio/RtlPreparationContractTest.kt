package app.privateaudio

import app.privateaudio.overlay.miniDirectionalX
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RtlPreparationContractTest {
    @Test
    fun settingsMirrorsOnlyDirectionalChevrons() {
        val copyIcon = settings.substringAfter("private fun CopyIcon").substringBefore("private fun ForwardChevron")
        val forwardChevron = settings.substringAfter("private fun ForwardChevron").substringBefore("private fun BackChevron")
        val backChevron = settings.substringAfter("private fun BackChevron").substringBefore("private fun androidx.compose")
        assertTrue(settings.contains("layoutDirection == LayoutDirection.Ltr"))
        assertTrue(forwardChevron.contains("directionalX"))
        assertTrue(backChevron.contains("directionalX"))
        assertTrue(productScreen.contains("LocalLayoutDirection provides presentationDirection"))
        assertFalse(copyIcon.contains("directionalX"))
    }

    @Test
    fun overlayUsesPhysicalLeftOriginCoordinatesInEveryLayoutDirection() {
        assertTrue(overlay.contains("gravity = Gravity.TOP or Gravity.LEFT"))
        assertFalse(overlay.contains("Gravity.START"))
        assertTrue(overlay.contains("startWindowX + dx.toInt()"))
        assertTrue(position.contains("x.coerceIn(0, (screenWidth - overlayWidth).coerceAtLeast(0))"))
    }

    @Test
    fun overlayStatusGeometryAndBidiDirectionFollowLayoutDirection() {
        val statusRenderer = overlay.kotlinDeclaration("private fun drawStatusLabel")
        assertTrue(statusRenderer.contains("StaticLayout.Builder.obtain"))
        assertTrue(statusRenderer.contains("TextDirectionHeuristics.FIRSTSTRONG_RTL"))
        assertTrue(statusRenderer.contains("TextDirectionHeuristics.FIRSTSTRONG_LTR"))
        assertTrue(statusRenderer.contains("setMaxLines(1)"))
        assertTrue(statusRenderer.contains("setEllipsize(TextUtils.TruncateAt.END)"))
        assertTrue(statusRenderer.contains("STATUS_TEXT_WIDTH"))
        assertFalse(overlay.contains("canvas.drawText(stateLabel(state)"))
        assertTrue(overlay.contains("STATUS_TEXT_LEFT = 34f"))
        assertTrue(overlay.contains("STATUS_TEXT_RIGHT = 134f"))
        assertTrue(overlay.contains("STATUS_DOT_X = 20f"))
        assertTrue(overlay.contains("miniDirectionalX(ltrX, DESIGN_WIDTH, isRtlLayout())"))
        assertEquals(34f, miniDirectionalX(34f, 300f, rtl = false))
        assertEquals(266f, miniDirectionalX(34f, 300f, rtl = true))
        assertTrue(overlay.contains("directionalX(STATUS_DOT_X)"))
        assertTrue(overlay.contains("directionalX(STATUS_TEXT_RIGHT)"))
        assertTrue(overlay.contains("RectF(134f, 15f, 166f, 47f)"))
    }

    @Test
    fun miniDirectionUsesTheLogicalApplicationLocaleAndFutureRtlSafePlatformResolution() {
        assertTrue(overlay.contains("private fun isRtlLayout() = rtlLayout"))
        assertTrue(overlay.contains("rtlLayout = miniLayoutDirection(context) == View.LAYOUT_DIRECTION_RTL"))
        assertTrue(overlay.kotlinDeclaration("fun refreshLocalizedPresentation").contains("rtlLayout = miniLayoutDirection(context)"))
        assertTrue(layoutDirection.kotlinDeclaration("internal fun miniLayoutDirection(").contains("AppLanguagePreferences.presentationLayoutDirection(context)"))
        assertTrue(productScreen.kotlinDeclaration("fun PrivateAudioScreen(").contains("AppLanguagePreferences.presentationLayoutDirection(context)"))
        assertTrue(languagePreferences.contains("TextUtils.getLayoutDirectionFromLocale(effectivePresentationLocale(context))"))
        listOf("\"yi\"", "\"he\"", "\"ar\"", "\"ur\"", "\"fa\"").forEach {
            assertFalse(presentationKotlin.contains(it))
        }

        listOf("values", "values-pl", "values-ar", "values-ur").forEach {
            assertTrue(File(root, "app/src/main/res/$it/strings.xml").isFile)
        }
    }

    @Test
    fun localeRefreshRepostsPresentationWithoutRestartingController() {
        val configurationChange = service.method("override fun onConfigurationChanged")
        assertTrue(configurationChange.contains("foregroundNotificationActive"))
        assertTrue(configurationChange.contains("buildForegroundNotification()"))
        assertTrue(configurationChange.contains(".notify("))
        assertFalse(configurationChange.contains("enterForeground()"))
        assertFalse(configurationChange.contains("enableController()"))
        assertFalse(configurationChange.contains("stopForeground"))

        val overlayConfigurationChange = overlay.method("override fun onConfigurationChanged")
        assertTrue(overlayConfigurationChange.contains("refreshLocalizedPresentation()"))
        assertFalse(overlayConfigurationChange.contains("hideOverlay()"))
    }

    private fun String.method(signature: String): String =
        substring(indexOf(signature)).substringBefore("\n    }")

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val settings = projectFile("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt").readText()
        val productScreen = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val overlay = projectFile("app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val layoutDirection = projectFile("app/src/main/java/app/privateaudio/overlay/MiniLayoutDirection.kt").readText()
        val presentationKotlin = listOf(productScreen, settings, overlay, layoutDirection).joinToString("\n")
        val position = projectFile("app/src/main/java/app/privateaudio/overlay/OverlayPosition.kt").readText()
        val service = projectFile("app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val languagePreferences = projectFile("app/src/main/java/app/privateaudio/localization/AppLanguagePreferences.kt").readText()

        fun projectFile(relativePath: String) = File(root, relativePath)
    }
}
