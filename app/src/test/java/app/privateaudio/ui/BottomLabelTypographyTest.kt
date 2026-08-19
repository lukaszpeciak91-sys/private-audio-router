package app.privateaudio.ui

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomLabelTypographyTest {
    @Test
    fun ordinaryLatinLabelRetainsExistingBaseline() {
        assertEquals(13.sp, bottomLabelInitialFontSize("Settings"))
        assertFalse("Settings".usesArabicScript())
    }

    @Test
    fun ArabicScriptLabelsReceiveOpticalBaselineAcrossExistingLocales() {
        listOf(
            "الإعدادات",
            "تنظیمات",
            "سیٹنگز",
        ).forEach { label ->
            assertTrue(label.usesArabicScript())
            assertEquals(14.sp, bottomLabelInitialFontSize(label))
        }
    }

    @Test
    fun commonCharactersAndMixedLatinTextDoNotHideArabicScript() {
        assertTrue("AI، ترتیبات 2".usesArabicScript())
        assertEquals(14.sp, bottomLabelInitialFontSize("AI، ترتیبات 2"))
    }

    @Test
    fun directionAloneDoesNotTriggerArabicScriptCompensation() {
        assertFalse("123 — AI".usesArabicScript())
        assertEquals(13.sp, bottomLabelInitialFontSize("123 — AI"))
    }
}
