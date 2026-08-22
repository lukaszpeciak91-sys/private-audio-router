package app.privateaudio.overlay

import android.content.Context
import app.privateaudio.localization.AppLanguagePreferences

internal fun miniLayoutDirection(context: Context): Int {
    return AppLanguagePreferences.presentationLayoutDirection(context)
}

internal fun miniDirectionalX(ltrX: Float, width: Float, rtl: Boolean): Float =
    if (rtl) width - ltrX else ltrX

internal fun miniControlAt(x: Float, width: Float, rtl: Boolean): MiniControl {
    val directionalTouchX = miniDirectionalX(x, width, rtl)
    return when {
        directionalTouchX >= width * 0.80f -> MiniControl.CLOSE
        directionalTouchX >= width * 0.60f -> MiniControl.EXPAND
        directionalTouchX >= width * 0.40f && directionalTouchX < width * 0.60f -> MiniControl.POWER
        else -> MiniControl.NONE
    }
}
