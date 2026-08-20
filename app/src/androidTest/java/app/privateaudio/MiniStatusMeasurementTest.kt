package app.privateaudio

import android.graphics.Paint
import android.graphics.Typeface
import android.content.res.Configuration
import android.os.LocaleList
import android.text.TextPaint
import android.view.View
import app.privateaudio.overlay.MINI_STATUS_NON_ELLIPSIS_WIDTH
import app.privateaudio.overlay.selectMiniStatusTextSize
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

    @Test fun reviewedPunjabiParadigmsResolveAndFitTheSharedSlot() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        mapOf(
            "pa-Guru-IN" to listOf("ਤਿਆਰ", "ਉਡੀਕ", "ਸਰਗਰਮ", "ਤਰੁੱਟੀ"),
            "pa-Arab-PK" to listOf("تیار", "اُڈیک", "فعال", "خرابی"),
        ).forEach { (languageTag, expectedStates) ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            val resolvedStates = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(expectedStates, resolvedStates)
            resolvedStates.forEach { state ->
                val measuredWidth = paint.measureText(state)
                assertTrue("$languageTag Mini state '$state' measured $measuredWidth in the 100-unit slot", measuredWidth <= 100f)
            }
        }
    }

    @Test fun resolvedLocaleParadigmSelectsOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        listOf("en", "ta", "gu", "ml", "pa-Guru-IN", "pa-Arab-PK", "ps", "ha", "am", "zu").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            assertTrue(selected in listOf(16f, 15f, 14f))
            paint.textSize = selected
            if (selected > 14f) {
                assertTrue(labels.all { paint.measureText(it) <= MINI_STATUS_NON_ELLIPSIS_WIDTH })
            }
        }
    }

    @Test fun hausaResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("ha")))
        })
        assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("A shirye", "Ana jira", "Ana aiki", "Kuskure"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Hausa Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

    @Test fun pashtoResolvesItsFullRtlParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("ps")))
        })
        assertEquals(View.LAYOUT_DIRECTION_RTL, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("چمتو", "په تمه", "فعال", "تېروتنه"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Pashto Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

    @Test fun malayalamResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("ml")))
        })
        assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("തയ്യാറാണ്", "കാത്തിരിക്കുന്നു", "സജീവം", "പിശക്"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Malayalam Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

    @Test fun amharicResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("am")))
        })
        assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("ዝግጁ", "በመጠበቅ ላይ", "ገባሪ", "ስህተት"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Amharic Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

    @Test fun zuluResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("zu")))
        })
        assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("Ilungile", "Iyalinda", "Iyasebenza", "Iphutha"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Zulu Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

}
