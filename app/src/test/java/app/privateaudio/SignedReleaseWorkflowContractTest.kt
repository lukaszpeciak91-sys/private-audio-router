package app.privateaudio

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignedReleaseWorkflowContractTest {
    @Test
    fun releaseWorkflowIsManualMainOnlyAndReadOnly() {
        assertTrue(workflow.contains("on:\n  workflow_dispatch:"))
        assertFalse(workflow.contains("pull_request:"))
        assertFalse(workflow.contains("push:"))
        assertFalse(workflow.contains("schedule:"))
        assertTrue(workflow.contains("GITHUB_REF}") && workflow.contains("refs/heads/main"))
        assertTrue(workflow.contains("contents: read"))
        assertFalse(workflow.contains("contents: write"))
    }

    @Test
    fun signingInputsStaySecretAndTheTemporaryKeystoreIsCleaned() {
        expectedSecrets.forEach { secret ->
            assertTrue("Missing secret reference: $secret", workflow.contains("secrets.$secret"))
        }
        assertTrue(workflow.contains("RUNNER_TEMP}/puzru-upload.keystore"))
        assertTrue(workflow.contains("chmod 600"))
        assertTrue(workflow.contains("if: always()"))
        assertTrue(workflow.contains("rm -f \"\${RUNNER_TEMP}/puzru-upload.keystore\""))
        assertFalse(gradle.contains("puzru-upload"))
        assertFalse(gradle.contains("PUZRU_UPLOAD_KEYSTORE_BASE64"))
        assertFalse(workflow.contains("set -x"))
    }

    @Test
    fun releaseBundleIsStrictlyVerifiedAndOnlyExpectedFilesAreUploaded() {
        val verification = workflow.substringAfter("- name: Verify signature, certificate, and bundle checksum")
            .substringBefore("- name: Upload signed Puzru AAB")
        assertTrue(verification.contains("jarsigner -verify -strict -verbose -certs"))
        assertTrue(verification.contains("-keystore \"\${PUZRU_UPLOAD_KEYSTORE_PATH}\""))
        assertTrue(verification.contains("-storetype JKS"))
        assertFalse(verification.contains("-storepass"))
        assertFalse(verification.contains("PUZRU_UPLOAD_STORE_PASSWORD"))
        assertFalse(verification.contains("PUZRU_UPLOAD_KEY_PASSWORD"))
        assertTrue(workflow.contains("keytool -printcert -jarfile"))
        assertTrue(workflow.contains(expectedFingerprint))
        assertTrue(workflow.contains("sha256sum"))
        assertTrue(workflow.contains("retention-days: 7"))

        val upload = workflow.substringAfter("- name: Upload signed Puzru AAB")
            .substringBefore("- name: Remove temporary keystore")
        assertTrue(upload.contains("app-release.aab\n"))
        assertTrue(upload.contains("app-release.aab.sha256"))
        assertFalse(upload.contains(".apk"))
        assertEquals(2, upload.lineSequence().count { it.trim().startsWith("app/build/outputs/") })
    }

    @Test
    fun signingIsOptionalForOrdinaryCiAndReleaseIdentityIsStable() {
        assertTrue(gradle.contains("values.all { !it.isNullOrBlank() }"))
        assertTrue(gradle.contains("if (hasCompleteReleaseSigningEnvironment)"))
        assertTrue(androidCi.contains("bundleRelease"))
        expectedSecrets.forEach { secret -> assertFalse(androidCi.contains(secret)) }
        assertTrue(gradle.contains("versionCode = 1"))
        assertTrue(gradle.contains("versionName = \"0.1.0\""))
    }

    @Test
    fun releaseOptimizationAndCrashMetadataVerificationStayEnabled() {
        assertTrue(gradle.contains("optimization {\n                enable = true\n            }"))
        assertFalse(gradle.contains("isMinifyEnabled = false"))
        assertTrue(gradle.contains("ndk.debugSymbolLevel = \"SYMBOL_TABLE\""))

        val metadataVerification = workflow.substringAfter("- name: Verify release crash-diagnostics metadata")
            .substringBefore("- name: Verify signature, certificate, and bundle checksum")
        assertTrue(metadataVerification.contains("app/build/outputs/mapping/release/mapping.txt"))
        assertTrue(metadataVerification.contains("[[ ! -s \"\${mapping}\" ]]"))
        assertTrue(
            metadataVerification.contains(
                "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map",
            ),
        )
        assertTrue(metadataVerification.contains("libandroidx.graphics.path.so"))
        assertTrue(metadataVerification.contains("com.android.tools.build.debugsymbols/"))
        assertTrue(metadataVerification.contains("::warning::No native debug-symbol metadata is embedded"))
    }

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val workflow = source(".github/workflows/signed-release-aab.yml")
        val androidCi = source(".github/workflows/android-ci.yml")
        val gradle = source("app/build.gradle.kts")
        val expectedSecrets = listOf(
            "PUZRU_UPLOAD_KEYSTORE_BASE64",
            "PUZRU_UPLOAD_KEY_ALIAS",
            "PUZRU_UPLOAD_STORE_PASSWORD",
            "PUZRU_UPLOAD_KEY_PASSWORD",
        )
        const val expectedFingerprint =
            "0F:45:43:E4:1F:FF:5F:2D:51:12:A2:05:23:08:64:64:99:ED:E4:22:87:A5:D8:E1:A6:4E:4D:CD:B4:38:54:4F"
    }
}
