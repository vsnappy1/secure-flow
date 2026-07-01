package dev.randos.secureflow.gradle.task

import dev.randos.secureflow.gradle.report.ReportWriter
import dev.randos.secureflow.gradle.scanner.HardcodedSecretScanner
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class SecureFlowCheckTask : DefaultTask() {
    @get:InputDirectory
    abstract val scanDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val reportDirectory: DirectoryProperty

    @get:Input
    abstract val failOnFindings: Property<Boolean>

    @TaskAction
    fun run() {
        val scanPath = scanDirectory.get().asFile.toPath()
        val reportPath = reportDirectory.get().asFile.toPath()
        val findings = HardcodedSecretScanner().scan(scanPath)

        ReportWriter().apply {
            writeMarkdown(reportPath.resolve("privacy-report.md"), findings)
            writeJson(reportPath.resolve("privacy-report.json"), findings)
        }

        if (findings.isEmpty()) {
            logger.lifecycle("SecureFlow found no issues. Reports written to {}", reportPath)
            return
        }

        logger.lifecycle("SecureFlow found {} issue(s). Reports written to {}", findings.size, reportPath)
        findings.forEach { finding ->
            logger.lifecycle(
                "[{}] {} at {}:{}",
                finding.severity,
                finding.message,
                finding.filePath,
                finding.lineNumber,
            )
        }

        if (failOnFindings.get()) {
            throw GradleException("SecureFlow found ${findings.size} issue(s). See $reportPath")
        }
    }
}