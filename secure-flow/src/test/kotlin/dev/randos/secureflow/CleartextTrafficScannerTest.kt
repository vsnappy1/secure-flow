package dev.randos.secureflow

import dev.randos.secureflow.gradle.scanner.CleartextTrafficScanner
import dev.randos.secureflow.gradle.type.Severity
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CleartextTrafficScannerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun detectsManifestCleartextTrafficEnabled() {
        val root = temporaryFolder.newFolder("project").toPath()
        val manifest = Files.createDirectories(root.resolve("src/main"))
        Files.write(
            manifest.resolve("AndroidManifest.xml"),
            """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application
                    android:name=".SecureFlowApp"
                    android:usesCleartextTraffic="true" />
            </manifest>
            """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        )

        val findings = CleartextTrafficScanner().scan(root)

        assertEquals(1, findings.size)
        assertEquals(Severity.CRITICAL, findings.first().severity)
        assertEquals("CleartextTraffic", findings.first().ruleId)
        assertEquals("src/main/AndroidManifest.xml", findings.first().filePath)
    }

    @Test
    fun detectsNetworkSecurityConfigCleartextPermitted() {
        val root = temporaryFolder.newFolder("project").toPath()
        val config = Files.createDirectories(root.resolve("src/main/res/xml"))
        Files.write(
            config.resolve("network_security_config.xml"),
            """
            <network-security-config>
                <domain-config cleartextTrafficPermitted = 'true'>
                    <domain includeSubdomains="true">example.com</domain>
                </domain-config>
            </network-security-config>
            """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        )

        val findings = CleartextTrafficScanner().scan(root)

        assertEquals(1, findings.size)
        assertEquals("Cleartext traffic is permitted by network security config", findings.first().message)
    }

    @Test
    fun ignoresFalseValuesCommentsAndBuildOutput() {
        val root = temporaryFolder.newFolder("project").toPath()
        Files.write(
            root.resolve("AndroidManifest.xml"),
            """
            <application android:usesCleartextTraffic="false" />
            <!-- android:usesCleartextTraffic="true" -->
            <domain-config cleartextTrafficPermitted="false" />
            """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        )
        val buildDir = Files.createDirectories(root.resolve("build/intermediates/merged_manifest"))
        Files.write(
            buildDir.resolve("AndroidManifest.xml"),
            "<application android:usesCleartextTraffic=\"true\" />\n".toByteArray(StandardCharsets.UTF_8)
        )

        val findings = CleartextTrafficScanner().scan(root)

        assertTrue(findings.isEmpty())
    }
}
