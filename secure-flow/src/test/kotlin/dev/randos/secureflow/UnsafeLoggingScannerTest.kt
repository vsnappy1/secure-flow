package dev.randos.secureflow

import dev.randos.secureflow.gradle.scanner.UnsafeLoggingScanner
import dev.randos.secureflow.gradle.type.Severity
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UnsafeLoggingScannerTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun detectsSensitiveAndroidLogging() {
        val root = temporaryFolder.newFolder("project").toPath()
        val source = Files.createDirectories(root.resolve("src/main/java"))
        Files.write(
            source.resolve("ProfileScreen.kt"),
            """
            Log.d("User", user.email)
            Log.e("AI_REQUEST", prompt)
            println(accessToken)
            """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        )

        val findings = UnsafeLoggingScanner().scan(root)

        assertEquals(3, findings.size)
        assertTrue(findings.all { finding -> finding.severity == Severity.WARNING })
        assertTrue(findings.all { finding -> finding.ruleId == "UnsafeLogging" })
    }

    @Test
    fun detectsSensitiveJavaSystemLogging() {
        val root = temporaryFolder.newFolder("project").toPath()
        Files.write(
            root.resolve("SessionLogger.java"),
            """
            class SessionLogger {
                void log(String refreshToken) {
                    System.out.println(refreshToken);
                }
            }
            """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        )

        val findings = UnsafeLoggingScanner().scan(root)

        assertEquals(1, findings.size)
        assertEquals("SessionLogger.java", findings.first().filePath)
    }

    @Test
    fun ignoresNonSensitiveLogsCommentsAndBuildOutput() {
        val root = temporaryFolder.newFolder("project").toPath()
        Files.write(
            root.resolve("MainActivity.kt"),
            """
            Log.d("Lifecycle", "screen opened")
            // Log.d("User", user.email)
            println("debug mode")
            """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        )
        val buildDir = Files.createDirectories(root.resolve("build/generated"))
        Files.write(
            buildDir.resolve("Generated.kt"),
            "Log.d(\"User\", user.email)\n".toByteArray(StandardCharsets.UTF_8)
        )

        val findings = UnsafeLoggingScanner().scan(root)

        assertTrue(findings.isEmpty())
    }

    @Test
    fun detectsKotlinStringInterpolation() {
        val root = temporaryFolder.newFolder("project").toPath()
        Files.write(
            root.resolve("AuthViewModel.kt"),
            "Log.d(\"Auth\", \"token=\$accessToken\")\n".toByteArray(StandardCharsets.UTF_8)
        )

        val findings = UnsafeLoggingScanner().scan(root)

        assertEquals(1, findings.size)
    }
}
