package app.privateaudio

import android.content.Context

/** Owns the durable, install-scoped notification permission explanation decision. */
class PermissionUxPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    var notificationExplanationResolved: Boolean
        get() = preferences.getBoolean(KEY_NOTIFICATION_EXPLANATION_RESOLVED, false)
        set(value) = preferences.edit().putBoolean(KEY_NOTIFICATION_EXPLANATION_RESOLVED, value).apply()

    private companion object {
        const val PREFERENCES_NAME = "permission_ux"
        const val KEY_NOTIFICATION_EXPLANATION_RESOLVED = "notification_explanation_resolved"
    }
}
