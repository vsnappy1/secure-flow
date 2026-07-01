package dev.randos.secureflow.gradle.model

import dev.randos.secureflow.gradle.type.Severity

data class Finding(
    val severity: Severity,
    val ruleId: String,
    val message: String,
    val filePath: String,
    val lineNumber: Int,
    val evidence: String,
)