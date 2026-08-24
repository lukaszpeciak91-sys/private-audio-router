package app.privateaudio

import android.app.LocaleConfig
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import app.privateaudio.localization.AppLanguagePreferences

@RunWith(AndroidJUnit4::class)
class LegacyLocaleResourceResolutionTest {
    @Test
    fun generatedLocaleConfigExactlyMatchesAppOwnedProductLocales() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val packaged = LocaleConfig(context).supportedLocales
            ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
            .orEmpty()
            .map(AppLanguagePreferences::canonicalLanguageTag)
            .toSet()
        val appOwned = BuildConfig.APP_OWNED_PRODUCT_LANGUAGE_TAGS
            .split(',')
            .map(AppLanguagePreferences::canonicalLanguageTag)
            .toSet()

        assertEquals("Dependency locales must not leak and app locales must not disappear", appOwned, packaged)
    }

    @Test
    fun representativePrivacyPoliciesResolveFiveSemanticParagraphs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        listOf("en", "lv", "yo", "ig").forEach { tag ->
            val body = localizedContext(context, tag).getString(R.string.settings_privacy_policy_body)

            assertEquals("$tag resolved paragraph separators", 4, body.windowed(2).count { it == "\n\n" })
            assertEquals("$tag resolved semantic paragraphs", 5, body.split("\n\n").size)
            assertTrue("$tag empty semantic paragraph", body.split("\n\n").all { it.isNotBlank() })
        }
    }

    @Test
    fun generatedLegacyAliasesAreCanonicalizedForApplicationLanguageOptions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val generatedTags = LocaleConfig(context).supportedLocales
            ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
            .orEmpty()
        val optionTags = AppLanguagePreferences.supportedLanguages(context).map { it.languageTag }

        assertTrue("generated LocaleConfig=$generatedTags", generatedTags.any { it == "ji" || it == "yi" })
        assertEquals(1, optionTags.count { it == "yi" })
        assertEquals(1, optionTags.count { it == "he" })
        assertEquals(1, optionTags.count { it == "id" })
        assertTrue(optionTags.none { it == "ji" || it == "iw" || it == "in" })
    }

    @Test
    fun legacyStoredSelectionHasCanonicalLogicalIdentityAndDirection() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val original = localeManager.applicationLocales
        try {
            localeManager.applicationLocales = android.os.LocaleList.forLanguageTags("ji")
            assertEquals("yi", AppLanguagePreferences.currentLanguageTag(context))
            assertEquals(android.view.View.LAYOUT_DIRECTION_RTL, AppLanguagePreferences.presentationLayoutDirection(context))
        } finally {
            localeManager.applicationLocales = original
        }
    }

    @Test
    fun logicalApplicationLocaleIdentitiesProvideMiniDirectionIndependentlyOfResourceAliases() {
        listOf("yi", "he", "ar", "fa", "ur").forEach { tag ->
            assertEquals(
                "$tag Mini direction",
                android.view.View.LAYOUT_DIRECTION_RTL,
                TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(tag)),
            )
        }
        listOf("en", "pl", "id").forEach { tag ->
            assertEquals(
                "$tag Mini direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(tag)),
            )
        }
    }

    @Test
    fun modernLocaleTagsResolveLegacyQualifiedResources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertLocalizedSettings(context, modernTag = "id", expected = "Setelan")
        assertLocalizedSettings(context, modernTag = "he", expected = "הגדרות")
        assertLocalizedSettings(context, modernTag = "yi", expected = "סעטינגס")
        assertLocalizedSettings(context, modernTag = "ml", expected = "ക്രമീകരണം")
        listOf("am", "am-ET").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ቅንብሮች")
        }
        assertLocalizedSettings(context, modernTag = "pa-Guru-IN", expected = "ਸੈਟਿੰਗਾਂ")
        assertLocalizedSettings(context, modernTag = "pa-Arab-PK", expected = "سیٹنگاں")
        listOf("ps", "ps-AF", "ps-PK").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "امستنې")
        }
        listOf("ha", "ha-NG", "ha-NE", "ha-GH").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Saituna")
        }
        listOf("zu", "zu-ZA").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Izilungiselelo")
        }
        listOf("so", "so-SO", "so-DJ", "so-ET", "so-KE").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Dejinta")
        }
        listOf("ne", "ne-NP", "ne-Deva-NP").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "सेटिङहरू")
        }
        listOf("hy", "hy-AM", "hy-Armn-AM").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Կարգավորումներ")
            assertEquals(
                "Private Audio-ի լողացող կառավարիչ։ Կարգավիճակ՝ Պատրաստ է։ Private Audio-ն միացնելու կամ անջատելու, կառավարիչն ընդարձակելու և փակելու կառավարման տարրեր։",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "Պատրաստ է"),
            )
        }
        listOf("jv", "jv-ID", "jv-Latn-ID").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Setelan")
            assertEquals(
                "Kontrol ngambang Private Audio. Status: Siyap. Tombol kanggo nguripake utawa mateni Private Audio, nggedhekake kontrol, lan nutup.",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "Siyap"),
            )
            assertEquals("$tag layout direction", android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, tag).resources.configuration.layoutDirection)
        }
        listOf("hy", "hy-AM", "hy-Armn-AM").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("or", "or-IN", "or-Orya-IN").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ସେଟିଂସ")
        }
        listOf("my", "my-MM", "my-Mymr-MM").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ဆက်တင်များ")
        }
        listOf("uz", "uz-UZ", "uz-Latn-UZ").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Sozlamalar")
        }
        assertLocalizedSettings(context, modernTag = "uz-Cyrl-UZ", expected = "Созламалар")
        assertLocalizedSettings(context, modernTag = "uz-Arab-AF", expected = "تنظیمات")
        listOf("km", "km-KH", "km-Khmr-KH").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ការកំណត់")
        }
        listOf("as", "as-IN", "as-Beng-IN").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ছেটিং")
        }
        listOf("ca", "ca-ES", "ca-Latn-ES").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Configuració")
            assertEquals(
                "Control flotant de Private Audio. Estat: A punt. Botons per activar o desactivar Private Audio, ampliar el control i tancar-lo.",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "A punt"),
            )
        }
        listOf("gl", "gl-ES", "gl-Latn-ES").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Configuración")
            assertEquals(
                "Control flotante de Private Audio. Estado: Preparado. Botóns para activar ou desactivar Private Audio, ampliar o control e pechalo.",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "Preparado"),
            )
            assertEquals("$tag layout direction", android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, tag).resources.configuration.layoutDirection)
        }
        listOf("kk", "kk-KZ", "kk-Cyrl-KZ").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Параметрлер")
        }
        listOf("mn", "mn-MN", "mn-Cyrl-MN").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Тохиргоо")
            assertEquals(
                "Private Audio-н хөвөгч удирдлага. Төлөв: Бэлэн. Асаах/унтраах, дэлгэх, хаах товчлуурууд.",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "Бэлэн"),
            )
        }
        listOf("ka", "ka-GE", "ka-Geor-GE").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "პარამეტრები")
            assertEquals(
                "Private Audio-ს მცურავი მართვის პანელი. სტატუსი: მზადაა. ჩართვის/გამორთვის, გაშლისა და დახურვის მართვის ელემენტები.",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "მზადაა"),
            )
            assertEquals("$tag layout direction", android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, tag).resources.configuration.layoutDirection)
        }
        listOf("lo", "lo-LA", "lo-Laoo-LA").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "ການຕັ້ງຄ່າ")
            assertEquals(
                "ຕົວຄວບຄຸມແບບລອຍຂອງ Private Audio. ສະຖານະ: ພ້ອມ. ປຸ່ມສຳລັບເປີດ/ປິດ Private Audio, ຂະຫຍາຍແຜງຄວບຄຸມ ແລະ ປິດແຜງຄວບຄຸມ.",
                localizedContext(context, tag).getString(R.string.overlay_controller_description, "ພ້ອມ"),
            )
            assertEquals("$tag layout direction", android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, tag).resources.configuration.layoutDirection)
        }
        listOf("az", "az-AZ", "az-Latn-AZ").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "Ayarlar")
            assertEquals("Private Audio üzən idarəedicisi. Vəziyyət: Hazır. Yandırıb-söndürmək, genişləndirmək və bağlamaq üçün idarəetmələr.", localizedContext(context, tag).getString(R.string.overlay_controller_description, "Hazır"))
        }
        listOf("az-IR", "az-Arab", "az-Arab-IR").forEach { tag ->
            assertLocalizedSettings(context, modernTag = tag, expected = "آیارلار")
            assertEquals("Private Audio-نون اۆزن ایداره‌ئدیجی‌سی. دوروم: حاضیر. یاندیرماق/سؤندورمک، گئنیشلندیرمک و باغلاماق اۆچون ایداره‌لر.", localizedContext(context, tag).getString(R.string.overlay_controller_description, "حاضیر"))
        }

        assertEquals(
            android.view.View.LAYOUT_DIRECTION_LTR,
            localizedContext(context, "ml").resources.configuration.layoutDirection,
        )
        listOf("am", "am-ET").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        assertEquals(
            android.view.View.LAYOUT_DIRECTION_LTR,
            localizedContext(context, "pa-Guru-IN").resources.configuration.layoutDirection,
        )
        assertEquals(
            android.view.View.LAYOUT_DIRECTION_RTL,
            localizedContext(context, "pa-Arab-PK").resources.configuration.layoutDirection,
        )
        listOf("ps", "ps-AF", "ps-PK").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_RTL,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("ha", "ha-NG", "ha-NE", "ha-GH").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("zu", "zu-ZA").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("so", "so-SO", "so-DJ", "so-ET", "so-KE").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("ne", "ne-NP", "ne-Deva-NP").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("or", "or-IN", "or-Orya-IN").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("my", "my-MM", "my-Mymr-MM").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("uz", "uz-UZ", "uz-Latn-UZ").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        assertEquals(android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, "uz-Cyrl-UZ").resources.configuration.layoutDirection)
        assertEquals(android.view.View.LAYOUT_DIRECTION_RTL, localizedContext(context, "uz-Arab-AF").resources.configuration.layoutDirection)
        listOf("km", "km-KH", "km-Khmr-KH").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("as", "as-IN", "as-Beng-IN").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("ca", "ca-ES", "ca-Latn-ES").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("kk", "kk-KZ", "kk-Cyrl-KZ").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("mn", "mn-MN", "mn-Cyrl-MN").forEach { tag ->
            assertEquals(
                "$tag layout direction",
                android.view.View.LAYOUT_DIRECTION_LTR,
                localizedContext(context, tag).resources.configuration.layoutDirection,
            )
        }
        listOf("az", "az-AZ", "az-Latn-AZ").forEach { tag ->
            assertEquals("$tag layout direction", android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, tag).resources.configuration.layoutDirection)
        }
        listOf("az-IR", "az-Arab", "az-Arab-IR").forEach { tag ->
            assertEquals("$tag layout direction", android.view.View.LAYOUT_DIRECTION_RTL, localizedContext(context, tag).resources.configuration.layoutDirection)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val discoveredTags = LocaleConfig(context).supportedLocales
                ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
                .orEmpty()
            assertTrue(discoveredTags.containsAll(listOf("id", "he", "yi", "ml", "pa-Guru-IN", "pa-Arab-PK", "ps", "ha", "am", "zu", "so", "ne", "hy", "jv", "or", "my", "uz", "uz-Cyrl-UZ", "uz-Arab-AF", "km", "as", "ca", "gl", "kk", "mn", "ka", "lo", "az", "az-Arab-IR")))
        }
    }

    @Test
    fun genericProductLocalesResolveForCommonRegionalIdentities() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        listOf(
            LocaleResolution("bho", listOf("bho", "bho-IN")),
            LocaleResolution("mai", listOf("mai", "mai-IN")),
            LocaleResolution("ku-Latn", listOf("ku", "ku-TR", "ku-Latn-TR")),
            LocaleResolution("ceb", listOf("ceb", "ceb-PH")),
            LocaleResolution("ln", listOf("ln", "ln-CD", "ln-CG", "ln-AO", "ln-CF")),
        ).forEach { resolution ->
            assertRequestsResolveToProductResource(context, resolution)
        }
    }

    @Test
    fun portugueseRegionsFollowTheirIntendedResourceFamilies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(context, "en-US").getString(R.string.settings)
        val brazilian = localizedContext(context, "pt-BR").getString(R.string.settings)
        val european = localizedContext(context, "pt-PT").getString(R.string.settings)

        assertNotEquals(english, brazilian)
        assertNotEquals(english, european)
        assertNotEquals("Portuguese variants need distinct sentinels for this architecture test", brazilian, european)
        listOf("pt-AO", "pt-MZ").forEach { tag ->
            assertEquals("$tag must use the European Portuguese family", european, localizedContext(context, tag).getString(R.string.settings))
        }
    }

    @Test
    fun unsupportedScriptsDoNotCrossResolveToAProductTreeInAnotherScript() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val english = localizedContext(context, "en-US").getString(R.string.settings)
        listOf(
            "az-Cyrl-AZ",
            "bs-Cyrl-BA",
            "hi-Latn-IN",
        ).forEach { unsupportedTag ->
            assertEquals(
                "$unsupportedTag must not silently use a supported different-script tree",
                english,
                localizedContext(context, unsupportedTag).getString(R.string.settings),
            )
        }
    }

    @Test
    fun cantoneseScriptVariantsResolveIndependentlyAndRemainDistinctInDiscoveryAndPicker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val traditionalTag = "yue-Hant-HK"
        val simplifiedTag = "yue-Hans-CN"
        val traditionalSettings = localizedContext(context, traditionalTag).getString(R.string.settings)
        val simplifiedSettings = localizedContext(context, simplifiedTag).getString(R.string.settings)

        assertEquals("設定", traditionalSettings)
        assertEquals("设置", simplifiedSettings)
        assertNotEquals(traditionalSettings, simplifiedSettings)
        listOf(traditionalTag, simplifiedTag).forEach { tag ->
            assertEquals(android.view.View.LAYOUT_DIRECTION_LTR, localizedContext(context, tag).resources.configuration.layoutDirection)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val discovered = LocaleConfig(context).supportedLocales
                ?.let { locales -> (0 until locales.size()).map { locales[it].toLanguageTag() } }
                .orEmpty()
            assertTrue(discovered.containsAll(listOf(traditionalTag, simplifiedTag)))
            val options = AppLanguagePreferences.supportedLanguages(context).filter { it.languageTag.startsWith("yue-") }
            assertEquals(setOf(traditionalTag, simplifiedTag), options.map { it.languageTag }.toSet())
            assertEquals(2, options.map { it.nativeName }.toSet().size)
        }
    }

    @Test
    fun uzbekScriptVariantsResolveIndependentlyAndRemainDistinctInThePicker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val tags = listOf("uz", "uz-Cyrl-UZ", "uz-Arab-AF")
        val resolvedSettings = tags.map { localizedContext(context, it).getString(R.string.settings) }

        assertEquals(listOf("Sozlamalar", "Созламалар", "تنظیمات"), resolvedSettings)
        assertEquals(3, resolvedSettings.toSet().size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val options = AppLanguagePreferences.supportedLanguages(context).filter { it.languageTag.startsWith("uz") }
            assertEquals(setOf("uz", "uz-Cyrl-UZ", "uz-Arab-AF"), options.map { it.languageTag }.toSet())
            assertEquals(3, options.map { it.nativeName }.toSet().size)
        }
    }

    private fun assertRequestsResolveToProductResource(context: Context, resolution: LocaleResolution) {
        val english = localizedContext(context, "en-US").getString(R.string.settings)
        val product = localizedContext(context, resolution.productTag).getString(R.string.settings)
        assertNotEquals("${resolution.productTag} must resolve outside English", english, product)
        resolution.requestTags.forEach { requestedTag ->
            assertEquals(
                "$requestedTag must resolve to ${resolution.productTag}",
                product,
                localizedContext(context, requestedTag).getString(R.string.settings),
            )
        }
    }

    private data class LocaleResolution(val productTag: String, val requestTags: List<String>)

    private fun assertLocalizedSettings(context: Context, modernTag: String, expected: String) {
        val resolved = localizedContext(context, modernTag).getString(R.string.settings)

        assertEquals(expected, resolved)
        assertNotEquals("Settings", resolved)
    }

    private fun localizedContext(context: Context, modernTag: String): Context {
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(modernTag))
        }
        return context.createConfigurationContext(configuration)
    }
}
