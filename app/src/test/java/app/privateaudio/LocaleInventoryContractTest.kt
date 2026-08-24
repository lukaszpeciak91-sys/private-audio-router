package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocaleInventoryContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main/res").isDirectory }
    private val resourceRoot = File(projectRoot, "app/src/main/res")
    private val localeResourceConfigurationPattern = Regex(
        "^(?:[a-z]{2,3}(?:-r(?:[A-Z]{2}|[0-9]{3}))?|b\\+[A-Za-z]{2,8}(?:\\+[A-Za-z0-9]{2,8})+)$",
    )

    @Test
    fun buildInventoryIsExactlyTheAppOwnedResourceInventory() {
        val localeConfigurations = resourceRoot.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .map { it.name.removePrefix("values-") }
            .filter(localeResourceConfigurationPattern::matches)
        val resourceTags = localeConfigurations.map(::logicalTag).toSet() + "en-US"
        val configuredTags = BuildConfig.APP_OWNED_PRODUCT_LANGUAGE_TAGS.split(',').toSet()

        assertTrue("The English default must remain part of the product locale inventory", "en-US" in configuredTags)
        assertEquals(resourceTags, configuredTags)
    }

    @Test
    fun nonLocaleValuesQualifiersAreNotProductLocales() {
        val nonLocaleConfigurations = resourceRoot.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .map { it.name.removePrefix("values-") }
            .filterNot(localeResourceConfigurationPattern::matches)
            .toSet()
        val configuredTags = BuildConfig.APP_OWNED_PRODUCT_LANGUAGE_TAGS.split(',').toSet()

        assertTrue("values-night must remain a non-locale resource qualifier", "night" in nonLocaleConfigurations)
        nonLocaleConfigurations.forEach { configuration ->
            assertFalse("values-$configuration must not become a product locale", configuration in configuredTags)
        }
    }

    @Test
    fun legacyAliasesHaveOneCompatibleResourceTreeAndOneModernLogicalIdentity() {
        mapOf("in" to "id", "iw" to "he", "ji" to "yi").forEach { (legacy, modern) ->
            assertEquals(modern, logicalTag(legacy))
            val equivalentTrees = listOf(
                "values-$legacy",
                "values-$modern",
                "values-b+$legacy",
                "values-b+$modern",
            )
            assertEquals(1, equivalentTrees.count { File(resourceRoot, it).isDirectory })
            assertFalse(File(resourceRoot, "values-$modern").exists())
            assertFalse(File(resourceRoot, "values-b+$modern").exists())
        }
    }

    private fun logicalTag(configuration: String): String {
        val tag = if (configuration.startsWith("b+")) {
            configuration.removePrefix("b+").replace('+', '-')
        } else {
            configuration.replace(Regex("-r([A-Z]{2}|[0-9]{3})$"), "-$1")
        }
        val parts = tag.split('-').toMutableList()
        parts[0] = mapOf("in" to "id", "iw" to "he", "ji" to "yi")[parts[0]] ?: parts[0]
        return parts.joinToString("-")
    }
}
