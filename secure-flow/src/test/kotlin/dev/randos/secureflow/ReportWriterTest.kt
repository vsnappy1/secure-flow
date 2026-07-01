package dev.randos.secureflow

import dev.randos.secureflow.gradle.model.Finding
import dev.randos.secureflow.gradle.report.ReportWriter
import dev.randos.secureflow.gradle.type.Severity
import dev.randos.secureflow.utils.ResourceReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ReportWriterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun writesEmptyMarkdownReport() {
        val reportPath = temporaryFolder.root.toPath().resolve("reports/privacy-report.md")

        ReportWriter().writeMarkdown(reportPath, emptyList())

        val report = reportPath.readText()
        val expected = ResourceReader.readFile("EmptyMarkdownReport.md")
        assertEquals(expected, report)
    }

    @Test
    fun writesMarkdownReportWithEscapedFindingValues() {
        val reportPath = temporaryFolder.root.toPath().resolve("reports/privacy-report.md")
        val findings = listOf(
            Finding(
                severity = Severity.CRITICAL,
                ruleId = "Rule|One",
                message = "Message with\nnewline",
                filePath = "src/main/App.kt",
                lineNumber = 12,
                evidence = "token|secret"
            )
        )

        ReportWriter().writeMarkdown(reportPath, findings)

        val report = reportPath.readText()
        val expected = ResourceReader.readFile("MarkdownReport.md")
        assertEquals(expected, report)
    }

    @Test
    fun writesJsonReportWithSummaryAndEscapedValues() {
        val reportPath = temporaryFolder.root.toPath().resolve("reports/privacy-report.json")
        val findings = listOf(
            Finding(
                severity = Severity.CRITICAL,
                ruleId = "HardcodedSecret",
                message = "Quote \" slash \\ newline\n tab\t backspace\b formfeed\u000C carriage\r",
                filePath = "src/main/App.kt",
                lineNumber = 7,
                evidence = "apikey=abcd1234"
            ),
            Finding(
                severity = Severity.WARNING,
                ruleId = "WarningRule",
                message = "warning",
                filePath = "gradle.properties",
                lineNumber = 2,
                evidence = "warn"
            ),
            Finding(
                severity = Severity.INFO,
                ruleId = "InfoRule",
                message = "info",
                filePath = "README.md",
                lineNumber = 1,
                evidence = "info"
            )
        )

        ReportWriter().writeJson(reportPath, findings)

        val report = reportPath.readText()
        val expected = ResourceReader.readFile("JsonReport.json")
        assertEquals(expected, report)
    }

    private fun Path.readText(): String = String(Files.readAllBytes(this), StandardCharsets.UTF_8)
}
