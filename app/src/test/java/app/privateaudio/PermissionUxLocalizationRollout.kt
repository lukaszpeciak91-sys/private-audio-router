package app.privateaudio

/** Temporary rollout state; remove this object when every locale has the complete bundle. */
object PermissionUxLocalizationRollout {
    val keys = setOf(
        "permission_notification_title",
        "permission_notification_body",
        "permission_notification_allow",
        "permission_notification_continue",
        "permission_overlay_title",
        "permission_overlay_body",
        "permission_overlay_open_settings",
        "permission_overlay_not_now",
    )

    // Add a resource directory only in the batch that adds all eight keys to that directory.
    val completedLocaleDirectories: Set<String> = emptySet()
}
