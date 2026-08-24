package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TestSourceContractsTest {
    @Test
    fun executableMemberCallIsDetectedAtItsSourceOffset() {
        val source = "val accepted = audioManager.setCommunicationDevice(earpiece)"

        assertEquals(source.indexOf("audioManager"), callSites(source).single().offset)
    }

    @Test
    fun ordinaryStringMemberCallIsIgnored() {
        assertEquals(emptyList<KotlinCallSite>(), callSites("val text = \"fake.setCommunicationDevice(device)\""))
    }

    @Test
    fun rawStringMemberCallIsIgnoredUntilTheClosingDelimiter() {
        val source = "val text = \"\"\"fake.setCommunicationDevice(device)\"\"\""

        assertEquals(emptyList<KotlinCallSite>(), callSites(source))
    }

    @Test
    fun lineCommentMemberCallIsIgnored() {
        assertEquals(emptyList<KotlinCallSite>(), callSites("// fake.setCommunicationDevice(device)"))
    }

    @Test
    fun blockCommentMemberCallIsIgnored() {
        assertEquals(emptyList<KotlinCallSite>(), callSites("/* fake.setCommunicationDevice(device) */"))
    }

    @Test
    fun escapedQuotesDoNotEndAnOrdinaryString() {
        val source = "val text = \"quoted \\\"fake.setCommunicationDevice(device)\\\" text\""

        assertEquals(emptyList<KotlinCallSite>(), callSites(source))
    }

    @Test
    fun onlyTheExecutableCallIsDetectedAmongAllFakeTextualCalls() {
        val source = listOf(
            "val quoted = \"fake.setCommunicationDevice(device)\"",
            "val raw = \"\"\"fake.setCommunicationDevice(device)\"\"\"",
            "// fake.setCommunicationDevice(device)",
            "/* fake.setCommunicationDevice(device) */",
            "val accepted = audioManager.setCommunicationDevice(earpiece)",
        ).joinToString("\n")

        assertEquals(source.indexOf("audioManager"), callSites(source).single().offset)
    }

    private fun callSites(source: String): List<KotlinCallSite> {
        val sourceFile = File.createTempFile("private-audio-source-contract", ".kt")
        return try {
            sourceFile.writeText(source)
            listOf(sourceFile).kotlinMemberCallSites("setCommunicationDevice")
        } finally {
            sourceFile.delete()
        }
    }
}
