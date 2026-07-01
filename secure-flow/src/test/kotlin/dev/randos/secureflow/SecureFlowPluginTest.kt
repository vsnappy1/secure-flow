package dev.randos.secureflow

import dev.randos.secureflow.gradle.SecureFlowPlugin
import dev.randos.secureflow.gradle.model.Finding
import dev.randos.secureflow.gradle.task.SecureFlowCheckTask
import dev.randos.secureflow.gradle.type.Severity
import dev.randos.secureflow.gradle.utils.LogLinkFormatter
import dev.randos.secureflow.gradle.utils.SecureFlowExtension
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SecureFlowPluginTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun registersExtensionAndTasksWithDefaultConventions() {
        val project = ProjectBuilder.builder()
            .withProjectDir(temporaryFolder.newFolder("project"))
            .build()

        SecureFlowPlugin().apply(project)

        val extension = project.extensions.getByType(SecureFlowExtension::class.java)
        val checkTask = project.tasks.named("secureFlowCheck", SecureFlowCheckTask::class.java).get()
        val reportTask = project.tasks.named("secureFlowReport").get()

        assertEquals(project.projectDir, extension.scanDirectory.get().asFile)
        assertEquals(project.layout.buildDirectory.dir("reports/secure-flow").get().asFile, extension.reportDirectory.get().asFile)
        assertTrue(extension.failOnFindings.get())
        assertEquals("verification", checkTask.group)
        assertEquals("reporting", reportTask.group)
        assertTrue(reportTask.taskDependencies.getDependencies(reportTask).contains(checkTask))
        assertNotNull(checkTask.description)
    }

    @Test
    fun checkTaskWritesNoFindingsReports() {
        val projectDir = temporaryFolder.newFolder("clean-project")
        Files.write(
            projectDir.toPath().resolve("local.properties"),
            "api_key=replace_me\n".toByteArray(StandardCharsets.UTF_8)
        )
        val reportDir = temporaryFolder.newFolder("clean-reports")
        val task = taskFor(projectDir, reportDir, failOnFindings = true)

        task.run()

        val markdown = reportDir.toPath().resolve("privacy-report.md").readText()
        val json = reportDir.toPath().resolve("privacy-report.json").readText()
        assertTrue(markdown.contains("No findings detected"))
        assertTrue(json.contains("\"critical\": 0"))
    }

    @Test
    fun checkTaskLogsFindingsWithoutFailingWhenConfigured() {
        val projectDir = temporaryFolder.newFolder("project-with-finding")
        Files.write(
            projectDir.toPath().resolve("gradle.properties"),
            "openai_api_key=sk-1234567890abcdefghijklmnop\n".toByteArray(StandardCharsets.UTF_8)
        )
        val reportDir = temporaryFolder.newFolder("finding-reports")
        val task = taskFor(projectDir, reportDir, failOnFindings = false)

        task.run()

        val json = reportDir.toPath().resolve("privacy-report.json").readText()
        assertTrue(json.contains("\"critical\": 1"))
        assertTrue(json.contains("\"filePath\": \"gradle.properties\""))
    }

    @Test(expected = GradleException::class)
    fun checkTaskFailsWhenFindingsAreDetectedAndFailureIsEnabled() {
        val projectDir = temporaryFolder.newFolder("failing-project")
        Files.write(
            projectDir.toPath().resolve("Secrets.kt"),
            "val clientSecret = \"abc123456789xyz\"\n".toByteArray(StandardCharsets.UTF_8)
        )
        val reportDir = temporaryFolder.newFolder("failing-reports")
        val task = taskFor(projectDir, reportDir, failOnFindings = true)

        task.run()
    }

    @Test
    fun findingLogLinkIsClickableFileUriWithLineNumber() {
        val projectDir = temporaryFolder.newFolder("clickable-project").toPath()
        val sourcePath = projectDir.resolve("src/main/java/dev/randos/sample/sample.kt")
        Files.createDirectories(sourcePath.parent)
        Files.write(sourcePath, "val secret = \"abc123456789xyz\"\n".toByteArray(StandardCharsets.UTF_8))
        val finding = Finding(
            severity = Severity.CRITICAL,
            ruleId = "HardcodedSecret",
            message = "Suspicious hardcoded secret detected",
            filePath = "src/main/java/dev/randos/sample/sample.kt",
            lineNumber = 5,
            evidence = "val secret = \"abc1...9xyz\""
        )

        assertEquals("$sourcePath:5", LogLinkFormatter.finding(projectDir, finding))
    }

    private fun taskFor(projectDir: File, reportDir: File, failOnFindings: Boolean): SecureFlowCheckTask {
        val project = ProjectBuilder.builder()
            .withProjectDir(projectDir)
            .build()
        val task = project.tasks.register("secureFlowCheck", SecureFlowCheckTask::class.java).get()
        task.scanDirectory.set(project.layout.projectDirectory)
        task.reportDirectory.set(reportDir)
        task.failOnFindings.set(failOnFindings)
        return task
    }

    private fun Path.readText(): String = String(Files.readAllBytes(this), StandardCharsets.UTF_8)
}
