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
        listOf("en", "ta", "gu", "ml", "pa-Guru-IN", "pa-Arab-PK", "ps", "ha", "am", "zu", "so", "ne", "ne-NP", "ne-Deva-NP", "hy", "hy-AM", "hy-Armn-AM", "or", "my", "my-MM", "my-Mymr-MM", "uz", "uz-UZ", "uz-Latn-UZ", "km", "km-KH", "km-Khmr-KH", "as", "as-IN", "as-Beng-IN", "ca", "ca-ES", "ca-Latn-ES", "kk", "kk-KZ", "kk-Cyrl-KZ", "mn", "mn-MN", "mn-Cyrl-MN").forEach { languageTag ->
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

    @Test fun mongolianResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("mn", "mn-MN", "mn-Cyrl-MN").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("Бэлэн", "Хүлээж байна", "Идэвхтэй", "Алдаа"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Mongolian Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun azerbaijaniVariantsResolveIndependentParadigmsAndUseMeasuredProductionSizes() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf(
            Triple(listOf("az", "az-AZ", "az-Latn-AZ"), listOf("Hazır", "Gözləyir", "Aktiv", "Xəta"), View.LAYOUT_DIRECTION_LTR),
            Triple(listOf("az-IR", "az-Arab", "az-Arab-IR"), listOf("حاضیر", "گؤزله‌ییر", "فعال", "خطا"), View.LAYOUT_DIRECTION_RTL),
        ).forEach { (tags, expectedLabels, expectedDirection) ->
            tags.forEach { languageTag ->
                val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply { setLocales(LocaleList(Locale.forLanguageTag(languageTag))) })
                assertEquals(expectedDirection, localized.resources.configuration.layoutDirection)
                val labels = listOf(localized.getString(R.string.state_ready_mini), localized.getString(R.string.state_waiting_mini), localized.getString(R.string.state_active_mini), localized.getString(R.string.state_error_mini))
                assertEquals(expectedLabels, labels)
                val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif", Typeface.NORMAL) }
                val selected = selectMiniStatusTextSize(labels) { label, textSize -> paint.textSize = textSize; paint.measureText(label) }
                println("$languageTag Azerbaijani Mini production-equivalent common size: $selected")
                assertTrue(selected in listOf(16f, 15f, 14f))
            }
        }
    }

    @Test fun burmeseResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("my", "my-MM", "my-Mymr-MM").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("အဆင်သင့်", "စောင့်နေသည်", "အသုံးပြုနေသည်", "အမှား"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Burmese Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun uzbekResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("uz", "uz-UZ", "uz-Latn-UZ").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("Tayyor", "Kutilmoqda", "Faol", "Xatolik"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Uzbek Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun khmerResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("km", "km-KH", "km-Khmr-KH").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("ត្រៀមរួចរាល់", "កំពុងរង់ចាំ", "សកម្ម", "កំហុស"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Khmer Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun assameseResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("as", "as-IN", "as-Beng-IN").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("সাজু", "অপেক্ষাৰত", "সক্ৰিয়", "ত্ৰুটি"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Assamese Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun somaliResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("so")))
        })
        assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("Diyaar", "Sugaya", "Firfircoon", "Khalad"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Somali Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

    @Test fun catalanResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("ca", "ca-ES", "ca-Latn-ES").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("A punt", "En espera", "Actiu", "Error"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Catalan Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun galicianResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("gl", "gl-ES", "gl-Latn-ES").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("Preparado", "En espera", "Activo", "Erro"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Galician Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun nepaliResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("ne", "ne-NP", "ne-Deva-NP").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("तयार", "पर्खँदै", "सक्रिय", "त्रुटि"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Nepali Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun armenianResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("hy", "hy-AM", "hy-Armn-AM").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("Պատրաստ է", "Սպասում է", "Ակտիվ", "Սխալ"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.create("sans-serif", Typeface.NORMAL) }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Armenian Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
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

    @Test fun odiaResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
            setLocales(LocaleList(Locale.forLanguageTag("or")))
        })
        assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
        val labels = listOf(
            localized.getString(R.string.state_ready_mini),
            localized.getString(R.string.state_waiting_mini),
            localized.getString(R.string.state_active_mini),
            localized.getString(R.string.state_error_mini),
        )
        assertEquals(listOf("ପ୍ରସ୍ତୁତ", "ଅପେକ୍ଷାରତ", "ସକ୍ରିୟ", "ତ୍ରୁଟି"), labels)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
        val selected = selectMiniStatusTextSize(labels) { label, textSize ->
            paint.textSize = textSize
            paint.measureText(label)
        }
        println("Odia Mini production-equivalent common size: $selected")
        assertTrue(selected in listOf(16f, 15f, 14f))
    }

    @Test fun kazakhResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("kk", "kk-KZ", "kk-Cyrl-KZ").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("Дайын", "Күтуде", "Белсенді", "Қате"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Kazakh Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun georgianResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("ka", "ka-GE", "ka-Geor-GE").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("მზადაა", "მოლოდინშია", "აქტიურია", "შეცდომა"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Georgian Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

    @Test fun laoResolvesItsFullLtrParadigmAndUsesOneMeasuredProductionSize() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        listOf("lo", "lo-LA", "lo-Laoo-LA").forEach { languageTag ->
            val localized = base.createConfigurationContext(Configuration(base.resources.configuration).apply {
                setLocales(LocaleList(Locale.forLanguageTag(languageTag)))
            })
            assertEquals(View.LAYOUT_DIRECTION_LTR, localized.resources.configuration.layoutDirection)
            val labels = listOf(
                localized.getString(R.string.state_ready_mini),
                localized.getString(R.string.state_waiting_mini),
                localized.getString(R.string.state_active_mini),
                localized.getString(R.string.state_error_mini),
            )
            assertEquals(listOf("ພ້ອມ", "ກຳລັງລໍຖ້າ", "ກຳລັງໃຊ້ງານ", "ຂໍ້ຜິດພາດ"), labels)
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            }
            val selected = selectMiniStatusTextSize(labels) { label, textSize ->
                paint.textSize = textSize
                paint.measureText(label)
            }
            println("$languageTag Lao Mini production-equivalent common size: $selected")
            assertTrue(selected in listOf(16f, 15f, 14f))
        }
    }

}
