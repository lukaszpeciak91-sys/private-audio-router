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
    val completedLocaleDirectories: Set<String> = setOf(
        "values-es",
        "values-fr",
        "values-de",
        "values-ru",
        "values-uk",
        "values-pl",
        "values-it",
        "values-b+zh+Hans",
        "values-b+zh+Hant",
        "values-hi",
        "values-ar",
        "values-fa",
        "values-pt-rPT",
        "values-pt-rBR",
        "values-sw",
        "values-ur",
        "values-ja",
        "values-ko",
        "values-in",
        "values-ta",
        "values-af",
        "values-am",
        "values-as",
        "values-az",
        "values-b+az+Arab+IR",
        "values-b+bho",
        "values-b+ceb",
        "values-b+ku+Latn",
        "values-b+mai",
        "values-b+pa+Arab+PK",
        "values-b+pa+Guru+IN",
        "values-b+sr+Latn",
        "values-b+sr+Latn+ME",
        "values-b+uz+Arab+AF",
        "values-b+uz+Cyrl+UZ",
        "values-b+yue+Hans+CN",
        "values-b+yue+Hant+HK",
        "values-be",
        "values-bg",
        "values-bn",
    )
}
