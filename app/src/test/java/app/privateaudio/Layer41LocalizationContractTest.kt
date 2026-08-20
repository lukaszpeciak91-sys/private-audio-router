package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class Layer41LocalizationContractTest {
    @Test
    fun standardAndroidLocaleConfigurationUsesEnglishDefaultResources() {
        assertTrue(appBuildSource.contains("generateLocaleConfig = true"))
        assertEquals("unqualifiedResLocale=en-US", resourcesProperties.trim())
        assertTrue(defaultStrings.contains("name=\"settings_system_default\">Default</string>"))
        assertFalse(defaultStrings.contains("translatable=\"false\""))
        localeDirectories.forEach { localeDirectory ->
            val localeStrings = File(localeDirectory, "strings.xml").readText()
            assertEquals(
                localeDirectory.name,
                stringKeys(defaultStrings).filterNot { it == "settings_fake_phone_pre_arm" },
                stringKeys(localeStrings),
            )
            assertEquals(
                localeDirectory.name,
                placeholders(defaultStrings) - "settings_fake_phone_pre_arm",
                placeholders(localeStrings),
            )
            assertFalse(localeDirectory.name, localeStrings.contains("settings_fake_phone_pre_arm"))
        }
        assertTrue(defaultStrings.contains("name=\"settings_fake_phone_pre_arm\">Fake Phone pre-arm</string>"))
        assertEquals("Melayu", nativeLocaleName("ms"))
        assertTrue(malayStrings.contains("name=\"routing_notification_text\">Menunggu audio dialihkan</string>"))
        assertTrue(malayStrings.contains("name=\"settings_language_android_13_required\"") && malayStrings.contains("bahasa sistem"))
        assertTrue(malayStrings.contains("name=\"settings_proximity_screen\">Matikan skrin berhampiran telinga anda</string>"))
        assertTrue(malayStrings.contains("name=\"settings_about_body\">Private Audio membantu mengalihkan audio suara yang disokong ke alat dengar terbina dalam telefon anda.</string>"))
        assertTrue(malayStrings.contains("hidupkan, kembangkan dan tutup"))
        assertEquals("Italiano", nativeLocaleName("it"))
        assertTrue(italianStrings.contains("name=\"routing_notification_title\">Private Audio è attivato</string>"))
        assertTrue(italianStrings.contains("name=\"state_active\">Attivo</string>"))
        assertFalse(italianStrings.contains("name=\"routing_notification_title\">Private Audio è attivo</string>"))
        assertTrue(italianStrings.contains("name=\"routing_notification_text\">In attesa di reindirizzare l\\'audio</string>"))
        assertTrue(italianStrings.contains("name=\"settings_language_android_13_required\"") && italianStrings.contains("lingua di sistema"))
        assertTrue(italianStrings.contains("name=\"settings_language_body\"") && italianStrings.contains("lingua del dispositivo"))
        assertTrue(italianStrings.contains("name=\"settings_proximity_screen\">Spegni lo schermo vicino all\\'orecchio</string>"))
        assertTrue(italianStrings.contains("altoparlante auricolare integrato"))
        assertTrue(italianStrings.contains("attivare, espandere e chiudere"))
        assertEquals("Română", nativeLocaleName("ro"))
        assertTrue(romanianStrings.contains("name=\"routing_notification_title\">Private Audio este activat</string>"))
        assertTrue(romanianStrings.contains("name=\"state_active\">Activ</string>"))
        assertFalse(romanianStrings.contains("name=\"routing_notification_title\">Private Audio este activ</string>"))
        assertTrue(romanianStrings.contains("name=\"routing_notification_text\">Se așteaptă redirecționarea sunetului</string>"))
        assertTrue(romanianStrings.contains("name=\"settings_language_android_13_required\"") && romanianStrings.contains("limba sistemului"))
        assertTrue(romanianStrings.contains("name=\"settings_language_body\"") && romanianStrings.contains("limba dispozitivului"))
        assertTrue(romanianStrings.contains("name=\"settings_proximity_screen\">Oprește ecranul lângă ureche</string>"))
        assertTrue(romanianStrings.contains("casca încorporată a telefonului"))
        assertTrue(romanianStrings.contains("activare, extindere și închidere"))
        assertEquals("Svenska", nativeLocaleName("sv"))
        assertTrue(swedishStrings.contains("name=\"routing_notification_text\">Väntar på att växla ljud</string>"))
        assertTrue(swedishStrings.contains("name=\"settings_language_android_13_required\"") && swedishStrings.contains("systemspråket"))
        assertTrue(swedishStrings.contains("name=\"settings_language_body\"") && swedishStrings.contains("enhetens språk"))
        assertTrue(swedishStrings.contains("name=\"settings_proximity_screen\">Stäng av skärmen nära örat</string>"))
        assertTrue(swedishStrings.contains("telefonens inbyggda samtalslur"))
        assertTrue(swedishStrings.contains("slå på, expandera och stänga"))
        assertTrue(swedishStrings.contains("name=\"state_ready\">Redo</string>"))
        assertTrue(swedishStrings.contains("name=\"state_waiting\">Väntar</string>"))
        assertTrue(swedishStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertTrue(swedishStrings.contains("name=\"state_error\">Fel</string>"))
        assertEquals("Norsk bokmål", nativeLocaleName("nb"))
        assertTrue(norwegianBokmalStrings.contains("name=\"routing_notification_text\">Venter på å bytte lyd</string>"))
        assertTrue(norwegianBokmalStrings.contains("name=\"settings_language_android_13_required\"") && norwegianBokmalStrings.contains("systemspråket"))
        assertTrue(norwegianBokmalStrings.contains("name=\"settings_language_body\"") && norwegianBokmalStrings.contains("språket på enheten"))
        assertTrue(norwegianBokmalStrings.contains("name=\"settings_proximity_screen\">Slå av skjermen nær øret</string>"))
        assertTrue(norwegianBokmalStrings.contains("telefonens innebygde ørehøyttaler"))
        assertTrue(norwegianBokmalStrings.contains("slå på, utvide og lukke"))
        assertTrue(norwegianBokmalStrings.contains("name=\"state_ready\">Klar</string>"))
        assertTrue(norwegianBokmalStrings.contains("name=\"state_waiting\">Venter</string>"))
        assertTrue(norwegianBokmalStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertTrue(norwegianBokmalStrings.contains("name=\"state_error\">Feil</string>"))
        assertEquals("Dansk", nativeLocaleName("da"))
        assertTrue(danishStrings.contains("name=\"routing_notification_text\">Venter på at skifte lyd</string>"))
        assertTrue(danishStrings.contains("name=\"settings_language_android_13_required\"") && danishStrings.contains("systemsproget"))
        assertTrue(danishStrings.contains("name=\"settings_language_body\"") && danishStrings.contains("enhedens sprog"))
        assertTrue(danishStrings.contains("name=\"settings_proximity_screen\">Sluk skærmen tæt på øret</string>"))
        assertTrue(danishStrings.contains("telefonens indbyggede ørestykke"))
        assertTrue(danishStrings.contains("tænde, udvide og lukke"))
        assertTrue(danishStrings.contains("name=\"state_ready\">Klar</string>"))
        assertTrue(danishStrings.contains("name=\"state_waiting\">Venter</string>"))
        assertTrue(danishStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertTrue(danishStrings.contains("name=\"state_error\">Fejl</string>"))
        assertEquals("Íslenska", nativeLocaleName("is"))
        assertTrue(icelandicStrings.contains("name=\"routing_notification_text\">Bíður eftir að skipta um hljóðúttak</string>"))
        assertTrue(icelandicStrings.contains("name=\"settings_language_android_13_required\"") && icelandicStrings.contains("tungumáli kerfisins"))
        assertTrue(icelandicStrings.contains("name=\"settings_language_body\"") && icelandicStrings.contains("tungumáli tækisins"))
        assertTrue(icelandicStrings.contains("name=\"settings_proximity_screen\">Slökkva á skjánum nálægt eyranu</string>"))
        assertTrue(icelandicStrings.contains("innbyggða símahátalarann"))
        assertTrue(icelandicStrings.contains("kveikja, stækka og loka"))
        assertTrue(icelandicStrings.contains("name=\"state_ready\">Tilbúið</string>"))
        assertTrue(icelandicStrings.contains("name=\"state_waiting\">Bíður</string>"))
        assertTrue(icelandicStrings.contains("name=\"state_active\">Virkt</string>"))
        assertTrue(icelandicStrings.contains("name=\"state_error\">Villa</string>"))
        assertEquals("Føroyskt", nativeLocaleName("fo"))
        assertTrue(faroeseStrings.contains("name=\"routing_notification_text\">Bíðar eftir at skifta ljóðið</string>"))
        assertTrue(faroeseStrings.contains("name=\"settings_language_android_13_required\"") && faroeseStrings.contains("skipanarmálinum"))
        assertTrue(faroeseStrings.contains("name=\"settings_language_body\"") && faroeseStrings.contains("málinum á tólinum"))
        assertTrue(faroeseStrings.contains("name=\"settings_proximity_screen\">Sløkk skíggjan nær oyranum</string>"))
        assertTrue(faroeseStrings.contains("innbygda hátalaran ovast á telefonini, sum verður brúktur við oyrað undir telefonsamrøðum"))
        assertTrue(faroeseStrings.contains("tendra, víðka og lata aftur"))
        assertTrue(faroeseStrings.contains("name=\"state_ready\">Klárt</string>"))
        assertTrue(faroeseStrings.contains("name=\"state_waiting\">Bíðar</string>"))
        assertTrue(faroeseStrings.contains("name=\"state_active\">Virkið</string>"))
        assertTrue(faroeseStrings.contains("name=\"state_error\">Villa</string>"))
        assertEquals("ಕನ್ನಡ", nativeLocaleName("kn"))
        assertTrue(kannadaStrings.contains("name=\"routing_notification_text\">ಆಡಿಯೊ ಸ್ವಿಚ್ ಆಗಲು ಕಾಯುತ್ತಿದೆ</string>"))
        assertTrue(kannadaStrings.contains("name=\"settings_language_android_13_required\"") && kannadaStrings.contains("ಸಿಸ್ಟಮ್ ಭಾಷೆ"))
        assertTrue(kannadaStrings.contains("name=\"settings_proximity_screen\">ಕಿವಿಯ ಬಳಿ ಸ್ಕ್ರೀನ್ ಆಫ್ ಮಾಡಿ</string>"))
        assertTrue(kannadaStrings.contains("name=\"settings_about_body\">Private Audio ಆಡಿಯೊವನ್ನು ಫೋನ್‌ನ ಇಯರ್‌ಪೀಸ್‌ಗೆ ಸ್ವಿಚ್ ಮಾಡಲು ಸಹಾಯ ಮಾಡುತ್ತದೆ.</string>"))
        assertTrue(kannadaStrings.contains("ಆನ್ ಮಾಡಲು, ವಿಸ್ತರಿಸಲು ಮತ್ತು ಮುಚ್ಚಲು"))
        assertTrue(kannadaStrings.filterNot { it.isWhitespace() }.any { it in '\u0C80'..'\u0CFF' })
        assertEquals("ગુજરાતી", nativeLocaleName("gu"))
        assertTrue(gujaratiStrings.contains("name=\"routing_notification_text\">ઑડિયો સ્વિચ થવાની રાહ જોઈ રહ્યું છે</string>"))
        assertTrue(gujaratiStrings.contains("name=\"state_waiting\">રાહ જોઈ રહ્યું છે</string>"))
        assertTrue(gujaratiStrings.contains("name=\"state_active\">સક્રિય</string>"))
        assertTrue(gujaratiStrings.contains("name=\"settings_language_android_13_required\"") && gujaratiStrings.contains("સિસ્ટમ ભાષા"))
        assertTrue(gujaratiStrings.contains("name=\"settings_proximity_screen\">કાન પાસે સ્ક્રીન બંધ કરો</string>"))
        assertTrue(gujaratiStrings.contains("name=\"settings_about_body\">Private Audio ઑડિયોને ફોનના ઇયરપીસ પર સ્વિચ કરવામાં મદદ કરે છે.</string>"))
        assertEquals("मराठी", nativeLocaleName("mr"))
        assertTrue(marathiStrings.contains("name=\"routing_notification_text\">ऑडिओ स्विच होण्याची प्रतीक्षा करत आहे</string>"))
        assertTrue(marathiStrings.contains("name=\"state_waiting\">प्रतीक्षा</string>"))
        assertTrue(marathiStrings.contains("name=\"state_active\">सक्रिय</string>"))
        assertTrue(marathiStrings.contains("name=\"settings_language_android_13_required\"") && marathiStrings.contains("सिस्टीम भाषा"))
        assertTrue(marathiStrings.contains("name=\"settings_proximity_screen\">कानाजवळ स्क्रीन बंद करा</string>"))
        assertTrue(marathiStrings.contains("name=\"settings_about_body\">Private Audio ऑडिओ फोनच्या इयरपीसवर स्विच करण्यात मदत करते.</string>"))
        assertEquals("తెలుగు", nativeLocaleName("te"))
        assertTrue(teluguStrings.contains("name=\"routing_notification_text\">ఆడియోను మార్చడానికి వేచి ఉంది</string>"))
        assertTrue(teluguStrings.contains("name=\"state_waiting\">వేచి ఉంది</string>"))
        assertTrue(teluguStrings.contains("name=\"state_active\">యాక్టివ్</string>"))
        assertTrue(teluguStrings.contains("name=\"settings_language_android_13_required\"") && teluguStrings.contains("సిస్టమ్ భాష"))
        assertTrue(teluguStrings.contains("name=\"settings_proximity_screen\">చెవి దగ్గర స్క్రీన్‌ను ఆఫ్ చేయండి</string>"))
        assertTrue(teluguStrings.contains("name=\"settings_about_body\">Private Audio ఆడియోను ఫోన్ ఇయర్‌పీస్‌కు మార్చడంలో సహాయపడుతుంది.</string>"))
        assertEquals("தமிழ்", nativeLocaleName("ta"))
        assertTrue(tamilStrings.contains("name=\"routing_notification_text\">ஆடியோவை மாற்றக் காத்திருக்கிறது</string>"))
        assertTrue(tamilStrings.contains("name=\"state_waiting\">காத்திருக்கிறது</string>"))
        assertTrue(tamilStrings.contains("name=\"state_active\">செயலில் உள்ளது</string>"))
        assertTrue(tamilStrings.contains("name=\"settings_proximity_screen\">காதுக்கு அருகில் திரையை அணை</string>"))
        assertTrue(tamilStrings.contains("name=\"settings_about_body\">தொலைபேசியின் இயர்பீஸுக்கு ஆடியோவை மாற்ற Private Audio உதவுகிறது.</string>"))
        assertEquals("ไทย", nativeLocaleName("th"))
        assertTrue(thaiStrings.contains("name=\"routing_notification_text\">กำลังรอสลับเสียง</string>"))
        assertTrue(thaiStrings.contains("name=\"product_subtitle\">คุยกับ AI แบบเป็นส่วนตัว เหมือนคุยโทรศัพท์</string>"))
        assertTrue(thaiStrings.contains("name=\"state_ready\">พร้อม</string>"))
        assertTrue(thaiStrings.contains("name=\"state_waiting\">กำลังรอ</string>"))
        assertTrue(thaiStrings.contains("name=\"state_active\">กำลังใช้งาน</string>"))
        assertTrue(thaiStrings.contains("name=\"state_error\">ข้อผิดพลาด</string>"))
        assertTrue(thaiStrings.contains("name=\"settings\">การตั้งค่า</string>"))
        assertTrue(thaiStrings.contains("name=\"settings_system_default\">ค่าเริ่มต้น</string>"))
        assertTrue(thaiStrings.contains("name=\"settings_about_body\">Private Audio ช่วยสลับเสียงไปยังหูฟังด้านบนของโทรศัพท์</string>"))
        assertEquals("日本語", nativeLocaleName("ja"))
        assertTrue(japaneseStrings.contains("name=\"routing_notification_text\">音声の切り替えを待機中</string>"))
        assertTrue(japaneseStrings.contains("name=\"state_ready\">準備完了</string>"))
        assertTrue(japaneseStrings.contains("name=\"state_waiting\">待機中</string>"))
        assertTrue(japaneseStrings.contains("name=\"state_active\">使用中</string>"))
        assertTrue(japaneseStrings.contains("name=\"state_error\">エラー</string>"))
        assertEquals("Kiswahili", nativeLocaleName("sw"))
        assertTrue(swahiliStrings.contains("name=\"routing_notification_title\">Private Audio imewashwa</string>"))
        assertTrue(swahiliStrings.contains("name=\"routing_notification_text\">Inasubiri kuelekeza sauti</string>"))
        assertTrue(swahiliStrings.contains("name=\"state_ready\">Tayari</string>"))
        assertTrue(swahiliStrings.contains("name=\"state_waiting\">Inasubiri</string>"))
        assertTrue(swahiliStrings.contains("name=\"state_active\">Inatumika</string>"))
        assertFalse(swahiliStrings.contains("name=\"state_active\">Imewashwa</string>"))
        assertTrue(swahiliStrings.contains("name=\"settings_advanced\">Mipangilio ya kina</string>"))
        assertFalse(swahiliStrings.contains("name=\"settings_advanced\">Kina</string>"))
        assertTrue(swahiliStrings.contains("name=\"state_error\">Hitilafu</string>"))
        assertEquals("Naijíriá Píjin", nativeLocaleName("pcm"))
        assertTrue(nigerianPidginStrings.contains("name=\"routing_notification_text\">Dey wait to route audio</string>"))
        assertTrue(nigerianPidginStrings.contains("name=\"state_ready\">Ready</string>"))
        assertTrue(nigerianPidginStrings.contains("name=\"state_waiting\">Dey wait</string>"))
        assertTrue(nigerianPidginStrings.contains("name=\"state_active\">Active</string>"))
        assertTrue(nigerianPidginStrings.contains("name=\"state_error\">Error</string>"))
        assertEquals("اردو", nativeLocaleName("ur"))
        assertTrue(urduStrings.contains("name=\"routing_notification_text\">آڈیو منتقل ہونے کا انتظار ہے</string>"))
        assertTrue(urduStrings.contains("name=\"state_ready\">تیار</string>"))
        assertTrue(urduStrings.contains("name=\"state_waiting\">انتظار میں</string>"))
        assertTrue(urduStrings.contains("name=\"state_active\">فعال</string>"))
        assertTrue(urduStrings.contains("name=\"state_error\">خرابی</string>"))
        assertTrue(urduStrings.contains("name=\"floating\">کمپیکٹ</string>"))
        assertTrue(urduStrings.contains("name=\"overlay_controller_description\">Private Audio کا فلوٹنگ کمپیکٹ کنٹرولر۔ اسٹیٹس: %1\$s۔ چالو کرنے، پھیلانے اور بند کرنے کے کنٹرولز۔</string>"))
        val urduMiniControllerFields = listOf("floating", "overlay_controller_description")
            .map { key -> Regex("name=\\\"$key\\\">([^<]*)</string>").find(urduStrings)?.groupValues?.get(1).orEmpty() }
        assertTrue(urduMiniControllerFields.none { it.contains("منی") })
        assertEquals("فارسی", nativeLocaleName("fa"))
        assertTrue(persianStrings.contains("name=\"routing_notification_text\">در انتظار هدایت صدا</string>"))
        assertTrue(persianStrings.contains("name=\"product_subtitle\">مثل تماس تلفنی، خصوصی با هوش مصنوعی صحبت کنید.</string>"))
        assertTrue(persianStrings.contains("name=\"state_ready\">آماده</string>"))
        assertTrue(persianStrings.contains("name=\"state_waiting\">در انتظار</string>"))
        assertTrue(persianStrings.contains("name=\"state_active\">فعال</string>"))
        assertTrue(persianStrings.contains("name=\"state_error\">خطا</string>"))
        assertTrue(persianStrings.contains("name=\"power_control\">روشن کردن</string>"))
        assertTrue(persianStrings.contains("گزارش عیب‌یابی"))
        assertFalse(persianStrings.contains('ي'))
        assertFalse(persianStrings.contains('ك'))
        assertEquals("Tiếng Việt", nativeLocaleName("vi"))
        assertTrue(vietnameseStrings.contains("name=\"routing_notification_text\">Đang chờ chuyển đổi âm thanh</string>"))
        assertTrue(vietnameseStrings.contains("name=\"product_subtitle\">Trò chuyện riêng tư với AI, như một cuộc gọi điện thoại.</string>"))
        assertTrue(vietnameseStrings.contains("name=\"state_ready\">Sẵn sàng</string>"))
        assertTrue(vietnameseStrings.contains("name=\"state_waiting\">Đang chờ</string>"))
        assertTrue(vietnameseStrings.contains("name=\"state_active\">Đang hoạt động</string>"))
        assertTrue(vietnameseStrings.contains("name=\"state_error\">Lỗi</string>"))
        assertTrue(vietnameseStrings.contains("name=\"settings\">Cài đặt</string>"))
        assertTrue(vietnameseStrings.contains("name=\"settings_system_default\">Mặc định</string>"))
        assertTrue(vietnameseStrings.contains("name=\"settings_about_body\">Private Audio giúp chuyển âm thanh sang loa trong của điện thoại.</string>"))
        assertEquals("Indonesia", nativeLocaleLanguage("id"))
        assertTrue(indonesianStrings.contains("name=\"routing_notification_text\">Menunggu pengalihan audio</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_ready\">Siap</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_waiting\">Menunggu</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_active\">Aktif</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_error\">Error</string>"))
        assertEquals("العربية", nativeLocaleName("ar"))
        assertTrue(arabicStrings.contains("name=\"routing_notification_text\">في انتظار توجيه الصوت</string>"))
        assertTrue(arabicStrings.contains("name=\"state_ready\">جاهز</string>"))
        assertTrue(arabicStrings.contains("name=\"state_waiting\">في الانتظار</string>"))
        assertTrue(arabicStrings.contains("name=\"state_active\">نشط</string>"))
        assertTrue(arabicStrings.contains("name=\"state_error\">خطأ</string>"))
        assertEquals("हिन्दी", nativeLocaleName("hi"))
        assertTrue(hindiStrings.contains("name=\"routing_notification_title\">Private Audio चालू है</string>"))
        assertTrue(hindiStrings.contains("name=\"routing_notification_text\">ऑडियो भेजे जाने का इंतज़ार है</string>"))
        assertTrue(hindiStrings.contains("name=\"state_ready\">तैयार</string>"))
        assertTrue(hindiStrings.contains("name=\"state_waiting\">इंतज़ार में</string>"))
        assertTrue(hindiStrings.contains("name=\"state_active\">सक्रिय</string>"))
        assertFalse(hindiStrings.contains("name=\"state_active\">चालू</string>"))
        assertTrue(hindiStrings.contains("name=\"state_error\">गड़बड़ी</string>"))
        assertTrue(simplifiedChineseStrings.contains("name=\"routing_notification_text\">正在等待音频重定向</string>"))
        assertTrue(simplifiedChineseStrings.contains("name=\"state_ready\">就绪</string>"))
        assertTrue(simplifiedChineseStrings.contains("name=\"state_waiting\">等待中</string>"))
        assertTrue(simplifiedChineseStrings.contains("name=\"routing_notification_title\">Private Audio 已开启</string>"))
        assertTrue(simplifiedChineseStrings.contains("name=\"state_active\">使用中</string>"))
        assertFalse(simplifiedChineseStrings.contains("name=\"state_active\">已启用</string>"))
        assertTrue(simplifiedChineseStrings.contains("name=\"state_error\">错误</string>"))
        assertTrue(traditionalChineseStrings.contains("name=\"routing_notification_text\">正在等待重新導向音訊</string>"))
        assertTrue(traditionalChineseStrings.contains("name=\"state_ready\">就緒</string>"))
        assertTrue(traditionalChineseStrings.contains("name=\"state_waiting\">等待中</string>"))
        assertTrue(traditionalChineseStrings.contains("name=\"routing_notification_title\">Private Audio 已開啟</string>"))
        assertTrue(traditionalChineseStrings.contains("name=\"state_active\">使用中</string>"))
        assertFalse(traditionalChineseStrings.contains("name=\"state_active\">已啟用</string>"))
        assertTrue(traditionalChineseStrings.contains("name=\"state_error\">錯誤</string>"))
        assertEquals("Português (Brasil)", nativeLocaleName("pt-BR"))
        assertEquals("Português (Portugal)", nativeLocaleName("pt-PT"))
        assertTrue(brazilianPortugueseStrings.contains("name=\"state_waiting\">Aguardando</string>"))
        assertTrue(brazilianPortugueseStrings.contains("name=\"settings\">Configurações</string>"))
        assertTrue(brazilianPortugueseStrings.contains("alto-falante de chamadas do telefone"))
        assertTrue(europeanPortugueseStrings.contains("name=\"state_waiting\">A aguardar</string>"))
        assertTrue(europeanPortugueseStrings.contains("name=\"settings\">Definições</string>"))
        assertTrue(europeanPortugueseStrings.contains("auricular integrado do telemóvel"))
        val spanishLocale = Locale.forLanguageTag("es")
        val spanishNativeName = spanishLocale.getDisplayName(spanishLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(spanishLocale) else first.toString()
        }
        assertEquals("Español", spanishNativeName)
        assertTrue(spanishStrings.contains("name=\"state_ready\">Listo</string>"))
        assertTrue(spanishStrings.contains("name=\"state_waiting\">Esperando</string>"))
        assertTrue(spanishStrings.contains("name=\"state_active\">Activo</string>"))
        assertTrue(spanishStrings.contains("name=\"state_error\">Error</string>"))
        val frenchLocale = Locale.forLanguageTag("fr")
        val frenchNativeName = frenchLocale.getDisplayName(frenchLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(frenchLocale) else first.toString()
        }
        assertEquals("Français", frenchNativeName)
        assertTrue(frenchStrings.contains("name=\"state_ready\">Prêt</string>"))
        assertTrue(frenchStrings.contains("name=\"state_waiting\">En attente</string>"))
        assertTrue(frenchStrings.contains("name=\"state_active\">Actif</string>"))
        assertTrue(frenchStrings.contains("name=\"state_error\">Erreur</string>"))
        val russianLocale = Locale.forLanguageTag("ru")
        val russianNativeName = russianLocale.getDisplayName(russianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(russianLocale) else first.toString()
        }
        assertEquals("Русский", russianNativeName)
        assertTrue(russianStrings.contains("name=\"state_ready\">Готово</string>"))
        assertTrue(russianStrings.contains("name=\"state_waiting\">Ожидание</string>"))
        assertTrue(russianStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(russianStrings.contains("name=\"state_error\">Ошибка</string>"))
        val lithuanianLocale = Locale.forLanguageTag("lt")
        val lithuanianNativeName = lithuanianLocale.getDisplayName(lithuanianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(lithuanianLocale) else first.toString()
        }
        assertEquals("Lietuvių", lithuanianNativeName)
        assertTrue(lithuanianStrings.contains("name=\"state_ready\">Paruošta</string>"))
        assertTrue(lithuanianStrings.contains("name=\"state_waiting\">Laukia</string>"))
        assertTrue(lithuanianStrings.contains("name=\"state_active\">Aktyvu</string>"))
        assertTrue(lithuanianStrings.contains("name=\"state_error\">Klaida</string>"))
        val belarusianLocale = Locale.forLanguageTag("be")
        val belarusianNativeName = belarusianLocale.getDisplayName(belarusianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(belarusianLocale) else first.toString()
        }
        assertEquals("Беларуская", belarusianNativeName)
        assertTrue(belarusianStrings.contains("name=\"state_ready\">Гатова</string>"))
        assertTrue(belarusianStrings.contains("name=\"state_waiting\">Чакае</string>"))
        assertTrue(belarusianStrings.contains("name=\"state_active\">Актыўна</string>"))
        assertTrue(belarusianStrings.contains("name=\"state_error\">Памылка</string>"))
        val ukrainianLocale = Locale.forLanguageTag("uk")
        val ukrainianNativeName = ukrainianLocale.getDisplayName(ukrainianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(ukrainianLocale) else first.toString()
        }
        assertEquals("Українська", ukrainianNativeName)
        assertTrue(ukrainianStrings.contains("name=\"state_ready\">Готово</string>"))
        assertTrue(ukrainianStrings.contains("name=\"state_waiting\">Очікує</string>"))
        assertTrue(ukrainianStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(ukrainianStrings.contains("name=\"state_error\">Помилка</string>"))
    }

    @Test
    fun finnishFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("Suomi", nativeLocaleName("fi"))
        assertTrue(finnishStrings.contains("name=\"routing_notification_text\">Odottaa äänen ohjaamista</string>"))
        assertTrue(finnishStrings.contains("name=\"settings_language_body\"") && finnishStrings.contains("laitteesi kieltä"))
        assertTrue(finnishStrings.contains("name=\"settings_language_android_13_required\"") && finnishStrings.contains("Tämä laite käyttää järjestelmän kieltä."))
        assertTrue(finnishStrings.contains("name=\"settings_proximity_screen\">Sammuta näyttö korvan lähellä</string>"))
        assertTrue(finnishStrings.contains("puhelimen sisäänrakennettuun kuulokkeeseen"))
        assertTrue(finnishStrings.contains("Virran kytkemisen, laajentamisen ja sulkemisen säätimet"))
        assertTrue(finnishStrings.contains("name=\"state_ready\">Valmis</string>"))
        assertTrue(finnishStrings.contains("name=\"state_waiting\">Odottaa</string>"))
        assertTrue(finnishStrings.contains("name=\"state_active\">Aktiivinen</string>"))
        assertTrue(finnishStrings.contains("name=\"state_error\">Virhe</string>"))
    }

    @Test
    fun estonianFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("Eesti", nativeLocaleName("et"))
        assertTrue(estonianStrings.contains("name=\"routing_notification_text\">Ootab heli suunamist</string>"))
        assertTrue(estonianStrings.contains("name=\"settings_language_body\"") && estonianStrings.contains("seadme keelt"))
        assertTrue(estonianStrings.contains("name=\"settings_language_android_13_required\"") && estonianStrings.contains("See seade kasutab süsteemi keelt."))
        assertTrue(estonianStrings.contains("name=\"settings_proximity_screen\">Lülita ekraan kõrva lähedal välja</string>"))
        assertTrue(estonianStrings.contains("telefoni sisseehitatud kuular"))
        assertTrue(estonianStrings.contains("Sisselülitamise, laiendamise ja sulgemise juhtelemendid"))
        assertTrue(estonianStrings.contains("name=\"state_ready\">Valmis</string>"))
        assertTrue(estonianStrings.contains("name=\"state_waiting\">Ootab</string>"))
        assertTrue(estonianStrings.contains("name=\"state_active\">Aktiivne</string>"))
        assertTrue(estonianStrings.contains("name=\"state_error\">Viga</string>"))
    }

    @Test
    fun hungarianFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("Magyar", nativeLocaleName("hu"))
        assertTrue(hungarianStrings.contains("name=\"routing_notification_text\">Várakozás a hang átirányítására</string>"))
        assertTrue(hungarianStrings.contains("name=\"settings_language_body\"") && hungarianStrings.contains("eszközöd nyelvét"))
        assertTrue(hungarianStrings.contains("name=\"settings_language_android_13_required\"") && hungarianStrings.contains("Ez az eszköz a rendszer nyelvét követi."))
        assertTrue(hungarianStrings.contains("name=\"settings_proximity_screen\">A képernyő kikapcsolása a fül közelében</string>"))
        assertTrue(hungarianStrings.contains("telefon beépített fülhallgatójára"))
        assertTrue(hungarianStrings.contains("A bekapcsolás, a kibontás és a bezárás vezérlői"))
        assertTrue(hungarianStrings.contains("name=\"state_ready\">Kész</string>"))
        assertTrue(hungarianStrings.contains("name=\"state_waiting\">Várakozik</string>"))
        assertTrue(hungarianStrings.contains("name=\"state_active\">Aktív</string>"))
        assertTrue(hungarianStrings.contains("name=\"state_error\">Hiba</string>"))
    }

    @Test
    fun basqueFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("euskara", Locale.forLanguageTag("eu").getDisplayLanguage(Locale.forLanguageTag("eu")))
        assertEquals("Euskara", nativeLocaleName("eu"))
        assertTrue(basqueStrings.contains("name=\"product_subtitle\">Hitz egin AArekin modu pribatuan, telefono-dei batean bezala.</string>"))
        assertTrue(basqueStrings.contains("name=\"routing_notification_text\">Audioa bideratzeko zain</string>"))
        assertTrue(basqueStrings.contains("name=\"settings_language_body\"") && basqueStrings.contains("zure gailuaren hizkuntza"))
        assertTrue(basqueStrings.contains("name=\"settings_language_android_13_required\"") && basqueStrings.contains("Gailu honek sistemaren hizkuntza erabiltzen du."))
        assertTrue(basqueStrings.contains("name=\"settings_proximity_screen\">Itzali pantaila belarriaren ondoan</string>"))
        assertTrue(basqueStrings.contains("name=\"settings_about_body\"") && basqueStrings.contains("telefonoaren aurikular integratura"))
        assertTrue(basqueStrings.contains("Pizteko, zabaltzeko eta ixteko kontrolak"))
        assertTrue(basqueStrings.contains("name=\"state_ready\">Prest</string>"))
        assertTrue(basqueStrings.contains("name=\"state_waiting\">Zain</string>"))
        assertTrue(basqueStrings.contains("name=\"state_active\">Aktibo</string>"))
        assertTrue(basqueStrings.contains("name=\"state_error\">Errorea</string>"))
    }

    @Test
    fun albanianFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("shqip", Locale.forLanguageTag("sq").getDisplayLanguage(Locale.forLanguageTag("sq")))
        assertEquals("Shqip", nativeLocaleName("sq"))
        assertTrue(albanianStrings.contains("name=\"routing_notification_title\">Private Audio është ndezur</string>"))
        assertTrue(albanianStrings.contains("name=\"routing_notification_text\">Në pritje për ndërrimin e audios</string>"))
        assertTrue(albanianStrings.contains("name=\"settings_language_body\"") && albanianStrings.contains("gjuhën e pajisjes sate"))
        assertTrue(albanianStrings.contains("name=\"settings_language_android_13_required\"") && albanianStrings.contains("Kjo pajisje përdor gjuhën e sistemit."))
        assertTrue(albanianStrings.contains("name=\"settings_proximity_screen\">Fike ekranin pranë veshit</string>"))
        assertTrue(albanianStrings.contains("name=\"settings_about_body\"") && albanianStrings.contains("marrësi i integruar i telefonit"))
        assertTrue(albanianStrings.contains("Kontrollet për ndezjen, zgjerimin dhe mbylljen"))
        assertTrue(albanianStrings.contains("name=\"state_ready\">Gati</string>"))
        assertTrue(albanianStrings.contains("name=\"state_waiting\">Në pritje</string>"))
        assertTrue(albanianStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertTrue(albanianStrings.contains("name=\"state_error\">Gabim</string>"))
    }

    @Test
    fun latvianFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("latviešu", Locale.forLanguageTag("lv").getDisplayLanguage(Locale.forLanguageTag("lv")))
        assertEquals("Latviešu", nativeLocaleName("lv"))
        assertTrue(latvianStrings.contains("name=\"product_subtitle\">Runājiet ar MI privāti, gluži kā tālruņa sarunā.</string>"))
        assertTrue(latvianStrings.contains("name=\"routing_notification_text\">Gaida audio pārslēgšanu</string>"))
        assertTrue(latvianStrings.contains("name=\"settings_language_body\"") && latvianStrings.contains("jūsu ierīces valodu"))
        assertTrue(latvianStrings.contains("name=\"settings_language_android_13_required\"") && latvianStrings.contains("Šī ierīce izmanto sistēmas valodu."))
        assertTrue(latvianStrings.contains("name=\"settings_proximity_screen\">Izslēgt ekrānu pie auss</string>"))
        assertTrue(latvianStrings.contains("name=\"settings_about_body\"") && latvianStrings.contains("tālruņa iebūvēto uztvērēju"))
        assertTrue(latvianStrings.contains("Ieslēgšanas, izvēršanas un aizvēršanas vadīklas"))
        assertTrue(latvianStrings.contains("name=\"state_ready\">Gatavs</string>"))
        assertTrue(latvianStrings.contains("name=\"state_waiting\">Gaida</string>"))
        assertTrue(latvianStrings.contains("name=\"state_active\">Aktīvs</string>"))
        assertTrue(latvianStrings.contains("name=\"state_error\">Kļūda</string>"))
    }

    @Test
    fun dutchFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("Nederlands", nativeLocaleName("nl"))
        assertTrue(dutchStrings.contains("name=\"product_subtitle\">Praat privé met AI, net als tijdens een telefoongesprek.</string>"))
        assertTrue(dutchStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(dutchStrings.contains("name=\"routing_notification_title\">Private Audio staat aan</string>"))
        assertTrue(dutchStrings.contains("name=\"state_active\">Actief</string>"))
        assertFalse(dutchStrings.contains("name=\"state_active\">Private Audio staat aan</string>"))
        assertTrue(dutchStrings.contains("name=\"routing_notification_text\">Wachten om audio om te schakelen</string>"))
        assertTrue(dutchStrings.contains("name=\"settings_language_body\"") && dutchStrings.contains("taal van je apparaat"))
        assertTrue(dutchStrings.contains("name=\"settings_language_android_13_required\"") && dutchStrings.contains("Dit apparaat volgt de systeemtaal."))
        assertTrue(dutchStrings.contains("name=\"settings_proximity_screen\">Scherm uitschakelen bij je oor</string>"))
        assertTrue(dutchStrings.contains("ingebouwde oorspeaker van je telefoon"))
        assertTrue(dutchStrings.contains("in-/uitschakelen, uitvouwen en sluiten"))
        assertTrue(dutchStrings.contains("name=\"state_ready\">Gereed</string>"))
        assertTrue(dutchStrings.contains("name=\"state_waiting\">Wachten</string>"))
        assertTrue(dutchStrings.contains("name=\"state_active\">Actief</string>"))
        assertTrue(dutchStrings.contains("name=\"state_error\">Fout</string>"))
    }

    @Test
    fun afrikaansFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("Afrikaans", nativeLocaleName("af"))
        assertTrue(afrikaansStrings.contains("name=\"product_subtitle\">Praat privaat met KI, soos tydens \\'n telefoonoproep.</string>"))
        assertTrue(afrikaansStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(afrikaansStrings.contains("name=\"routing_notification_title\">Private Audio is aan</string>"))
        assertTrue(afrikaansStrings.contains("name=\"state_active\">Aktief</string>"))
        assertFalse(afrikaansStrings.contains("name=\"state_active\">Private Audio is aan</string>"))
        assertTrue(afrikaansStrings.contains("name=\"routing_notification_text\">Wag om oudio oor te skakel</string>"))
        assertTrue(afrikaansStrings.contains("name=\"settings_language_body\"") && afrikaansStrings.contains("jou toestel se taal"))
        assertTrue(afrikaansStrings.contains("name=\"settings_language_android_13_required\"") && afrikaansStrings.contains("Hierdie toestel volg die stelseltaal."))
        assertTrue(afrikaansStrings.contains("name=\"settings_proximity_screen\">Skakel die skerm af naby jou oor</string>"))
        assertTrue(afrikaansStrings.contains("jou foon se ingeboude oorstuk"))
        assertTrue(afrikaansStrings.contains("aan/af te skakel, uit te vou en toe te maak"))
        assertTrue(afrikaansStrings.contains("name=\"state_ready\">Gereed</string>"))
        assertTrue(afrikaansStrings.contains("name=\"state_waiting\">Wag</string>"))
        assertTrue(afrikaansStrings.contains("name=\"state_active\">Aktief</string>"))
        assertTrue(afrikaansStrings.contains("name=\"state_error\">Fout</string>"))
    }

    @Test
    fun luxembourgishFrozenLocalizationSemanticsRemainIntact() {
        assertEquals("Lëtzebuergesch", nativeLocaleName("lb"))
        assertTrue(luxembourgishStrings.contains("name=\"product_subtitle\">Schwätz privat mat der KI, wéi bei engem Telefonsgespréich.</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"routing_notification_title\">Private Audio ass un</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertFalse(luxembourgishStrings.contains("name=\"state_active\">Private Audio ass un</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"routing_notification_text\">Waart drop, den Audio ëmzeschalten</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"settings_language_body\"") && luxembourgishStrings.contains("Sprooch vun dengem Apparat"))
        assertTrue(luxembourgishStrings.contains("name=\"settings_language_android_13_required\"") && luxembourgishStrings.contains("Dësen Apparat riicht sech no der Systemsprooch."))
        assertTrue(luxembourgishStrings.contains("name=\"settings_proximity_screen\">Écran beim Ouer ausschalten</string>"))
        assertTrue(luxembourgishStrings.contains("den agebaute Lautsprecher uewen um Telefon ze leeden, deen bei Telefonsgespréicher um Ouer benotzt gëtt"))
        assertTrue(luxembourgishStrings.contains("un-/auszeschalten, ze vergréisseren an zouzemaachen"))
        assertTrue(luxembourgishStrings.contains("name=\"state_ready\">Prett</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"state_waiting\">Waart</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertTrue(luxembourgishStrings.contains("name=\"state_error\">Feeler</string>"))
    }

    @Test
    fun bulgarianFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("bg")
        assertEquals("bg", locale.toLanguageTag())
        assertEquals("Cyrl", Locale.forLanguageTag("bg-Cyrl").script)
        assertEquals("български", locale.getDisplayLanguage(locale))
        assertEquals("Български", nativeLocaleName("bg"))
        assertTrue(projectFile("app/src/main/res/values-bg/strings.xml").isFile)
        assertTrue(bulgarianStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(bulgarianStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(bulgarianStrings.contains("name=\"product_subtitle\">Разговаряйте насаме с ИИ, както по телефона.</string>"))
        assertTrue(bulgarianStrings.contains("name=\"routing_notification_title\">Private Audio е включено</string>"))
        assertTrue(bulgarianStrings.contains("name=\"state_active\">Активно</string>"))
        assertFalse(bulgarianStrings.contains("name=\"state_active\">Private Audio е включено</string>"))
        assertTrue(bulgarianStrings.contains("name=\"routing_notification_text\">Изчакване за превключване на звука</string>"))
        assertTrue(bulgarianStrings.contains("name=\"settings_language_body\"") && bulgarianStrings.contains("езика на устройството ви"))
        assertTrue(bulgarianStrings.contains("name=\"settings_language_android_13_required\"") && bulgarianStrings.contains("системния език"))
        assertTrue(bulgarianStrings.contains("name=\"settings_proximity_screen\">Изключване на екрана близо до ухото</string>"))
        assertTrue(bulgarianStrings.contains("вградената телефонна слушалка"))
        assertTrue(bulgarianStrings.contains("включване/изключване, разгъване и затваряне"))
        assertTrue(bulgarianStrings.contains("name=\"settings_system_default\">По подразбиране</string>"))
        assertTrue(bulgarianStrings.contains("name=\"state_ready\">Готово</string>"))
        assertTrue(bulgarianStrings.contains("name=\"state_waiting\">Изчакване</string>"))
        assertTrue(bulgarianStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(bulgarianStrings.contains("name=\"state_error\">Грешка</string>"))
    }

    @Test
    fun serbianCyrillicFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("sr")
        assertEquals("sr", locale.toLanguageTag())
        assertEquals("Cyrl", Locale.forLanguageTag("sr-Cyrl").script)
        assertEquals("српски", locale.getDisplayLanguage(locale))
        assertEquals("Српски", nativeLocaleName("sr"))
        assertTrue(projectFile("app/src/main/res/values-sr/strings.xml").isFile)
        assertTrue(projectFile("app/src/main/res/values-b+sr+Latn/strings.xml").isFile)
        assertTrue(serbianCyrillicStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"product_subtitle\">Разговарајте приватно са ВИ, као током телефонског позива.</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"product_subtitle\">Razgovarajte privatno sa AI-jem, kao tokom telefonskog poziva.</string>"))
        assertFalse(serbianCyrillicStrings == serbianLatinStrings)
        assertTrue(serbianCyrillicStrings.contains("name=\"routing_notification_title\">Private Audio је укључен</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"state_active\">Активно</string>"))
        assertFalse(serbianCyrillicStrings.contains("name=\"state_active\">Private Audio је укључен</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"routing_notification_text\">Чекање на пребацивање звука</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"settings_language_body\"") && serbianCyrillicStrings.contains("језик вашег уређаја"))
        assertTrue(serbianCyrillicStrings.contains("name=\"settings_language_android_13_required\"") && serbianCyrillicStrings.contains("језик система"))
        assertTrue(serbianCyrillicStrings.contains("name=\"settings_proximity_screen\">Искључи екран близу уха</string>"))
        assertTrue(serbianCyrillicStrings.contains("уграђену слушалицу телефона"))
        assertTrue(serbianCyrillicStrings.contains("укључивање/искључивање, проширивање и затварање"))
        assertTrue(serbianCyrillicStrings.contains("name=\"settings\">Подешавања</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"settings_system_default\">Подразумевано</string>"))
        assertTrue(serbianCyrillicStrings.contains("извештај"))
        assertTrue(serbianCyrillicStrings.contains("овде"))
        assertTrue(serbianCyrillicStrings.contains("name=\"state_ready\">Спремно</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"state_waiting\">Чекање</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(serbianCyrillicStrings.contains("name=\"state_error\">Грешка</string>"))
        assertTrue(serbianCyrillicStrings.filterNot { it.isWhitespace() }.any { it in '\u0400'..'\u04FF' })
        assertFalse(serbianLatinStrings.any { it in '\u0400'..'\u04FF' })
    }

    @Test
    fun macedonianFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("mk")
        assertEquals("mk", locale.toLanguageTag())
        assertEquals("Cyrl", Locale.forLanguageTag("mk-Cyrl").script)
        assertEquals("македонски", locale.getDisplayLanguage(locale))
        assertEquals("Македонски", nativeLocaleName("mk"))
        assertTrue(projectFile("app/src/main/res/values-mk/strings.xml").isFile)
        assertTrue(macedonianStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(macedonianStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(macedonianStrings.contains("name=\"product_subtitle\">Разговарајте приватно со ВИ, како при телефонски повик.</string>"))
        assertTrue(macedonianStrings.contains("name=\"routing_notification_title\">Private Audio е вклучено</string>"))
        assertTrue(macedonianStrings.contains("name=\"state_active\">Активно</string>"))
        assertFalse(macedonianStrings.contains("name=\"state_active\">Private Audio е вклучено</string>"))
        assertTrue(macedonianStrings.contains("name=\"routing_notification_text\">Се чека префрлање на звукот</string>"))
        assertTrue(macedonianStrings.contains("name=\"settings_language_body\"") && macedonianStrings.contains("јазикот на вашиот уред"))
        assertTrue(macedonianStrings.contains("name=\"settings_language_android_13_required\"") && macedonianStrings.contains("системскиот јазик"))
        assertTrue(macedonianStrings.contains("name=\"settings_proximity_screen\">Исклучи го екранот близу до увото</string>"))
        assertTrue(macedonianStrings.contains("вградената слушалка на телефонот"))
        assertTrue(macedonianStrings.contains("вклучување/исклучување, проширување и затворање"))
        assertTrue(macedonianStrings.contains("name=\"settings_system_default\">Стандардно</string>"))
        assertTrue(macedonianStrings.contains("name=\"state_ready\">Подготвено</string>"))
        assertTrue(macedonianStrings.contains("name=\"state_waiting\">Чекање</string>"))
        assertTrue(macedonianStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(macedonianStrings.contains("name=\"state_error\">Грешка</string>"))
    }

    @Test
    fun slovenianFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("sl")
        assertEquals("slovenščina", locale.getDisplayLanguage(locale))
        assertEquals("Slovenščina", nativeLocaleName("sl"))
        assertTrue(projectFile("app/src/main/res/values-sl/strings.xml").isFile)
        assertTrue(slovenianStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(slovenianStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(slovenianStrings.contains("name=\"product_subtitle\">Z UI se pogovarjajte zasebno, kot po telefonu.</string>"))
        assertTrue(slovenianStrings.contains("name=\"routing_notification_title\">Private Audio je vklopljen</string>"))
        assertTrue(slovenianStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertFalse(slovenianStrings.contains("name=\"state_active\">Private Audio je vklopljen</string>"))
        assertTrue(slovenianStrings.contains("name=\"routing_notification_text\">Čakanje na preklop zvoka</string>"))
        assertTrue(slovenianStrings.contains("name=\"settings_language_body\"") && slovenianStrings.contains("jezik vaše naprave"))
        assertTrue(slovenianStrings.contains("name=\"settings_language_android_13_required\"") && slovenianStrings.contains("sistemski jezik"))
        assertTrue(slovenianStrings.contains("name=\"settings_proximity_screen\">Izklopi zaslon ob ušesu</string>"))
        assertTrue(slovenianStrings.contains("vgrajeno slušalko telefona"))
        assertTrue(slovenianStrings.contains("vklop/izklop, razširitev in zapiranje"))
        assertTrue(slovenianStrings.contains("name=\"state_ready\">Pripravljeno</string>"))
        assertTrue(slovenianStrings.contains("name=\"state_waiting\">Čakanje</string>"))
        assertTrue(slovenianStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertTrue(slovenianStrings.contains("name=\"state_error\">Napaka</string>"))
    }

    @Test
    fun croatianFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("hr")
        assertEquals("hrvatski", locale.getDisplayLanguage(locale))
        assertEquals("Hrvatski", nativeLocaleName("hr"))
        assertTrue(projectFile("app/src/main/res/values-hr/strings.xml").isFile)
        assertTrue(croatianStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(croatianStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(croatianStrings.contains("name=\"product_subtitle\">Razgovarajte privatno s AI-jem, kao tijekom telefonskog poziva.</string>"))
        assertTrue(croatianStrings.contains("name=\"routing_notification_title\">Private Audio je uključen</string>"))
        assertTrue(croatianStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertFalse(croatianStrings.contains("name=\"state_active\">Private Audio je uključen</string>"))
        assertTrue(croatianStrings.contains("name=\"routing_notification_text\">Čekanje na prebacivanje zvuka</string>"))
        assertTrue(croatianStrings.contains("name=\"settings_language_body\"") && croatianStrings.contains("jezik vašeg uređaja"))
        assertTrue(croatianStrings.contains("name=\"settings_language_android_13_required\"") && croatianStrings.contains("jezik sustava"))
        assertTrue(croatianStrings.contains("name=\"settings_proximity_screen\">Isključi zaslon blizu uha</string>"))
        assertTrue(croatianStrings.contains("ugrađenu slušalicu telefona"))
        assertTrue(croatianStrings.contains("uključivanje/isključivanje, proširivanje i zatvaranje"))
        assertTrue(croatianStrings.contains("name=\"state_ready\">Spremno</string>"))
        assertTrue(croatianStrings.contains("name=\"state_waiting\">Čekanje</string>"))
        assertTrue(croatianStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertTrue(croatianStrings.contains("name=\"state_error\">Pogreška</string>"))
        assertTrue(croatianStrings.contains("dijagnostičko izvješće"))
    }

    @Test
    fun serbianLatinFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("sr-Latn")
        assertEquals("sr-Latn", locale.toLanguageTag())
        assertEquals("Latn", locale.script)
        assertEquals("srpski (latinica)", locale.getDisplayName(locale))
        assertEquals("Srpski (latinica)", nativeLocaleName("sr-Latn"))
        assertTrue(projectFile("app/src/main/res/values-b+sr+Latn/strings.xml").isFile)
        assertTrue(serbianLatinStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"product_subtitle\">Razgovarajte privatno sa AI-jem, kao tokom telefonskog poziva.</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"routing_notification_title\">Private Audio je uključen</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertFalse(serbianLatinStrings.contains("name=\"state_active\">Private Audio je uključen</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"routing_notification_text\">Čekanje na prebacivanje zvuka</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"settings_language_body\"") && serbianLatinStrings.contains("jezik vašeg uređaja"))
        assertTrue(serbianLatinStrings.contains("name=\"settings_language_android_13_required\"") && serbianLatinStrings.contains("jezik sistema"))
        assertTrue(serbianLatinStrings.contains("name=\"settings_proximity_screen\">Isključi ekran blizu uha</string>"))
        assertTrue(serbianLatinStrings.contains("ugrađenu slušalicu telefona"))
        assertTrue(serbianLatinStrings.contains("uključivanje/isključivanje, proširivanje i zatvaranje"))
        assertTrue(serbianLatinStrings.contains("name=\"state_ready\">Spremno</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"state_waiting\">Čekanje</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"state_error\">Greška</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"settings\">Podešavanja</string>"))
        assertTrue(serbianLatinStrings.contains("name=\"settings_system_default\">Podrazumevano</string>"))
        assertTrue(serbianLatinStrings.contains("dijagnostički izveštaj"))
        assertTrue(serbianLatinStrings.contains("pojavljivati ovde"))
        assertFalse(serbianLatinStrings.any { it in '\u0400'..'\u04FF' })
    }

    @Test
    fun bosnianFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("bs")
        assertEquals("bosanski", locale.getDisplayLanguage(locale))
        assertEquals("Bosanski", nativeLocaleName("bs"))
        assertTrue(projectFile("app/src/main/res/values-bs/strings.xml").isFile)
        assertTrue(bosnianStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(bosnianStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(bosnianStrings.contains("name=\"product_subtitle\">Razgovarajte privatno s AI-jem, kao tokom telefonskog poziva.</string>"))
        assertTrue(bosnianStrings.contains("name=\"routing_notification_title\">Private Audio je uključen</string>"))
        assertTrue(bosnianStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertFalse(bosnianStrings.contains("name=\"state_active\">Private Audio je uključen</string>"))
        assertTrue(bosnianStrings.contains("name=\"routing_notification_text\">Čekanje na prebacivanje zvuka</string>"))
        assertTrue(bosnianStrings.contains("name=\"settings_language_body\"") && bosnianStrings.contains("jezik vašeg uređaja"))
        assertTrue(bosnianStrings.contains("name=\"settings_language_android_13_required\"") && bosnianStrings.contains("jezik sistema"))
        assertTrue(bosnianStrings.contains("name=\"settings_proximity_screen\">Isključi ekran blizu uha</string>"))
        assertTrue(bosnianStrings.contains("ugrađenu slušalicu telefona"))
        assertTrue(bosnianStrings.contains("uključivanje/isključivanje, proširivanje i zatvaranje"))
        assertTrue(bosnianStrings.contains("name=\"state_ready\">Spremno</string>"))
        assertTrue(bosnianStrings.contains("name=\"state_waiting\">Čekanje</string>"))
        assertTrue(bosnianStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertTrue(bosnianStrings.contains("name=\"state_error\">Greška</string>"))
        assertTrue(bosnianStrings.contains("name=\"settings\">Postavke</string>"))
        assertTrue(bosnianStrings.contains("name=\"settings_system_default\">Zadano</string>"))
        assertTrue(bosnianStrings.contains("dijagnostički izvještaj"))
        assertTrue(bosnianStrings.contains("pojavljivati ovdje"))
    }

    @Test
    fun serbianLatinMontenegroFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("sr-Latn-ME")
        assertEquals("sr-Latn-ME", locale.toLanguageTag())
        assertEquals("sr", locale.language)
        assertEquals("Latn", locale.script)
        assertEquals("ME", locale.country)
        assertEquals("srpski (latinica, Crna Gora)", locale.getDisplayName(locale))
        assertEquals("Srpski (latinica, Crna Gora)", nativeLocaleName("sr-Latn-ME"))
        assertTrue(projectFile("app/src/main/res/values-b+sr+Latn+ME/strings.xml").isFile)
        assertTrue(projectFile("app/src/main/res/values-b+sr+Latn/strings.xml").isFile)
        assertTrue(projectFile("app/src/main/res/values-sr/strings.xml").isFile)
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"product_subtitle\">Razgovarajte privatno sa AI-jem, kao tokom telefonskog poziva.</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"routing_notification_title\">Private Audio je uključen</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertFalse(serbianLatinMontenegroStrings.contains("name=\"state_active\">Private Audio je uključen</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"routing_notification_text\">Čekanje na prebacivanje zvuka</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"settings_language_body\"") && serbianLatinMontenegroStrings.contains("jezik vašeg uređaja"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"settings_language_android_13_required\"") && serbianLatinMontenegroStrings.contains("jezik sistema"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"settings_proximity_screen\">Isključi ekran blizu uha</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("ugrađenu slušalicu telefona"))
        assertTrue(serbianLatinMontenegroStrings.contains("uključivanje/isključivanje, proširivanje i zatvaranje"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"state_ready\">Spremno</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"state_waiting\">Čekanje</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"state_active\">Aktivno</string>"))
        assertTrue(serbianLatinMontenegroStrings.contains("name=\"state_error\">Greška</string>"))
        listOf("Podrazumijevano", "podrazumijevana", "zahtijeva", "izvještaj", "ovdje").forEach {
            assertTrue(it, serbianLatinMontenegroStrings.contains(it))
            assertFalse(it, serbianLatinStrings.contains(it))
        }
        listOf("Podrazumevano", "podrazumevana", "zahteva", "izveštaj", "ovde").forEach {
            assertTrue(it, serbianLatinStrings.contains(it))
            assertFalse(it, serbianLatinMontenegroStrings.contains(it))
        }
        assertFalse(serbianLatinMontenegroStrings == serbianLatinStrings)
        assertFalse(serbianLatinMontenegroStrings.any { it in '\u0400'..'\u04FF' })
    }

    @Test
    fun malteseFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("mt")
        assertEquals("mt", locale.toLanguageTag())
        assertEquals("mt", locale.language)
        assertEquals("Malti", nativeLocaleName("mt"))
        assertTrue(projectFile("app/src/main/res/values-mt/strings.xml").isFile)
        assertTrue(malteseStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(malteseStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(malteseStrings.contains("name=\"product_subtitle\">Tkellem mal-AI fil-privat, bħal f\\'telefonata.</string>"))
        assertTrue(malteseStrings.contains("name=\"routing_notification_title\">Private Audio huwa mixgħul</string>"))
        assertTrue(malteseStrings.contains("name=\"state_active\">Attiv</string>"))
        assertFalse(malteseStrings.contains("name=\"state_active\">Private Audio huwa mixgħul</string>"))
        assertTrue(malteseStrings.contains("name=\"routing_notification_text\">Stennija biex jinbidel l-output tal-awdjo</string>"))
        assertTrue(malteseStrings.contains("name=\"settings\">Issettjar</string>"))
        assertFalse(malteseStrings.contains("name=\"settings\">Settings</string>"))
        assertTrue(malteseStrings.contains("name=\"settings_advanced\">Issettjar avvanzat</string>"))
        assertTrue(malteseStrings.contains("name=\"settings_system_default\">Default tas-sistema</string>"))
        assertTrue(malteseStrings.contains("name=\"settings_language_body\"") && malteseStrings.contains("lingwa tal-apparat tiegħek"))
        assertTrue(malteseStrings.contains("name=\"settings_language_android_13_required\"") && malteseStrings.contains("lingwa tas-sistema"))
        assertTrue(malteseStrings.contains("name=\"settings_proximity_screen\">Itfi l-iskrin meta jkun qrib widintek</string>"))
        assertTrue(malteseStrings.contains("riċevitur integrat tat-telefon tiegħek"))
        assertTrue(malteseStrings.contains("dak ta\\' fuq li tuża ma\\' widintek waqt telefonata"))
        assertTrue(malteseStrings.contains("tixgħel/titfi, tespandi u tagħlaq"))
        assertTrue(malteseStrings.contains("name=\"state_ready\">Lest</string>"))
        assertTrue(malteseStrings.contains("name=\"state_waiting\">Stennija</string>"))
        assertTrue(malteseStrings.contains("name=\"state_active\">Attiv</string>"))
        assertTrue(malteseStrings.contains("name=\"state_error\">Żball</string>"))
    }

    @Test
    fun greekFrozenLocalizationSemanticsRemainIntact() {
        val localeTag = "el"
        val locale = Locale.forLanguageTag(localeTag)
        assertEquals("el", locale.language)
        assertEquals("Ελληνικά", locale.getDisplayLanguage(locale))
        assertEquals("Ελληνικά", nativeLocaleName(localeTag))
        assertTrue(projectFile("app/src/main/res/values-el/strings.xml").isFile)
        assertTrue(greekStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(greekStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(greekStrings.contains("name=\"product_subtitle\">Μιλήστε ιδιωτικά με AI, σαν να μιλάτε στο τηλέφωνο.</string>"))
        assertTrue(greekStrings.contains("name=\"routing_notification_title\">Το Private Audio είναι ενεργοποιημένο</string>"))
        assertTrue(greekStrings.contains("name=\"state_active\">Ενεργό</string>"))
        assertFalse(greekStrings.contains("name=\"state_active\">Το Private Audio είναι ενεργοποιημένο</string>"))
        assertTrue(greekStrings.contains("name=\"routing_notification_text\">Αναμονή για αλλαγή εξόδου ήχου</string>"))
        assertTrue(greekStrings.contains("name=\"settings_language_body\"") && greekStrings.contains("γλώσσα της συσκευής σας"))
        assertTrue(greekStrings.contains("name=\"settings_language_android_13_required\"") && greekStrings.contains("γλώσσα συστήματος"))
        assertTrue(greekStrings.contains("name=\"settings_system_default\">Προεπιλογή συστήματος</string>"))
        assertTrue(greekStrings.contains("name=\"settings_proximity_screen\">Απενεργοποίηση οθόνης κοντά στο αυτί</string>"))
        assertTrue(greekStrings.contains("ενσωματωμένο ακουστικό του τηλεφώνου σας"))
        assertTrue(greekStrings.contains("ενεργοποίηση/απενεργοποίηση, ανάπτυξη και κλείσιμο"))
        assertTrue(greekStrings.contains("name=\"state_ready\">Έτοιμο</string>"))
        assertTrue(greekStrings.contains("name=\"state_waiting\">Αναμονή</string>"))
        assertTrue(greekStrings.contains("name=\"state_active\">Ενεργό</string>"))
        assertTrue(greekStrings.contains("name=\"state_error\">Σφάλμα</string>"))
    }

    @Test
    fun bengaliFrozenLocalizationSemanticsRemainIntact() {
        val localeTag = "bn"
        val locale = Locale.forLanguageTag(localeTag)
        assertEquals("bn", locale.toLanguageTag())
        assertEquals("bn", locale.language)
        assertEquals("বাংলা", locale.getDisplayLanguage(locale))
        assertEquals("বাংলা", nativeLocaleName(localeTag))
        assertTrue(projectFile("app/src/main/res/values-bn/strings.xml").isFile)
        assertTrue(bengaliStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(bengaliStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(bengaliStrings.contains("name=\"product_subtitle\">AI-এর সঙ্গে ব্যক্তিগতভাবে কথা বলুন, ঠিক যেন ফোনে কথা বলছেন।</string>"))
        assertTrue(bengaliStrings.contains("name=\"routing_notification_title\">Private Audio চালু আছে</string>"))
        assertTrue(bengaliStrings.contains("name=\"state_active\">সক্রিয়</string>"))
        assertFalse(bengaliStrings.contains("name=\"state_active\">চালু আছে</string>"))
        assertTrue(bengaliStrings.contains("name=\"routing_notification_text\">অডিও আউটপুট পাল্টানোর জন্য অপেক্ষা করা হচ্ছে</string>"))
        assertTrue(bengaliStrings.contains("name=\"settings_language_body\"") && bengaliStrings.contains("ডিভাইসের ভাষা"))
        assertTrue(bengaliStrings.contains("name=\"settings_language_android_13_required\"") && bengaliStrings.contains("সিস্টেমের ভাষা"))
        assertTrue(bengaliStrings.contains("name=\"settings_system_default\">সিস্টেম ডিফল্ট</string>"))
        assertTrue(bengaliStrings.contains("name=\"settings_proximity_screen\">কানের কাছে আনলে স্ক্রিন বন্ধ করুন</string>"))
        assertTrue(bengaliStrings.contains("বিল্ট-ইন হ্যান্ডসেট ইয়ারপিস"))
        assertTrue(bengaliStrings.contains("পাওয়ার চালু বা বন্ধ করা"))
        assertTrue(bengaliStrings.contains("বড় করা"))
        assertTrue(bengaliStrings.contains("কন্ট্রোলার বন্ধ করা"))
        assertTrue(bengaliStrings.contains("name=\"state_ready\">প্রস্তুত</string>"))
        assertTrue(bengaliStrings.contains("name=\"state_waiting\">অপেক্ষা</string>"))
        assertTrue(bengaliStrings.contains("name=\"state_active\">সক্রিয়</string>"))
        assertTrue(bengaliStrings.contains("name=\"state_error\">ত্রুটি</string>"))
        assertTrue(bengaliStrings.any { it in '\u0980'..'\u09FF' })
    }

    @Test
    fun malayalamFrozenLocalizationSemanticsRemainIntact() {
        val locale = Locale.forLanguageTag("ml")
        assertEquals("ml", locale.toLanguageTag())
        assertEquals("ml", locale.language)
        assertEquals("മലയാളം", nativeLocaleName("ml"))
        assertTrue(projectFile("app/src/main/res/values-ml/strings.xml").isFile)
        assertFalse(projectFile("app/src/main/res/values-b+ml/strings.xml").exists())
        assertTrue(malayalamStrings.any { it in '\u0D00'..'\u0D7F' })
        assertTrue(malayalamStrings.contains("name=\"product_subtitle\">ഒരു ഫോൺ കോൾ പോലെ AI-യുമായി സ്വകാര്യമായി സംസാരിക്കൂ.</string>"))
        assertTrue(malayalamStrings.contains("name=\"routing_notification_title\">Private Audio ഓണാണ്</string>"))
        assertTrue(malayalamStrings.contains("name=\"state_active\">സജീവം</string>"))
        assertFalse(malayalamStrings.contains("name=\"state_active\">ഓണാണ്</string>"))
        assertTrue(malayalamStrings.contains("name=\"floating\">മിനി</string>"))
        assertTrue(malayalamStrings.contains("name=\"settings_advanced\">വിപുലമായ ക്രമീകരണം</string>"))
        assertTrue(malayalamStrings.contains("ബിൽറ്റ്-ഇൻ ഇയർപീസ്"))
        assertEquals(
            mapOf("state_ready" to "തയ്യാറാണ്", "state_waiting" to "കാത്തിരിക്കുന്നു", "state_active" to "സജീവം", "state_error" to "പിശക്"),
            frozenStates(malayalamStrings),
        )
    }

    @Test
    fun somaliKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("so")
        val defaultIdentity = Locale.forLanguageTag("so-Latn-SO")
        assertEquals("so", locale.toLanguageTag())
        assertEquals("so", locale.language)
        assertEquals("Latn", defaultIdentity.script)
        assertEquals("SO", defaultIdentity.country)
        assertEquals("Soomaali", nativeLocaleName("so"))
        assertTrue(projectFile("app/src/main/res/values-so/strings.xml").isFile)
        listOf(
            "values-so-rSO",
            "values-so-rDJ",
            "values-so-rET",
            "values-so-rKE",
            "values-b+so+Latn",
            "values-b+so+Latn+SO",
        ).forEach { assertFalse(projectFile("app/src/main/res/$it").exists()) }
        assertTrue(somaliStrings.filterNot(Char::isWhitespace).all { it.code < 0x0250 })
        assertTrue(somaliStrings.contains("name=\"routing_notification_title\">Private Audio waa daaran yahay</string>"))
        assertTrue(somaliStrings.contains("name=\"state_active\">Firfircoon</string>"))
        assertFalse(somaliStrings.contains("name=\"state_active\">daaran</string>"))
        assertEquals(
            mapOf("state_ready" to "Diyaar", "state_waiting" to "Sugaya", "state_active" to "Firfircoon", "state_error" to "Khalad"),
            frozenStates(somaliStrings),
        )
        assertTrue(somaliStrings.contains("name=\"routing_notification_text\">Sugaya in la beddelo halka codku ka soo baxayo</string>"))
        assertTrue(somaliStrings.contains("name=\"product_subtitle\">AI si gaar ah ula hadal, sida adigoo taleefan ku hadlaya.</string>"))
        assertTrue(somaliStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(somaliStrings.contains("name=\"settings_system_default\">Caadi</string>"))
        assertTrue(somaliStrings.contains("name=\"settings_advanced\">Dejinta horumarsan</string>"))
        assertTrue(somaliStrings.contains("name=\"power_control\">Daar/Dami</string>"))
        assertTrue(somaliStrings.contains("sameecadda sare ee taleefankaaga"))
        assertTrue(somaliStrings.contains("qalabkaaga"))
        assertTrue(somaliStrings.contains("luqadda nidaamka"))
        assertTrue(somaliStrings.contains("shaashadda"))
        assertFalse(somaliStrings.contains("sameecadda dhegta"))
        assertFalse(projectFile("app/src/main/res/values-so/mini_state_strings.xml").exists())
    }

    @Test
    fun kazakhKeepsOneCyrillicQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("kk")
        val defaultIdentity = Locale.forLanguageTag("kk-Cyrl-KZ")
        assertEquals("kk", locale.toLanguageTag())
        assertEquals("kk", locale.language)
        assertEquals("Cyrl", defaultIdentity.script)
        assertEquals("KZ", defaultIdentity.country)
        assertEquals("Қазақ тілі", nativeLocaleName("kk"))
        assertTrue(projectFile("app/src/main/res/values-kk/strings.xml").isFile)
        listOf("values-kk-rKZ", "values-b+kk+Cyrl", "values-b+kk+Cyrl+KZ", "values-b+kk+Latn")
            .forEach { assertFalse(projectFile("app/src/main/res/$it").exists()) }
        assertTrue(kazakhStrings.any { it in '\u0400'..'\u04FF' })
        assertTrue(kazakhStrings.contains("name=\"routing_notification_title\">Private Audio қосулы</string>"))
        assertTrue(kazakhStrings.contains("name=\"state_active\">Белсенді</string>"))
        assertFalse(kazakhStrings.contains("name=\"state_active\">Қосулы</string>"))
        assertEquals(
            mapOf("state_ready" to "Дайын", "state_waiting" to "Күтуде", "state_active" to "Белсенді", "state_error" to "Қате"),
            frozenStates(kazakhStrings),
        )
        assertTrue(kazakhStrings.contains("name=\"product_subtitle\">ЖИ-мен телефон қоңырауындағыдай жеке сөйлесіңіз.</string>"))
        assertTrue(kazakhStrings.contains("name=\"floating\">Мини</string>"))
        assertTrue(kazakhStrings.contains("name=\"settings_system_default\">Әдепкі</string>"))
        assertTrue(kazakhStrings.contains("name=\"settings_advanced\">Қосымша</string>"))
        assertTrue(kazakhStrings.contains("name=\"routing_notification_text\">Аудио шығысын ауыстыруды күтуде</string>"))
        assertTrue(kazakhStrings.contains("кірістірілген құлақ динамигіне"))
        assertTrue(kazakhStrings.contains("Диагностикалық есеп"))
        assertTrue(kazakhStrings.contains("қосу немесе өшіру, басқару құралын кеңейту және жабу"))
        assertFalse(kazakhStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(projectFile("app/src/main/res/values-kk/mini_state_strings.xml").exists())
    }

    @Test
    fun nepaliKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("ne")
        val defaultIdentity = Locale.forLanguageTag("ne-Deva-NP")
        assertEquals("ne", locale.toLanguageTag())
        assertEquals("ne", locale.language)
        assertEquals("Deva", defaultIdentity.script)
        assertEquals("NP", defaultIdentity.country)
        assertEquals("नेपाली", nativeLocaleName("ne"))
        assertTrue(projectFile("app/src/main/res/values-ne/strings.xml").isFile)
        listOf("values-ne-rNP", "values-b+ne+Deva", "values-b+ne+Deva+NP").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(nepaliStrings.any { it in '\u0900'..'\u097F' })
        assertTrue(nepaliStrings.contains("name=\"routing_notification_title\">Private Audio अन छ</string>"))
        assertTrue(nepaliStrings.contains("name=\"state_active\">सक्रिय</string>"))
        assertFalse(nepaliStrings.contains("name=\"state_active\">अन छ</string>"))
        assertEquals(
            mapOf("state_ready" to "तयार", "state_waiting" to "पर्खँदै", "state_active" to "सक्रिय", "state_error" to "त्रुटि"),
            frozenStates(nepaliStrings),
        )
        assertTrue(nepaliStrings.contains("name=\"routing_notification_text\">अडियो आउटपुट बदल्न पर्खँदै</string>"))
        assertTrue(nepaliStrings.contains("name=\"product_subtitle\">फोन कल गरेजस्तै, एआईसँग निजी रूपमा कुरा गर्नुहोस्।</string>"))
        assertTrue(nepaliStrings.contains("name=\"floating\">मिनी</string>"))
        assertTrue(nepaliStrings.contains("name=\"settings_system_default\">डिफल्ट</string>"))
        assertTrue(nepaliStrings.contains("name=\"settings_advanced\">उन्नत</string>"))
        assertTrue(nepaliStrings.contains("ह्यान्डसेट इयरपिस"))
        assertTrue(nepaliStrings.contains("पावर अन वा अफ गर्ने, विस्तृत गर्ने र बन्द गर्ने नियन्त्रणहरू"))
        assertFalse(projectFile("app/src/main/res/values-ne/mini_state_strings.xml").exists())
    }

    @Test
    fun armenianKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("hy")
        val defaultIdentity = Locale.forLanguageTag("hy-Armn-AM")
        assertEquals("hy", locale.toLanguageTag())
        assertEquals("hy", locale.language)
        assertEquals("Armn", defaultIdentity.script)
        assertEquals("AM", defaultIdentity.country)
        assertEquals("Հայերեն", nativeLocaleName("hy"))
        assertTrue(projectFile("app/src/main/res/values-hy/strings.xml").isFile)
        listOf("values-hy-rAM", "values-b+hy+Armn", "values-b+hy+Armn+AM").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(armenianStrings.any { it in '\u0530'..'\u058F' })
        assertTrue(armenianStrings.contains("name=\"routing_notification_title\">Private Audio-ն միացված է</string>"))
        assertTrue(armenianStrings.contains("name=\"state_active\">Ակտիվ</string>"))
        assertFalse(armenianStrings.contains("name=\"state_active\">Միացված է</string>"))
        assertEquals(
            mapOf("state_ready" to "Պատրաստ է", "state_waiting" to "Սպասում է", "state_active" to "Ակտիվ", "state_error" to "Սխալ"),
            frozenStates(armenianStrings),
        )
        assertTrue(armenianStrings.contains("name=\"product_subtitle\">ԱԲ-ի հետ խոսեք"))
        assertTrue(armenianStrings.contains("name=\"floating\">Մինի</string>"))
        assertTrue(armenianStrings.contains("name=\"settings\">Կարգավորումներ</string>"))
        assertTrue(armenianStrings.contains("name=\"settings_system_default\">Կանխադրված</string>"))
        assertTrue(armenianStrings.contains("name=\"settings_advanced\">Ընդլայնված</string>"))
        assertTrue(armenianStrings.contains("հեռախոսի ներկառուցված լսափող"))
        assertTrue(armenianStrings.contains("աուդիո ելքի փոխարկման"))
        assertTrue(armenianStrings.contains("ախտորոշման հաշվետվություն"))
        assertTrue(armenianStrings.contains("Private Audio-ն միացնելու կամ անջատելու, կառավարիչն ընդարձակելու և փակելու կառավարման տարրեր"))
        assertFalse(armenianStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(projectFile("app/src/main/res/values-hy/mini_state_strings.xml").exists())
    }

    @Test
    fun mongolianKeepsOneNeutralCyrillicQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("mn")
        val defaultIdentity = Locale.forLanguageTag("mn-Cyrl-MN")
        assertEquals("mn", locale.toLanguageTag())
        assertEquals("mn", locale.language)
        assertEquals("Cyrl", defaultIdentity.script)
        assertEquals("MN", defaultIdentity.country)
        assertEquals("монгол", nativeLocaleName("mn"))
        assertTrue(projectFile("app/src/main/res/values-mn/strings.xml").isFile)
        listOf("values-mn-rMN", "values-b+mn+Cyrl", "values-b+mn+Cyrl+MN", "values-mn-rCN", "values-b+mn+Mong", "values-b+mn+Mong+CN", "values-b+mn+Latn").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(mongolianStrings.any { it in '\u0400'..'\u04FF' })
        assertTrue(mongolianStrings.contains("name=\"routing_notification_title\">Private Audio асаалттай</string>"))
        assertTrue(mongolianStrings.contains("name=\"state_active\">Идэвхтэй</string>"))
        assertFalse(mongolianStrings.contains("name=\"state_active\">Асаалттай</string>"))
        assertEquals(
            mapOf("state_ready" to "Бэлэн", "state_waiting" to "Хүлээж байна", "state_active" to "Идэвхтэй", "state_error" to "Алдаа"),
            frozenStates(mongolianStrings),
        )
        assertTrue(mongolianStrings.contains("name=\"product_subtitle\">ХОУ-тай утсаар ярьж байгаа мэт хувийн байдлаар ярилцаарай.</string>"))
        assertTrue(mongolianStrings.contains("name=\"floating\">Мини</string>"))
        assertTrue(mongolianStrings.contains("name=\"settings_system_default\">Өгөгдмөл</string>"))
        assertTrue(mongolianStrings.contains("name=\"settings_advanced\">Нарийвчилсан</string>"))
        assertTrue(mongolianStrings.contains("name=\"routing_notification_text\">Аудио гаралтыг солихыг хүлээж байна</string>"))
        assertTrue(mongolianStrings.contains("утсанд суурилуулсан чихний чанга яригч"))
        assertTrue(mongolianStrings.contains("Оношилгооны тайлан"))
        assertTrue(mongolianStrings.contains("Асаах/унтраах, дэлгэх, хаах товчлуурууд"))
        assertFalse(mongolianStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(projectFile("app/src/main/res/values-mn/mini_state_strings.xml").exists())
    }

    @Test
    fun georgianKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("ka")
        val defaultIdentity = Locale.forLanguageTag("ka-Geor-GE")
        assertEquals("ka", locale.toLanguageTag())
        assertEquals("ka", locale.language)
        assertEquals("Geor", defaultIdentity.script)
        assertEquals("GE", defaultIdentity.country)
        assertEquals("ქართული", nativeLocaleName("ka"))
        assertTrue(projectFile("app/src/main/res/values-ka/strings.xml").isFile)
        listOf("values-ka-rGE", "values-b+ka+Geor", "values-b+ka+Geor+GE").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(georgianStrings.any { it in '\u10A0'..'\u10FF' })
        assertTrue(georgianStrings.contains("name=\"routing_notification_title\">Private Audio ჩართულია</string>"))
        assertTrue(georgianStrings.contains("name=\"state_active\">აქტიურია</string>"))
        assertFalse(georgianStrings.contains("name=\"state_active\">ჩართულია</string>"))
        assertEquals(
            mapOf("state_ready" to "მზადაა", "state_waiting" to "მოლოდინშია", "state_active" to "აქტიურია", "state_error" to "შეცდომა"),
            frozenStates(georgianStrings),
        )
        listOf("AI-სთან", ">მინი</string>", ">პარამეტრები</string>", ">ნაგულისხმევი</string>", ">დამატებითი</string>", "ტელეფონის ჩაშენებულ ყურმილზე", "გამომავალი აუდიო", "გამომავალი აუდიოს გადართვა", "დიაგნოსტიკის ანგარიში").forEach {
            assertTrue(it, georgianStrings.contains(it))
        }
        assertTrue(georgianStrings.contains("ჩართვის/გამორთვის, გაშლისა და დახურვის მართვის ელემენტები"))
        assertFalse(georgianStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(projectFile("app/src/main/res/values-ka/mini_state_strings.xml").exists())
    }

    @Test
    fun laoKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("lo")
        val defaultIdentity = Locale.forLanguageTag("lo-Laoo-LA")
        assertEquals("lo", locale.toLanguageTag())
        assertEquals("lo", locale.language)
        assertEquals("Laoo", defaultIdentity.script)
        assertEquals("LA", defaultIdentity.country)
        assertEquals("ລາວ", nativeLocaleName("lo"))
        assertTrue(projectFile("app/src/main/res/values-lo/strings.xml").isFile)
        listOf("values-lo-rLA", "values-b+lo+Laoo", "values-b+lo+Laoo+LA").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(laoStrings.any { it in '\u0E80'..'\u0EFF' })
        assertTrue(laoStrings.contains("name=\"routing_notification_title\">Private Audio ເປີດຢູ່</string>"))
        assertTrue(laoStrings.contains("name=\"state_active\">ກຳລັງໃຊ້ງານ</string>"))
        assertFalse(laoStrings.contains("name=\"state_active\">ເປີດ</string>"))
        assertEquals(
            mapOf("state_ready" to "ພ້ອມ", "state_waiting" to "ກຳລັງລໍຖ້າ", "state_active" to "ກຳລັງໃຊ້ງານ", "state_error" to "ຂໍ້ຜິດພາດ"),
            frozenStates(laoStrings),
        )
        listOf("ກັບ AI", ">ມິນິ</string>", ">ການຕັ້ງຄ່າ</string>", ">ຄ່າເລີ່ມຕົ້ນ</string>", ">ຂັ້ນສູງ</string>", "ຊຸດຫູຟັງໃນຕົວໂທລະສັບ", "ອຸປະກອນສຽງອອກ", "ປ່ຽນອຸປະກອນສຽງອອກ", "ລາຍງານການວິນິດໄສ").forEach {
            assertTrue(it, laoStrings.contains(it))
        }
        assertTrue(laoStrings.contains("ປຸ່ມສຳລັບເປີດ/ປິດ Private Audio, ຂະຫຍາຍແຜງຄວບຄຸມ ແລະ ປິດແຜງຄວບຄຸມ"))
        assertTrue(laoStrings.contains("Private Audio") && laoStrings.contains("Android 13"))
        assertFalse(laoStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(projectFile("app/src/main/res/values-lo/mini_state_strings.xml").exists())
    }

    @Test
    fun azerbaijaniVariantsKeepDistinctQualifiersScriptsAndFrozenSemantics() {
        val northernIdentity = Locale.forLanguageTag("az-Latn-AZ")
        val iranianIdentity = Locale.forLanguageTag("az-Arab-IR")
        assertEquals("Latn", northernIdentity.script)
        assertEquals("AZ", northernIdentity.country)
        assertEquals("Arab", iranianIdentity.script)
        assertEquals("IR", iranianIdentity.country)
        assertTrue(projectFile("app/src/main/res/values-az/strings.xml").isFile)
        assertTrue(projectFile("app/src/main/res/values-b+az+Arab+IR/strings.xml").isFile)
        listOf(
            "values-az-rAZ", "values-b+az+Latn", "values-b+az+Latn+AZ",
            "values-az-rIR", "values-b+az+Arab", "values-azb", "values-az-rRU",
            "values-b+az+Cyrl", "values-b+az+Cyrl+RU",
        ).forEach { assertFalse(projectFile("app/src/main/res/$it").exists()) }

        assertTrue(northernAzerbaijaniStrings.contains("name=\"routing_notification_title\">Private Audio açıqdır</string>"))
        assertTrue(northernAzerbaijaniStrings.contains("name=\"state_active\">Aktiv</string>"))
        assertFalse(northernAzerbaijaniStrings.contains("name=\"state_active\">Açıq</string>"))
        assertEquals(mapOf("state_ready" to "Hazır", "state_waiting" to "Gözləyir", "state_active" to "Aktiv", "state_error" to "Xəta"), frozenStates(northernAzerbaijaniStrings))
        listOf("Sİ ilə", ">Mini</string>", ">Ayarlar</string>", ">Defolt</string>", ">Qabaqcıl</string>", "daxili qulaq dinamiki", "Audio çıxışını", "Diaqnostik hesabat").forEach {
            assertTrue(it, northernAzerbaijaniStrings.contains(it))
        }

        assertTrue(iranianAzerbaijaniStrings.any { it in '\u0600'..'\u06FF' })
        assertTrue(iranianAzerbaijaniStrings.contains("name=\"routing_notification_title\">Private Audio آچیقدیر</string>"))
        assertTrue(iranianAzerbaijaniStrings.contains("name=\"state_active\">فعال</string>"))
        assertFalse(iranianAzerbaijaniStrings.contains("name=\"state_active\">آچیق</string>"))
        assertEquals(mapOf("state_ready" to "حاضیر", "state_waiting" to "گؤزله‌ییر", "state_active" to "فعال", "state_error" to "خطا"), frozenStates(iranianAzerbaijaniStrings))
        listOf("یاپای ذکاء", ">مینی</string>", ">آیارلار</string>", ">فرض ائدیلن</string>", ">گئنیشمیش</string>", "داخیل قولاق دینامیکی", "آودیو چیخیشی", "دیاقنوستیک حسابات").forEach {
            assertTrue(it, iranianAzerbaijaniStrings.contains(it))
        }
        assertFalse(northernAzerbaijaniStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(iranianAzerbaijaniStrings.contains("settings_fake_phone_pre_arm"))
        assertFalse(projectFile("app/src/main/res/values-az/mini_state_strings.xml").exists())
        assertFalse(projectFile("app/src/main/res/values-b+az+Arab+IR/mini_state_strings.xml").exists())
    }

    @Test
    fun amharicKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("am")
        val defaultIdentity = Locale.forLanguageTag("am-Ethi-ET")
        assertEquals("am", locale.toLanguageTag())
        assertEquals("am", locale.language)
        assertEquals("Ethi", defaultIdentity.script)
        assertEquals("ET", defaultIdentity.country)
        assertEquals("አማርኛ", nativeLocaleName("am"))
        assertTrue(projectFile("app/src/main/res/values-am/strings.xml").isFile)
        listOf("values-am-rET", "values-b+am+Ethi", "values-b+am+Ethi+ET").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(amharicStrings.any { it in '\u1200'..'\u137F' })
        assertTrue(amharicStrings.contains("name=\"routing_notification_title\">Private Audio በርቷል</string>"))
        assertTrue(amharicStrings.contains("name=\"state_active\">ገባሪ</string>"))
        assertFalse(amharicStrings.contains("name=\"state_active\">በርቷል</string>"))
        assertEquals(
            mapOf("state_ready" to "ዝግጁ", "state_waiting" to "በመጠበቅ ላይ", "state_active" to "ገባሪ", "state_error" to "ስህተት"),
            frozenStates(amharicStrings),
        )
        assertTrue(amharicStrings.contains("name=\"product_subtitle\">ከAI ጋር"))
        assertTrue(amharicStrings.contains("name=\"floating\">ሚኒ</string>"))
        assertTrue(amharicStrings.contains("name=\"settings_advanced\">የላቁ ቅንብሮች</string>"))
        assertTrue(amharicStrings.contains("name=\"settings_system_default\">ነባሪ</string>"))
        assertTrue(amharicStrings.contains("name=\"settings_about_body\">Private Audio የሚደገፍ የድምፅ ኦዲዮን ወደ የስልክዎ አብሮገነብ መስሚያ ለመቀየር ያግዛል።</string>"))
        assertTrue(amharicStrings.contains("የስልክዎ አብሮገነብ መስሚያ"))
        assertFalse(amharicStrings.contains("የጆሮ ማዳመጫ"))
        assertFalse(projectFile("app/src/main/res/values-am/mini_state_strings.xml").exists())
    }

    @Test
    fun hebrewFrozenLocalizationSemanticsRemainIntact() {
        val logicalLocale = Locale.forLanguageTag("he")
        val legacyQualifierLocale = Locale.forLanguageTag("iw")
        assertEquals("he", logicalLocale.language)
        assertEquals("he", logicalLocale.toLanguageTag())
        assertEquals(logicalLocale, legacyQualifierLocale)
        assertEquals("he", legacyQualifierLocale.toLanguageTag())
        assertEquals("עברית", nativeLocaleName("he"))
        // Android's legacy qualifier keeps API 31-34 matching while Locale canonicalization exposes `he`.
        assertTrue(projectFile("app/src/main/res/values-iw/strings.xml").isFile)
        assertFalse(projectFile("app/src/main/res/values-he/strings.xml").exists())
        assertFalse(projectFile("app/src/main/res/values-ji/strings.xml").readText() == hebrewStrings)
        assertTrue(hebrewStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(hebrewStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(hebrewStrings.contains("דברו עם AI בפרטיות"))
        assertTrue(hebrewStrings.contains("name=\"routing_notification_title\">Private Audio מופעל</string>"))
        assertTrue(hebrewStrings.contains("name=\"state_active\">פעיל</string>"))
        assertFalse(hebrewStrings.contains("name=\"state_active\">מופעל</string>"))
        assertTrue(hebrewStrings.contains("name=\"routing_notification_text\">בהמתנה להחלפת פלט האודיו</string>"))
        assertTrue(hebrewStrings.contains("name=\"settings_language_body\"") && hebrewStrings.contains("שפת המכשיר"))
        assertTrue(hebrewStrings.contains("name=\"settings_language_android_13_required\"") && hebrewStrings.contains("שפת המערכת"))
        assertTrue(hebrewStrings.contains("name=\"settings_system_default\">ברירת המחדל של המערכת</string>"))
        assertTrue(hebrewStrings.contains("name=\"settings_proximity_screen\">כיבוי המסך כשהטלפון ליד האוזן</string>"))
        assertTrue(hebrewStrings.contains("אוזניה המובנית בחלק העליון של הטלפון"))
        assertTrue(hebrewStrings.contains("להפעלה/כיבוי, להרחבה ולסגירה"))
        assertTrue(hebrewStrings.contains("name=\"state_ready\">מוכן</string>"))
        assertTrue(hebrewStrings.contains("name=\"state_waiting\">בהמתנה</string>"))
        assertTrue(hebrewStrings.contains("name=\"state_active\">פעיל</string>"))
        assertTrue(hebrewStrings.contains("name=\"state_error\">שגיאה</string>"))
    }

    @Test
    fun indonesianFrozenLocalizationSemanticsRemainIntact() {
        val logicalLocale = Locale.forLanguageTag("id")
        val legacyQualifierLocale = Locale.forLanguageTag("in")
        assertEquals("id", logicalLocale.language)
        assertEquals("id", logicalLocale.toLanguageTag())
        assertEquals(logicalLocale, legacyQualifierLocale)
        assertEquals("id", legacyQualifierLocale.toLanguageTag())
        assertEquals("Indonesia", nativeLocaleLanguage("id"))
        assertTrue(indonesianStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(indonesianStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(indonesianStrings.contains("Bicara dengan AI secara pribadi"))
        assertTrue(indonesianStrings.contains("name=\"routing_notification_title\">Private Audio diaktifkan</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_active\">Aktif</string>"))
        assertFalse(indonesianStrings.contains("name=\"routing_notification_title\">Private Audio aktif</string>"))
        assertTrue(indonesianStrings.contains("name=\"routing_notification_text\">Menunggu pengalihan audio</string>"))
        assertTrue(indonesianStrings.contains("name=\"settings_system_default\">Default</string>"))
        assertTrue(indonesianStrings.contains("name=\"settings_language_body\"") && indonesianStrings.contains("bahasa perangkat Anda"))
        assertTrue(indonesianStrings.contains("name=\"settings_language_android_13_required\"") && indonesianStrings.contains("bahasa sistem"))
        assertTrue(indonesianStrings.contains("name=\"settings_proximity_screen\">Matikan layar saat dekat telinga</string>"))
        assertTrue(indonesianStrings.contains("earpiece bawaan ponsel"))
        assertTrue(indonesianStrings.contains("mengaktifkan, memperluas, dan menutup"))
        assertTrue(indonesianStrings.contains("name=\"state_ready\">Siap</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_waiting\">Menunggu</string>"))
        assertTrue(indonesianStrings.contains("name=\"state_error\">Error</string>"))
    }

    @Test
    fun modernLogicalLocalesKeepLegacyAndroidResourceQualifiers() {
        mapOf("id" to "in", "he" to "iw", "yi" to "ji").forEach { (modernTag, legacyQualifier) ->
            assertEquals(modernTag, Locale.forLanguageTag(modernTag).toLanguageTag())
            assertEquals(Locale.forLanguageTag(modernTag), Locale.forLanguageTag(legacyQualifier))
            assertTrue(projectFile("app/src/main/res/values-$legacyQualifier/strings.xml").isFile)
            assertFalse(projectFile("app/src/main/res/values-$modernTag/strings.xml").exists())
        }
    }

    @Test
    fun yiddishFrozenLocalizationSemanticsRemainIntact() {
        val logicalLocale = Locale.forLanguageTag("yi")
        val legacyQualifierLocale = Locale.forLanguageTag("ji")
        assertEquals("yi", logicalLocale.language)
        assertEquals("yi", logicalLocale.toLanguageTag())
        assertEquals(logicalLocale, legacyQualifierLocale)
        assertEquals("yi", legacyQualifierLocale.toLanguageTag())
        assertEquals("ייִדיש", nativeLocaleName("yi"))
        // Android's legacy qualifier keeps API 31-34 matching while Locale canonicalization exposes `yi`.
        assertTrue(projectFile("app/src/main/res/values-ji/strings.xml").isFile)
        assertFalse(projectFile("app/src/main/res/values-yi/strings.xml").exists())
        assertFalse(projectFile("app/src/main/res/values-iw/strings.xml").readText() == yiddishStrings)
        assertTrue(yiddishStrings.contains("name=\"app_name\">Private Audio</string>"))
        assertTrue(yiddishStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(yiddishStrings.contains("רעדט פריוואט מיט AI"))
        assertFalse(yiddishStrings.contains("איי־אײַ"))
        assertTrue(yiddishStrings.contains("name=\"routing_notification_title\">Private Audio איז אנגעצונדן</string>"))
        assertTrue(yiddishStrings.contains("name=\"state_active\">אקטיוו</string>"))
        assertFalse(yiddishStrings.contains("name=\"state_active\">אנגעצונדן</string>"))
        assertTrue(yiddishStrings.contains("name=\"routing_notification_text\">מען ווארט צו איבערשטעלן דעם אודיא</string>"))
        assertTrue(yiddishStrings.contains("name=\"settings\">סעטינגס</string>"))
        assertTrue(yiddishStrings.contains("name=\"settings_system_default\">סיסטעם דיפאלט</string>"))
        assertTrue(yiddishStrings.contains("name=\"settings_language_body\"") && yiddishStrings.contains("שפראך פונעם מכשיר"))
        assertTrue(yiddishStrings.contains("name=\"settings_language_android_13_required\"") && yiddishStrings.contains("סיסטעם שפראך"))
        assertTrue(yiddishStrings.contains("name=\"settings_proximity_screen\"") && yiddishStrings.contains("אויסלעשן דעם סקרין"))
        assertTrue(yiddishStrings.contains("ווען דער פאון איז נאנט צום אויער"))
        assertTrue(yiddishStrings.contains("איינגעבויטן טרייבל אויבן אויפן פאון"))
        assertTrue(yiddishStrings.contains("וואס מען האלט צום אויער ביי א טעלעפאן רוף"))
        assertTrue(yiddishStrings.contains("אנצינדן/אויסלעשן, פארגרעסערן און פארמאכן"))
        assertTrue(yiddishStrings.contains("name=\"state_ready\">גרייט</string>"))
        assertTrue(yiddishStrings.contains("name=\"state_waiting\">ווארטנדיג</string>"))
        assertTrue(yiddishStrings.contains("name=\"state_active\">אקטיוו</string>"))
        assertTrue(yiddishStrings.contains("name=\"state_error\">פעלער</string>"))
    }

    @Test
    fun punjabiScriptLocalesKeepExactQualifiersAndFrozenSemantics() {
        val gurmukhiLocale = Locale.forLanguageTag("pa-Guru-IN")
        val shahmukhiLocale = Locale.forLanguageTag("pa-Arab-PK")
        assertEquals("pa-Guru-IN", gurmukhiLocale.toLanguageTag())
        assertEquals("Guru", gurmukhiLocale.script)
        assertEquals("IN", gurmukhiLocale.country)
        assertEquals("pa-Arab-PK", shahmukhiLocale.toLanguageTag())
        assertEquals("Arab", shahmukhiLocale.script)
        assertEquals("PK", shahmukhiLocale.country)
        assertTrue(projectFile("app/src/main/res/values-b+pa+Guru+IN/strings.xml").isFile)
        assertTrue(projectFile("app/src/main/res/values-b+pa+Arab+PK/strings.xml").isFile)

        assertTrue(gurmukhiPunjabiStrings.contains("name=\"routing_notification_title\">Private Audio ਚਾਲੂ ਹੈ</string>"))
        assertTrue(gurmukhiPunjabiStrings.contains("name=\"product_subtitle\">AI ਨਾਲ"))
        assertTrue(gurmukhiPunjabiStrings.contains("name=\"floating\">ਕੰਪੈਕਟ</string>"))
        assertTrue(gurmukhiPunjabiStrings.contains("ਬਿਲਟ-ਇਨ ਈਅਰਪੀਸ"))
        assertEquals(
            mapOf("state_ready" to "ਤਿਆਰ", "state_waiting" to "ਉਡੀਕ", "state_active" to "ਸਰਗਰਮ", "state_error" to "ਤਰੁੱਟੀ"),
            frozenStates(gurmukhiPunjabiStrings),
        )
        assertFalse(gurmukhiPunjabiStrings.contains("name=\"state_active\">ਚਾਲੂ</string>"))

        assertTrue(shahmukhiPunjabiStrings.contains("name=\"routing_notification_title\">Private Audio چالو اے</string>"))
        assertTrue(shahmukhiPunjabiStrings.contains("name=\"product_subtitle\">AI نال"))
        assertTrue(shahmukhiPunjabiStrings.contains("name=\"floating\">کمپیکٹ</string>"))
        assertTrue(shahmukhiPunjabiStrings.contains("بِلٹ اِن ایئر پیس"))
        assertTrue(shahmukhiPunjabiStrings.contains("name=\"settings_advanced\">ایڈوانسڈ</string>"))
        assertFalse(shahmukhiPunjabiStrings.contains("ہے"))
        assertEquals(
            mapOf("state_ready" to "تیار", "state_waiting" to "اُڈیک", "state_active" to "فعال", "state_error" to "خرابی"),
            frozenStates(shahmukhiPunjabiStrings),
        )
        assertFalse(shahmukhiPunjabiStrings.contains("name=\"state_active\">چالو</string>"))
    }

    @Test
    fun pashtoKeepsOneNeutralQualifierAndFrozenRtlSemantics() {
        val locale = Locale.forLanguageTag("ps")
        assertEquals("ps", locale.toLanguageTag())
        assertEquals("پښتو", nativeLocaleName("ps"))
        assertTrue(projectFile("app/src/main/res/values-ps/strings.xml").isFile)
        listOf("values-ps-rAF", "values-ps-rPK", "values-b+ps+Arab+AF", "values-b+ps+Arab+PK").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(pashtoStrings.any { it in '\u0750'..'\u077F' } || pashtoStrings.contains('ښ'))
        assertTrue(pashtoStrings.contains("name=\"routing_notification_title\">Private Audio چالان دی</string>"))
        assertTrue(pashtoStrings.contains("name=\"state_active\">فعال</string>"))
        assertFalse(pashtoStrings.contains("name=\"state_active\">چالان</string>"))
        assertEquals(
            mapOf("state_ready" to "چمتو", "state_waiting" to "په تمه", "state_active" to "فعال", "state_error" to "تېروتنه"),
            frozenStates(pashtoStrings),
        )
        assertTrue(pashtoStrings.contains("له AI سره"))
        assertTrue(pashtoStrings.contains("name=\"floating\">مینی</string>"))
        assertTrue(pashtoStrings.contains("name=\"settings_advanced\">پرمختللی</string>"))
        assertTrue(pashtoStrings.contains("د تلیفون رسیدونکي ته"))
        assertFalse(pashtoStrings.contains("سپیکر"))
        assertFalse(pashtoStrings.contains("غوږۍ"))
        assertFalse(projectFile("app/src/main/res/values-ps/mini_state_strings.xml").exists())
    }

    @Test
    fun hausaKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("ha")
        assertEquals("ha", locale.toLanguageTag())
        assertEquals("Hausa", nativeLocaleName("ha"))
        assertTrue(projectFile("app/src/main/res/values-ha/strings.xml").isFile)
        listOf(
            "values-ha-rNG", "values-ha-rNE", "values-ha-rGH",
            "values-b+ha+Latn+NG", "values-b+ha+Latn+NE", "values-b+ha+Latn+GH",
            "values-b+ha+Arab", "values-b+ha+Arab+NG",
        ).forEach { assertFalse(projectFile("app/src/main/res/$it").exists()) }
        assertTrue(hausaStrings.contains('ƙ') && hausaStrings.contains('ɗ'))
        assertFalse(hausaStrings.any { it in '\u0600'..'\u06FF' })
        assertTrue(hausaStrings.contains("name=\"routing_notification_title\">An kunna Private Audio</string>"))
        assertTrue(hausaStrings.contains("name=\"state_active\">Ana aiki</string>"))
        assertFalse(hausaStrings.contains("name=\"state_active\">An kunna</string>"))
        assertEquals(
            mapOf("state_ready" to "A shirye", "state_waiting" to "Ana jira", "state_active" to "Ana aiki", "state_error" to "Kuskure"),
            frozenStates(hausaStrings),
        )
        assertTrue(hausaStrings.contains("magana da AI"))
        assertTrue(hausaStrings.contains("name=\"floating\">Ƙarami</string>"))
        assertFalse(hausaStrings.contains("name=\"floating\">Mini</string>"))
        assertFalse(hausaStrings.contains("name=\"floating\">mini</string>"))
        assertTrue(hausaStrings.contains("Ƙaramin mai sarrafa Private Audio"))
        assertFalse(hausaStrings.contains("Mini mai sarrafa Private Audio"))
        assertTrue(hausaStrings.contains("name=\"settings_advanced\">Ci gaba</string>"))
        assertTrue(hausaStrings.contains("name=\"settings_system_default\">Na asali</string>"))
        assertTrue(hausaStrings.contains("lasifikar kunne da ke cikin wayarka"))
        listOf("loudspeaker", "speakerphone", "headphones", "earbuds", "Bluetooth").forEach {
            assertFalse(hausaStrings.contains(it, ignoreCase = true))
        }
        assertFalse(projectFile("app/src/main/res/values-ha/mini_state_strings.xml").exists())
    }

    @Test
    fun zuluKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("zu")
        assertEquals("zu", locale.toLanguageTag())
        assertEquals("isiZulu", nativeLocaleName("zu"))
        val defaultIdentity = Locale.forLanguageTag("zu-Latn-ZA")
        assertEquals("zu", defaultIdentity.language)
        assertEquals("Latn", defaultIdentity.script)
        assertEquals("ZA", defaultIdentity.country)
        assertTrue(projectFile("app/src/main/res/values-zu/strings.xml").isFile)
        listOf("values-zu-rZA", "values-b+zu+Latn", "values-b+zu+Latn+ZA").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(zuluStrings.filterNot { it.isWhitespace() }.all { it.code < 0x0250 })
        assertTrue(zuluStrings.contains("name=\"routing_notification_title\">I-Private Audio ivuliwe</string>"))
        assertTrue(zuluStrings.contains("name=\"state_active\">Iyasebenza</string>"))
        assertFalse(zuluStrings.contains("name=\"state_active\">ivuliwe</string>"))
        assertEquals(
            mapOf("state_ready" to "Ilungile", "state_waiting" to "Iyalinda", "state_active" to "Iyasebenza", "state_error" to "Iphutha"),
            frozenStates(zuluStrings),
        )
        assertTrue(zuluStrings.contains("name=\"routing_notification_text\">Iyalinda ukushintsha okukhiphayo komsindo</string>"))
        assertFalse(zuluStrings.contains("Iyalinda ukushintsha lapho umsindo uphuma khona"))
        assertTrue(zuluStrings.contains("Khuluma ne-AI"))
        assertTrue(zuluStrings.contains("name=\"floating\">Mini</string>"))
        assertFalse(zuluStrings.contains("name=\"floating\">imini</string>"))
        assertTrue(zuluStrings.contains("name=\"settings_system_default\">Okuzenzakalelayo</string>"))
        assertTrue(zuluStrings.contains("name=\"settings_advanced\">Izilungiselelo ezithuthukisiwe</string>"))
        assertTrue(zuluStrings.contains("ulimi lwedivayisi yakho"))
        assertTrue(zuluStrings.contains("Le divayisi ilandela ulimi lwesistimu."))
        assertTrue(zuluStrings.contains("Cisha isikrini uma ifoni iseduze nendlebe yakho"))
        assertTrue(zuluStrings.contains("isipikha sendlebe"))
        assertTrue(zuluStrings.contains("esipikheni sendlebe sefoni yakho"))
        assertFalse(zuluStrings.contains("name=\"settings_about_body\">I-Private Audio isiza ukudlulisela umsindo wezwi osekelwayo esipikheni sefoni yakho.</string>"))
        assertTrue(zuluStrings.contains("ukuvula/ukucisha, ukunweba nokuvala"))
        assertFalse(projectFile("app/src/main/res/values-zu/mini_state_strings.xml").exists())
    }

    @Test
    fun odiaKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("or")
        val defaultIdentity = Locale.forLanguageTag("or-Orya-IN")
        assertEquals("or", locale.toLanguageTag())
        assertEquals("or", locale.language)
        assertEquals("Orya", defaultIdentity.script)
        assertEquals("IN", defaultIdentity.country)
        assertEquals("ଓଡ଼ିଆ", nativeLocaleName("or"))
        assertTrue(projectFile("app/src/main/res/values-or/strings.xml").isFile)
        listOf("values-or-rIN", "values-b+or+Orya", "values-b+or+Orya+IN").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(odiaStrings.any { it in '\u0B00'..'\u0B7F' })
        assertTrue(odiaStrings.contains("name=\"routing_notification_title\">Private Audio ଚାଲୁ ଅଛି</string>"))
        assertTrue(odiaStrings.contains("name=\"state_active\">ସକ୍ରିୟ</string>"))
        assertFalse(odiaStrings.contains("name=\"state_active\">ଚାଲୁ ଅଛି</string>"))
        assertEquals(
            mapOf("state_ready" to "ପ୍ରସ୍ତୁତ", "state_waiting" to "ଅପେକ୍ଷାରତ", "state_active" to "ସକ୍ରିୟ", "state_error" to "ତ୍ରୁଟି"),
            frozenStates(odiaStrings),
        )
        assertTrue(odiaStrings.contains("name=\"routing_notification_text\">ଅଡିଓ ଆଉଟପୁଟ ସ୍ୱିଚ୍ କରିବାକୁ ଅପେକ୍ଷା କରାଯାଉଛି</string>"))
        assertTrue(odiaStrings.contains("name=\"product_subtitle\">ଫୋନ୍ କଲ୍ ପରି, ଏଆଇ ସହିତ ବ୍ୟକ୍ତିଗତ ଭାବେ କଥା ହୁଅନ୍ତୁ।</string>"))
        assertFalse(odiaStrings.contains("ଗୋପନୀୟ ଭାବେ"))
        assertTrue(odiaStrings.contains("name=\"floating\">ମିନି</string>"))
        assertTrue(odiaStrings.contains("name=\"settings_system_default\">ଡିଫଲ୍ଟ</string>"))
        assertTrue(odiaStrings.contains("name=\"settings_advanced\">ଉନ୍ନତ ସେଟିଂସ</string>"))
        assertTrue(odiaStrings.contains("ଆପଣଙ୍କ ଫୋନର ବିଲ୍ଟ-ଇନ୍ ହ୍ୟାଣ୍ଡସେଟ୍ ଇୟରପିସ୍"))
        assertFalse(odiaStrings.contains("ଫୋନର ବିଲ୍ଟ-ଇନ୍ ଇୟରପିସ୍"))
        assertTrue(odiaStrings.contains("ଚାଲୁ/ବନ୍ଦ କରିବା, ବଢ଼ାଇବା ଏବଂ କଣ୍ଟ୍ରୋଲର୍ ବନ୍ଦ କରିବା"))
        assertFalse(projectFile("app/src/main/res/values-or/mini_state_strings.xml").exists())
    }

    @Test
    fun burmeseKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("my")
        val defaultIdentity = Locale.forLanguageTag("my-Mymr-MM")
        assertEquals("my", locale.toLanguageTag())
        assertEquals("Mymr", defaultIdentity.script)
        assertEquals("MM", defaultIdentity.country)
        assertEquals("မြန်မာ", nativeLocaleName("my"))
        assertTrue(projectFile("app/src/main/res/values-my/strings.xml").isFile)
        listOf("values-my-rMM", "values-b+my+Mymr", "values-b+my+Mymr+MM").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(burmeseStrings.any { it in '\u1000'..'\u109F' })
        assertTrue(burmeseStrings.contains("name=\"routing_notification_title\">Private Audio ဖွင့်ထားသည်</string>"))
        assertTrue(burmeseStrings.contains("name=\"state_active\">အသုံးပြုနေသည်</string>"))
        assertFalse(burmeseStrings.contains("name=\"state_active\">ဖွင့်ထားသည်</string>"))
        assertEquals(
            mapOf("state_ready" to "အဆင်သင့်", "state_waiting" to "စောင့်နေသည်", "state_active" to "အသုံးပြုနေသည်", "state_error" to "အမှား"),
            frozenStates(burmeseStrings),
        )
        assertTrue(burmeseStrings.contains("name=\"routing_notification_text\">အသံအထွက် ပြောင်းရန် စောင့်နေသည်</string>"))
        assertTrue(burmeseStrings.contains("name=\"product_subtitle\">ဖုန်းပြောသလို အေအိုင်နှင့် သီးသန့် စကားပြောပါ။</string>"))
        assertTrue(burmeseStrings.contains("name=\"floating\">မီနီ</string>"))
        assertTrue(burmeseStrings.contains("name=\"settings_system_default\">မူရင်း</string>"))
        assertTrue(burmeseStrings.contains("name=\"settings_advanced\">အဆင့်မြင့်အပြင်အဆင်များ</string>"))
        assertTrue(burmeseStrings.contains("တယ်လီဖုန်းနားခွက်သို့"))
        assertTrue(burmeseStrings.contains("ပါဝါဖွင့်/ပိတ်ရန်၊ တိုးချဲ့ရန်နှင့် ထိန်းချုပ်ကိရိယာကို ပိတ်ရန်"))
        assertEquals(3, burmeseStrings.occurrences("ချို့ယွင်းချက်ရှာဖွေမှု အစီရင်ခံစာ"))
        assertFalse(projectFile("app/src/main/res/values-my/mini_state_strings.xml").exists())
    }

    @Test
    fun uzbekKeepsOneLatinQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("uz")
        val defaultIdentity = Locale.forLanguageTag("uz-Latn-UZ")
        assertEquals("uz", locale.toLanguageTag())
        assertEquals("Latn", defaultIdentity.script)
        assertEquals("UZ", defaultIdentity.country)
        assertEquals("O‘zbek", nativeLocaleName("uz"))
        assertTrue(projectFile("app/src/main/res/values-uz/strings.xml").isFile)
        listOf("values-uz-rUZ", "values-b+uz+Latn", "values-b+uz+Latn+UZ", "values-b+uz+Cyrl", "values-b+uz+Cyrl+UZ").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertFalse(uzbekStrings.any { it in '\u0400'..'\u04FF' })
        assertTrue(uzbekStrings.contains("name=\"routing_notification_title\">Private Audio yoniq</string>"))
        assertTrue(uzbekStrings.contains("name=\"state_active\">Faol</string>"))
        assertFalse(uzbekStrings.contains("name=\"state_active\">Yoniq</string>"))
        assertEquals(
            mapOf("state_ready" to "Tayyor", "state_waiting" to "Kutilmoqda", "state_active" to "Faol", "state_error" to "Xatolik"),
            frozenStates(uzbekStrings),
        )
        assertTrue(uzbekStrings.contains("name=\"routing_notification_text\">Audio chiqishini almashtirish kutilmoqda</string>"))
        assertTrue(uzbekStrings.contains("AI bilan xuddi telefon qo‘ng‘irog‘idek, boshqalarga eshittirmay gaplashing."))
        assertTrue(uzbekStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(uzbekStrings.contains("name=\"settings_system_default\">Birlamchi</string>"))
        assertTrue(uzbekStrings.contains("name=\"settings_advanced\">Kengaytirilgan sozlamalar</string>"))
        assertTrue(uzbekStrings.contains("telefoningizning ichki quloq karnaychasiga"))
        assertTrue(uzbekStrings.contains("nutq audiosini"))
        assertFalse(uzbekStrings.contains("ovozli audioni"))
        assertTrue(uzbekStrings.contains("Yoqish/o‘chirish, kengaytirish va yopish boshqaruvlari"))
        assertFalse(projectFile("app/src/main/res/values-uz/mini_state_strings.xml").exists())
    }

    @Test
    fun khmerKeepsOneNeutralQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("km")
        val defaultIdentity = Locale.forLanguageTag("km-Khmr-KH")
        assertEquals("km", locale.toLanguageTag())
        assertEquals("Khmr", defaultIdentity.script)
        assertEquals("KH", defaultIdentity.country)
        assertEquals("ខ្មែរ", nativeLocaleName("km"))
        assertTrue(projectFile("app/src/main/res/values-km/strings.xml").isFile)
        listOf("values-km-rKH", "values-b+km+Khmr", "values-b+km+Khmr+KH").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(khmerStrings.any { it in '\u1780'..'\u17FF' })
        assertTrue(khmerStrings.contains("name=\"routing_notification_title\">Private Audio បានបើក</string>"))
        assertTrue(khmerStrings.contains("name=\"state_active\">សកម្ម</string>"))
        assertFalse(khmerStrings.contains("name=\"state_active\">បានបើក</string>"))
        assertEquals(
            mapOf("state_ready" to "ត្រៀមរួចរាល់", "state_waiting" to "កំពុងរង់ចាំ", "state_active" to "សកម្ម", "state_error" to "កំហុស"),
            frozenStates(khmerStrings),
        )
        assertTrue(khmerStrings.contains("name=\"routing_notification_text\">កំពុងរង់ចាំប្ដូរឧបករណ៍បញ្ចេញសំឡេង</string>"))
        assertTrue(khmerStrings.contains("និយាយជាមួយ AI ជាលក្ខណៈឯកជន"))
        assertTrue(khmerStrings.contains("name=\"floating\">Mini</string>"))
        assertTrue(khmerStrings.contains("name=\"settings_system_default\">លំនាំដើម</string>"))
        assertTrue(khmerStrings.contains("name=\"settings_advanced\">កម្រិតខ្ពស់</string>"))
        assertTrue(khmerStrings.contains("ឧបករណ៍ស្ដាប់សំឡេងដែលមានស្រាប់ក្នុងទូរសព្ទរបស់អ្នក"))
        assertEquals(3, khmerStrings.occurrences("របាយការណ៍វិនិច្ឆ័យ"))
        assertTrue(khmerStrings.contains("សម្រាប់បើក ឬបិទ Private Audio សម្រាប់ពង្រីកឧបករណ៍បញ្ជា និងសម្រាប់បិទឧបករណ៍បញ្ជា"))
        assertFalse(projectFile("app/src/main/res/values-km/mini_state_strings.xml").exists())
    }

    @Test
    fun assameseKeepsOneBengaliScriptQualifierAndFrozenLtrSemantics() {
        val locale = Locale.forLanguageTag("as")
        val defaultIdentity = Locale.forLanguageTag("as-Beng-IN")
        assertEquals("as", locale.toLanguageTag())
        assertEquals("Beng", defaultIdentity.script)
        assertEquals("IN", defaultIdentity.country)
        assertEquals("অসমীয়া", nativeLocaleName("as"))
        assertTrue(projectFile("app/src/main/res/values-as/strings.xml").isFile)
        listOf("values-as-rIN", "values-b+as+Beng", "values-b+as+Beng+IN").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
        assertTrue(assameseStrings.contains("name=\"routing_notification_title\">Private Audio অন আছে</string>"))
        assertTrue(assameseStrings.contains("name=\"state_active\">সক্ৰিয়</string>"))
        assertFalse(assameseStrings.contains("name=\"state_active\">অন আছে</string>"))
        assertEquals(
            mapOf("state_ready" to "সাজু", "state_waiting" to "অপেক্ষাৰত", "state_active" to "সক্ৰিয়", "state_error" to "ত্ৰুটি"),
            frozenStates(assameseStrings),
        )
        assertTrue(assameseStrings.contains("name=\"routing_notification_text\">অডিঅ’ আউটপুট সলনি কৰিবলৈ অপেক্ষা কৰি আছে</string>"))
        assertTrue(assameseStrings.contains("name=\"product_subtitle\">ফ’ন কলৰ দৰে AIৰ সৈতে ব্যক্তিগতভাৱে কথা পাতক।</string>"))
        assertTrue(assameseStrings.contains("name=\"floating\">মিনি</string>"))
        assertTrue(assameseStrings.contains("name=\"settings_system_default\">ডিফ’ল্ট</string>"))
        assertTrue(assameseStrings.contains("name=\"settings_advanced\">উচ্চখাপৰ</string>"))
        assertTrue(assameseStrings.contains("বিল্ট-ইন ইয়েৰপিচলৈ"))
        assertEquals(3, assameseStrings.occurrences("ডায়েগন’ষ্টিক ৰিপ’ৰ্ট"))
        assertTrue(assameseStrings.contains("পাৱাৰ অন বা অফ কৰিবলৈ, বিস্তাৰ কৰিবলৈ আৰু বন্ধ কৰিবলৈ"))
        assertFalse(projectFile("app/src/main/res/values-as/mini_state_strings.xml").exists())
    }

    @Test
    fun languageSelectionUsesPlatformConfigurationWithoutAParallelLocaleRegistry() {
        assertTrue(languagePreferencesSource.contains("LocaleConfig(context).supportedLocales"))
        assertTrue(languagePreferencesSource.contains("getSystemService(LocaleManager::class.java)"))
        assertTrue(languagePreferencesSource.contains("LocaleList.getEmptyLocaleList()"))
        assertTrue(languagePreferencesSource.contains("Build.VERSION_CODES.TIRAMISU"))
        assertFalse(languagePreferencesSource.contains("listOf(\"en-US\", \"pl\")"))
        assertFalse(languagePreferencesSource.contains("SharedPreferences"))
        assertTrue(settingsSource.contains("supportedLanguages.forEach"))
        assertTrue(settingsSource.contains("selectedLanguageTag == null"))
    }

    @Test
    fun existingOverlayRefreshesOnlyLocalizedPresentationOnConfigurationChange() {
        assertTrue(overlaySource.contains("override fun onConfigurationChanged"))
        assertTrue(overlaySource.contains("overlayView?.refreshLocalizedPresentation()"))
        assertTrue(overlaySource.contains("contentDescription = stateDescription(state)"))
        assertFalse(overlaySource.method("override fun onConfigurationChanged").contains("hideOverlay()"))
        assertFalse(overlaySource.method("override fun onConfigurationChanged").contains("bindControllerService()"))
    }

    @Test
    fun productSurfacesUseResourcesWhileDiagnosticsStayOutsideLocalization() {
        assertFalse(mainSource.contains("ClipboardManager"))
        assertTrue(mainSource.contains("R.string.diagnostic_report_saved"))
        assertTrue(mainSource.contains("R.string.diagnostic_report_save_failed"))
        assertFalse(productScreenSource.contains("Text(\""))
        assertFalse(settingsSource.contains("Text(\""))
        assertTrue(diagnosticScreenSource.contains("Text(\"PRIVATE AUDIO\""))
    }

    @Test
    fun protectedRoutingAndReportFormatterRemainSingleAndUnchangedInOwnership() {
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertFalse(defaultStrings.contains("PRIVATE AUDIO DIAGNOSTIC REPORT"))
    }

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private fun String.method(signature: String): String =
        substring(indexOf(signature)).substringBefore("\n    }")

    private fun stringKeys(resources: String) =
        Regex("<string name=\"([^\"]+)\"").findAll(resources).map { it.groupValues[1] }.toList()

    private fun placeholders(resources: String) =
        Regex("<string name=\"([^\"]+)\">([^<]*)</string>").findAll(resources).associate {
            it.groupValues[1] to Regex("%\\d+\\$[a-z]").findAll(it.groupValues[2]).map { match -> match.value }.toList()
        }

    private fun frozenStates(resources: String) =
        Regex("<string name=\"(state_(?:ready|waiting|active|error))\">([^<]*)</string>")
            .findAll(resources).associate { it.groupValues[1] to it.groupValues[2] }

    private fun nativeLocaleName(languageTag: String): String {
        val locale = Locale.forLanguageTag(languageTag)
        return locale.getDisplayName(locale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(locale) else first.toString()
        }
    }

    private fun nativeLocaleLanguage(languageTag: String): String {
        val locale = Locale.forLanguageTag(languageTag)
        return locale.getDisplayLanguage(locale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(locale) else first.toString()
        }
    }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val appBuildSource = projectFile("app/build.gradle.kts").readText()
        val resourcesProperties = projectFile("app/src/main/res/resources.properties").readText()
        val defaultStrings = projectFile("app/src/main/res/values/strings.xml").readText()
        val malayStrings = projectFile("app/src/main/res/values-ms/strings.xml").readText()
        val italianStrings = projectFile("app/src/main/res/values-it/strings.xml").readText()
        val romanianStrings = projectFile("app/src/main/res/values-ro/strings.xml").readText()
        val swedishStrings = projectFile("app/src/main/res/values-sv/strings.xml").readText()
        val norwegianBokmalStrings = projectFile("app/src/main/res/values-nb/strings.xml").readText()
        val danishStrings = projectFile("app/src/main/res/values-da/strings.xml").readText()
        val icelandicStrings = projectFile("app/src/main/res/values-is/strings.xml").readText()
        val faroeseStrings = projectFile("app/src/main/res/values-fo/strings.xml").readText()
        val kannadaStrings = projectFile("app/src/main/res/values-kn/strings.xml").readText()
        val gujaratiStrings = projectFile("app/src/main/res/values-gu/strings.xml").readText()
        val marathiStrings = projectFile("app/src/main/res/values-mr/strings.xml").readText()
        val teluguStrings = projectFile("app/src/main/res/values-te/strings.xml").readText()
        val tamilStrings = projectFile("app/src/main/res/values-ta/strings.xml").readText()
        val thaiStrings = projectFile("app/src/main/res/values-th/strings.xml").readText()
        val ukrainianStrings = projectFile("app/src/main/res/values-uk/strings.xml").readText()
        val belarusianStrings = projectFile("app/src/main/res/values-be/strings.xml").readText()
        val lithuanianStrings = projectFile("app/src/main/res/values-lt/strings.xml").readText()
        val russianStrings = projectFile("app/src/main/res/values-ru/strings.xml").readText()
        val frenchStrings = projectFile("app/src/main/res/values-fr/strings.xml").readText()
        val spanishStrings = projectFile("app/src/main/res/values-es/strings.xml").readText()
        val brazilianPortugueseStrings = projectFile("app/src/main/res/values-pt-rBR/strings.xml").readText()
        val europeanPortugueseStrings = projectFile("app/src/main/res/values-pt-rPT/strings.xml").readText()
        val simplifiedChineseStrings = projectFile("app/src/main/res/values-b+zh+Hans/strings.xml").readText()
        val traditionalChineseStrings = projectFile("app/src/main/res/values-b+zh+Hant/strings.xml").readText()
        val hindiStrings = projectFile("app/src/main/res/values-hi/strings.xml").readText()
        val arabicStrings = projectFile("app/src/main/res/values-ar/strings.xml").readText()
        val indonesianStrings = projectFile("app/src/main/res/values-in/strings.xml").readText()
        val urduStrings = projectFile("app/src/main/res/values-ur/strings.xml").readText()
        val persianStrings = projectFile("app/src/main/res/values-fa/strings.xml").readText()
        val vietnameseStrings = projectFile("app/src/main/res/values-vi/strings.xml").readText()
        val nigerianPidginStrings = projectFile("app/src/main/res/values-pcm/strings.xml").readText()
        val japaneseStrings = projectFile("app/src/main/res/values-ja/strings.xml").readText()
        val swahiliStrings = projectFile("app/src/main/res/values-sw/strings.xml").readText()
        val finnishStrings = projectFile("app/src/main/res/values-fi/strings.xml").readText()
        val estonianStrings = projectFile("app/src/main/res/values-et/strings.xml").readText()
        val hungarianStrings = projectFile("app/src/main/res/values-hu/strings.xml").readText()
        val basqueStrings = projectFile("app/src/main/res/values-eu/strings.xml").readText()
        val albanianStrings = projectFile("app/src/main/res/values-sq/strings.xml").readText()
        val latvianStrings = projectFile("app/src/main/res/values-lv/strings.xml").readText()
        val dutchStrings = projectFile("app/src/main/res/values-nl/strings.xml").readText()
        val afrikaansStrings = projectFile("app/src/main/res/values-af/strings.xml").readText()
        val luxembourgishStrings = projectFile("app/src/main/res/values-lb/strings.xml").readText()
        val bulgarianStrings = projectFile("app/src/main/res/values-bg/strings.xml").readText()
        val serbianCyrillicStrings = projectFile("app/src/main/res/values-sr/strings.xml").readText()
        val macedonianStrings = projectFile("app/src/main/res/values-mk/strings.xml").readText()
        val slovenianStrings = projectFile("app/src/main/res/values-sl/strings.xml").readText()
        val croatianStrings = projectFile("app/src/main/res/values-hr/strings.xml").readText()
        val serbianLatinStrings = projectFile("app/src/main/res/values-b+sr+Latn/strings.xml").readText()
        val serbianLatinMontenegroStrings = projectFile("app/src/main/res/values-b+sr+Latn+ME/strings.xml").readText()
        val bosnianStrings = projectFile("app/src/main/res/values-bs/strings.xml").readText()
        val malteseStrings = projectFile("app/src/main/res/values-mt/strings.xml").readText()
        val greekStrings = projectFile("app/src/main/res/values-el/strings.xml").readText()
        val bengaliStrings = projectFile("app/src/main/res/values-bn/strings.xml").readText()
        val malayalamStrings = projectFile("app/src/main/res/values-ml/strings.xml").readText()
        val somaliStrings = projectFile("app/src/main/res/values-so/strings.xml").readText()
        val nepaliStrings = projectFile("app/src/main/res/values-ne/strings.xml").readText()
        val armenianStrings = projectFile("app/src/main/res/values-hy/strings.xml").readText()
        val mongolianStrings = projectFile("app/src/main/res/values-mn/strings.xml").readText()
        val georgianStrings = projectFile("app/src/main/res/values-ka/strings.xml").readText()
        val laoStrings = projectFile("app/src/main/res/values-lo/strings.xml").readText()
        val amharicStrings = projectFile("app/src/main/res/values-am/strings.xml").readText()
        val hebrewStrings = projectFile("app/src/main/res/values-iw/strings.xml").readText()
        val yiddishStrings = projectFile("app/src/main/res/values-ji/strings.xml").readText()
        val gurmukhiPunjabiStrings = projectFile("app/src/main/res/values-b+pa+Guru+IN/strings.xml").readText()
        val shahmukhiPunjabiStrings = projectFile("app/src/main/res/values-b+pa+Arab+PK/strings.xml").readText()
        val pashtoStrings = projectFile("app/src/main/res/values-ps/strings.xml").readText()
        val hausaStrings = projectFile("app/src/main/res/values-ha/strings.xml").readText()
        val zuluStrings = projectFile("app/src/main/res/values-zu/strings.xml").readText()
        val odiaStrings = projectFile("app/src/main/res/values-or/strings.xml").readText()
        val burmeseStrings = projectFile("app/src/main/res/values-my/strings.xml").readText()
        val uzbekStrings = projectFile("app/src/main/res/values-uz/strings.xml").readText()
        val khmerStrings = projectFile("app/src/main/res/values-km/strings.xml").readText()
        val assameseStrings = projectFile("app/src/main/res/values-as/strings.xml").readText()
        val kazakhStrings = projectFile("app/src/main/res/values-kk/strings.xml").readText()
        val northernAzerbaijaniStrings = projectFile("app/src/main/res/values-az/strings.xml").readText()
        val iranianAzerbaijaniStrings = projectFile("app/src/main/res/values-b+az+Arab+IR/strings.xml").readText()
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val productScreenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val settingsSource = projectFile("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt").readText()
        val diagnosticScreenSource = projectFile("app/src/main/java/app/privateaudio/ui/DiagnosticScreen.kt").readText()
        val languagePreferencesSource = projectFile("app/src/main/java/app/privateaudio/localization/AppLanguagePreferences.kt").readText()
        val overlaySource = projectFile("app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val observerSource = projectFile("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt").readText()
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        val localeDirectories = File(projectRoot, "app/src/main/res")
            .listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") && it.name != "values-night" }

        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
    }
}
