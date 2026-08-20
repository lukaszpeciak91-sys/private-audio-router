package app.privateaudio

import android.graphics.Paint
import android.graphics.Typeface
import android.content.res.Configuration
import android.os.LocaleList
import android.text.TextPaint
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class MiniStatusMeasurementTest {
    @Test fun aliasesResolveThroughTheCurrentLocaleRatherThanEnglish() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val swedish = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("sv")))
        })
        assertEquals("Väntar", swedish.getString(R.string.state_waiting_mini))

        val tamil = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("ta")))
        })
        assertEquals("காத்திருப்பு", tamil.getString(R.string.state_waiting_mini))

        val gurmukhiPunjabi = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("pa-Guru-IN")))
        })
        assertEquals("ਉਡੀਕ", gurmukhiPunjabi.getString(R.string.state_waiting_mini))

        val shahmukhiPunjabi = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("pa-Arab-PK")))
        })
        assertEquals("اُڈیک", shahmukhiPunjabi.getString(R.string.state_waiting_mini))
    }

    @Test fun approvedTamilWaitingHasNonEllipsisMarginInSharedSlot() {
        val currentMiniPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val measuredWidth = currentMiniPaint.measureText("காத்திருப்பு")
        assertTrue("Tamil Mini WAITING measured $measuredWidth in the 100-unit slot", measuredWidth <= 96f)
    }
}
