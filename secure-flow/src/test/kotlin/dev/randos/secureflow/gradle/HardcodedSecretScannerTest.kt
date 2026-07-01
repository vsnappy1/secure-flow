package dev.randos.secureflow.gradle

import dev.randos.secureflow.gradle.scanner.HardcodedSecretScanner
import dev.randos.secureflow.gradle.type.Severity
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class HardcodedSecretScannerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun detectsSecretsInSupportedFiles() {
        val root = temporaryFolder.newFolder("project").toPath()
        val source = Files.createDirectories(root.resolve("src/main/java"))
        Files.write(
            source.resolve("NetworkModule.kt"),
            "private const val OPENAI_API_KEY = \"sk-1234567890abcdefghijklmnop\"\n".toByteArray(StandardCharsets.UTF_8)
        )
        Files.write(
            root.resolve("gradle.properties"),
            "firebase_server_key=AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz123456789\n".toByteArray(StandardCharsets.UTF_8)
        )

        val findings = HardcodedSecretScanner().scan(root)

        assertEquals(2, findings.size)
        assertTrue(findings.all { finding -> finding.severity == Severity.CRITICAL })
    }

    @Test
    fun ignoresPlaceholdersAndBuildOutput() {
        val root = temporaryFolder.newFolder("project").toPath()
        Files.write(
            root.resolve("local.properties"),
            "api_key=replace_me\n".toByteArray(StandardCharsets.UTF_8)
        )
        val buildDir = Files.createDirectories(root.resolve("build/generated"))
        Files.write(
            buildDir.resolve("Generated.kt"),
            "val clientSecret = \"abc123456789xyz\"\n".toByteArray(StandardCharsets.UTF_8)
        )

        val findings = HardcodedSecretScanner().scan(root)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun detectsXmlAttributeSecrets() {
        val root = temporaryFolder.newFolder("project").toPath()
        Files.write(
            root.resolve("AndroidManifest.xml"),
            "<meta-data android:name=\"firebase_server_key\" android:value=\"AIzaSyAbCdEfGhIjKlMnOpQrStUvWxYz123456789\" />\n".toByteArray(
                StandardCharsets.UTF_8
            )
        )

        val findings = HardcodedSecretScanner().scan(root)

        assertEquals(1, findings.size)
        assertEquals("AndroidManifest.xml", findings.first().filePath)
    }
}
