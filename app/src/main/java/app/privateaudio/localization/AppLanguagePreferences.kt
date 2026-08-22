package app.privateaudio.localization

import android.app.LocaleConfig
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import androidx.annotation.ChecksSdkIntAtLeast
import java.util.Locale

data class AppLanguageOption(val languageTag: String, val nativeName: String)

/** Android's generated locale configuration and app-locale override remain authoritative. */
object AppLanguagePreferences {
    private val legacyAndroidLanguageAliases = mapOf(
        "ji" to "yi",
        "iw" to "he",
        "in" to "id",
    )

    @get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    val isSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun currentLanguageTag(context: Context): String? {
        if (!isSupported) return null
        return context.getSystemService(LocaleManager::class.java)
            .applicationLocales
            .takeUnless(LocaleList::isEmpty)
            ?.get(0)
            ?.toLanguageTag()
            ?.let(::canonicalLanguageTag)
    }

    fun supportedLanguages(context: Context): List<AppLanguageOption> {
        if (!isSupported) return emptyList()
        val locales = LocaleConfig(context).supportedLocales ?: return emptyList()
        val localeValues = (0 until locales.size())
            .map(locales::get)
            .map(::canonicalLocale)
            .distinctBy(Locale::toLanguageTag)
        val languageCounts = localeValues.groupingBy(Locale::getLanguage).eachCount()
        return localeValues.map { locale ->
            val rawName = if (languageCounts[locale.language] == 1) {
                locale.getDisplayLanguage(locale)
            } else {
                locale.getDisplayName(locale)
            }
            AppLanguageOption(locale.toLanguageTag(), rawName.localizedTitlecase(locale))
        }
    }

    fun select(context: Context, languageTag: String?) {
        if (!isSupported) return
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            languageTag?.let(::canonicalLanguageTag)?.let(LocaleList::forLanguageTags)
                ?: LocaleList.getEmptyLocaleList()
    }

    fun nativeName(languageTag: String): String {
        val locale = canonicalLocale(Locale.forLanguageTag(languageTag))
        return locale.getDisplayName(locale).localizedTitlecase(locale)
    }

    /** Converts Android/Java legacy language identities to modern logical BCP-47 identities. */
    internal fun canonicalLanguageTag(languageTag: String): String {
        val parts = languageTag.split('-').toMutableList()
        if (parts.isEmpty()) return languageTag
        parts[0] = legacyAndroidLanguageAliases[parts[0].lowercase(Locale.ROOT)] ?: parts[0]
        return Locale.forLanguageTag(parts.joinToString("-")).toLanguageTag()
    }

    fun effectivePresentationLocale(context: Context): Locale {
        val applicationLocale = if (isSupported) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales
                .takeUnless(LocaleList::isEmpty)
                ?.get(0)
        } else {
            null
        }
        return canonicalLocale(applicationLocale ?: context.resources.configuration.locales[0])
    }

    fun presentationLayoutDirection(context: Context): Int =
        TextUtils.getLayoutDirectionFromLocale(effectivePresentationLocale(context))

    private fun canonicalLocale(locale: Locale): Locale =
        Locale.forLanguageTag(canonicalLanguageTag(locale.toLanguageTag()))

    private fun String.localizedTitlecase(locale: Locale): String = replaceFirstChar { first ->
        if (first.isLowerCase()) first.titlecase(locale) else first.toString()
    }
}
