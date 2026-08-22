package app.privateaudio

import android.app.LocaleConfig
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

@RunWith(AndroidJUnit4::class)
class LegacyLocaleResourceResolutionTest {
    @Test
    fun logicalApplicationLocaleIdentitiesProvideMiniDirectionIndependentlyOfResourceAliases() {
        listOf("yi", "he", "ar", "fa", "ur").forEach { tag ->
            assertEquals(
                "$tag Mini direction",
                android.view.View.LAYOUT_DIRECTION_RTL,
                TextUtils.getLayoutDirectionFromLocale(Locale.forLanguageTag(tag)),
            )
        }
        listOf("en", "pl").forEach { tag ->
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
            assertTrue(discoveredTags.containsAll(listOf("id", "he", "yi", "ml", "pa-Guru-IN", "pa-Arab-PK", "ps", "ha", "am", "zu", "so", "ne", "hy", "jv", "or", "my", "uz", "km", "as", "ca", "gl", "kk", "mn", "ka", "lo", "az", "az-Arab-IR")))
        }
    }

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
