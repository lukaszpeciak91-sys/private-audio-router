package app.privateaudio.overlay

import android.content.Context
import android.text.TextUtils
import app.privateaudio.localization.AppLanguagePreferences
import java.util.Locale

internal fun miniLayoutDirection(context: Context): Int {
    val applicationLocale = AppLanguagePreferences.currentLanguageTag(context)
        ?.let(Locale::forLanguageTag)
    val effectiveLocale = applicationLocale ?: context.resources.configuration.locales[0]
    return TextUtils.getLayoutDirectionFromLocale(effectiveLocale)
}
