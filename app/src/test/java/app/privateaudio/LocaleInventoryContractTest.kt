package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class LocaleInventoryContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main/res").isDirectory }
    private val resourceRoot = File(projectRoot, "app/src/main/res")

    @Test
    fun buildInventoryIsExactlyTheAppOwnedResourceInventory() {
        val resourceTags = resourceRoot.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") && it.name != "values-night" }
            .map { logicalTag(it.name.removePrefix("values-")) }
            .toSet() + "en-US"
        val configuredTags = BuildConfig.APP_OWNED_PRODUCT_LANGUAGE_TAGS.split(',').toSet()

        assertEquals("The product inventory must remain English plus 99 localized resource sets", 100, resourceTags.size)
        assertEquals(resourceTags, configuredTags)
    }

    @Test
    fun legacyAliasesHaveOneCompatibleResourceTreeAndOneModernLogicalIdentity() {
        mapOf("in" to "id", "iw" to "he", "ji" to "yi").forEach { (legacy, modern) ->
            assertEquals(modern, logicalTag(legacy))
            assertEquals(1, listOf("values-$legacy", "values-$modern").count { File(resourceRoot, it).isDirectory })
            assertFalse(File(resourceRoot, "values-$modern").exists())
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
