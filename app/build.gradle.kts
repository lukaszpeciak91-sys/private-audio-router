plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val localeResourceConfigurationPattern = Regex(
    "^(?:[a-z]{2,3}(?:-r(?:[A-Z]{2}|[0-9]{3}))?|b\\+[A-Za-z]{2,8}(?:\\+[A-Za-z0-9]{2,8})+)$",
)

val appOwnedLocalizedResourceConfigurations = file("src/main/res").listFiles().orEmpty()
    .asSequence()
    .filter { it.isDirectory && it.name.startsWith("values-") }
    .map { it.name.removePrefix("values-") }
    .filter(localeResourceConfigurationPattern::matches)
    .sorted()
    .toList()
val appOwnedDefaultLanguageTag = file("src/main/res/resources.properties").readLines()
    .single { it.startsWith("unqualifiedResLocale=") }
    .substringAfter('=')

fun String.toAndroidResourceConfiguration(): String {
    val parts = split('-')
    return if (parts.size == 2 && (parts[1].length == 2 || parts[1].length == 3)) {
        "${parts[0]}-r${parts[1]}"
    } else {
        "b+${parts.joinToString("+")}"
    }
}

val appOwnedLocaleResourceConfigurations =
    (listOf(appOwnedDefaultLanguageTag.toAndroidResourceConfiguration()) + appOwnedLocalizedResourceConfigurations).sorted()

fun String.toLogicalLanguageTag(): String {
    val resourceTag = if (startsWith("b+")) {
        removePrefix("b+").replace('+', '-')
    } else {
        replace(Regex("-r([A-Z]{2}|[0-9]{3})$"), "-$1")
    }
    val parts = resourceTag.split('-').toMutableList()
    parts[0] = mapOf("in" to "id", "iw" to "he", "ji" to "yi")[parts[0]] ?: parts[0]
    return parts.joinToString("-")
}

val appOwnedProductLanguageTags =
    (listOf(appOwnedDefaultLanguageTag) + appOwnedLocalizedResourceConfigurations.map(String::toLogicalLanguageTag)).sorted()

android {
    namespace = "app.privateaudio"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.privateaudio"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "APP_OWNED_PRODUCT_LANGUAGE_TAGS",
            "\"${appOwnedProductLanguageTags.joinToString(",")}\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
        // Dependencies can contribute translations to generated LocaleConfig. Restrict both
        // packaged resources and generated discovery to locale trees owned by this app.
        localeFilters += appOwnedLocaleResourceConfigurations
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2026.06.00")

    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:runner:1.7.0")
}
