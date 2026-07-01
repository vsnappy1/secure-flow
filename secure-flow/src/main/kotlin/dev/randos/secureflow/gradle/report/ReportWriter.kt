package dev.randos.secureflow.gradle.report

import dev.randos.secureflow.gradle.model.Finding
import dev.randos.secureflow.gradle.type.Severity
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ReportWriter {
    fun writeMarkdown(path: Path, findings: List<Finding>) {
        Files.createDirectories(path.parent)
        val report = buildString {
            appendLine("# SecureFlow Report")
            appendLine()
            appendLine("| Severity | Rule | Location | Message | Evidence |")
            appendLine("| --- | --- | --- | --- | --- |")
            if (findings.isEmpty()) {
                appendLine("| Info | None | - | No findings detected. | - |")
            } else {
                findings.forEach { finding ->
                    append("| ")
                    append(finding.severity)
                    append(" | ")
                    append(finding.ruleId.escapeMarkdown())
                    append(" | ")
                    append("${finding.filePath}:${finding.lineNumber}".escapeMarkdown())
                    append(" | ")
                    append(finding.message.escapeMarkdown())
                    append(" | `")
                    append(finding.evidence.escapeMarkdown())
                    appendLine("` |")
                }
            }
        }
        Files.write(path, report.toByteArray(StandardCharsets.UTF_8))
    }

    fun writeJson(path: Path, findings: List<Finding>) {
        Files.createDirectories(path.parent)
        val report = buildString {
            appendLine("{")
            appendLine("  \"summary\": {")
            appendLine("    \"critical\": ${findings.count(Severity.CRITICAL)},")
            appendLine("    \"warning\": ${findings.count(Severity.WARNING)},")
            appendLine("    \"info\": ${findings.count(Severity.INFO)}")
            appendLine("  },")
            appendLine("  \"findings\": [")
            findings.forEachIndexed { index, finding ->
                appendLine("    {")
                appendLine("      \"severity\": \"${finding.severity.name.json()}\",")
                appendLine("      \"ruleId\": \"${finding.ruleId.json()}\",")
                appendLine("      \"message\": \"${finding.message.json()}\",")
                appendLine("      \"filePath\": \"${finding.filePath.json()}\",")
                appendLine("      \"lineNumber\": ${finding.lineNumber},")
                appendLine("      \"evidence\": \"${finding.evidence.json()}\"")
                append("    }")
                if (index < findings.lastIndex) {
                    append(",")
                }
                appendLine()
            }
            appendLine("  ]")
            appendLine("}")
        }
        Files.write(path, report.toByteArray(StandardCharsets.UTF_8))
    }

    private fun List<Finding>.count(severity: Severity): Int = count { finding -> finding.severity == severity }

    private fun String.escapeMarkdown(): String = replace("|", "\\|").replace("\n", " ")

    private fun String.json(): String = buildString {
        this@json.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000C' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (character.code < 0x20) {
                        append("\\u")
                        append(character.code.toString(16).padStart(4, '0'))
                    } else {
                        append(character)
                    }
                }
            }
        }
    }
}
