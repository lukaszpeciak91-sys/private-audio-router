package app.privateaudio

import android.content.Context

/** Owns only the two durable, install-scoped permission explanation decisions. */
class PermissionUxPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var notificationExplanationResolved: Boolean
        get() = preferences.getBoolean(KEY_NOTIFICATION_EXPLANATION_RESOLVED, false)
        set(value) = preferences.edit().putBoolean(KEY_NOTIFICATION_EXPLANATION_RESOLVED, value).apply()

    var overlayExplanationResolved: Boolean
        get() = preferences.getBoolean(KEY_OVERLAY_EXPLANATION_RESOLVED, false)
        set(value) = preferences.edit().putBoolean(KEY_OVERLAY_EXPLANATION_RESOLVED, value).apply()

    private companion object {
        const val PREFERENCES_NAME = "permission_ux"
        const val KEY_NOTIFICATION_EXPLANATION_RESOLVED = "notification_explanation_resolved"
        const val KEY_OVERLAY_EXPLANATION_RESOLVED = "overlay_explanation_resolved"
    }
}
