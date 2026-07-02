package dev.randos.secureflow.gradle.scanner

import dev.randos.secureflow.gradle.model.Finding
import dev.randos.secureflow.gradle.type.Severity
import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.Locale
import java.util.regex.Pattern
import kotlin.collections.plusAssign

class CleartextTrafficScanner {
    fun scan(root: Path): List<Finding> {
        val findings = mutableListOf<Finding>()
        if (!Files.exists(root)) {
            return findings
        }

        Files.walkFileTree(
            root,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (dir != root && dir.fileName.toString() in IGNORED_DIRECTORIES) {
                        return FileVisitResult.SKIP_SUBTREE
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.isSupported()) {
                        scanFile(root, file, findings)
                    }
                    return FileVisitResult.CONTINUE
                }
            }
        )

        return findings
    }

    private fun scanFile(root: Path, file: Path, findings: MutableList<Finding>) {
        val relativePath = root.relativize(file).toString()
        Files.readAllLines(file, StandardCharsets.UTF_8).forEachIndexed { index, line ->
            val trimmed = line.trim()
            if (trimmed.shouldSkip()) {
                return@forEachIndexed
            }

            val message = detectCleartextTraffic(trimmed) ?: return@forEachIndexed
            findings += Finding(
                severity = Severity.CRITICAL,
                ruleId = RULE_ID,
                message = message,
                filePath = relativePath,
                lineNumber = index + 1,
                evidence = trimmed
            )
        }
    }

    private fun detectCleartextTraffic(line: String): String? = when {
        USES_CLEARTEXT_TRAFFIC.matcher(line).find() -> "Cleartext traffic is enabled in Android manifest"
        CLEARTEXT_TRAFFIC_PERMITTED.matcher(line).find() -> "Cleartext traffic is permitted by network security config"
        else -> null
    }

    private fun String.shouldSkip(): Boolean = isEmpty() ||
        startsWith("<!--") ||
        startsWith("//") ||
        startsWith("/*") ||
        startsWith("*")

    private fun Path.isSupported(): Boolean {
        val name = fileName.toString().lowercase(Locale.ROOT)
        return name.endsWith(".xml")
    }

    private companion object {
        const val RULE_ID = "CleartextTraffic"

        val IGNORED_DIRECTORIES = setOf(
            ".git",
            ".gradle",
            ".idea",
            "build",
            ".externalNativeBuild",
            ".cxx"
        )

        val USES_CLEARTEXT_TRAFFIC: Pattern = Pattern.compile(
            "\\bandroid:usesCleartextTraffic\\s*=\\s*[\"']true[\"']",
            Pattern.CASE_INSENSITIVE
        )

        val CLEARTEXT_TRAFFIC_PERMITTED: Pattern = Pattern.compile(
            "\\bcleartextTrafficPermitted\\s*=\\s*[\"']true[\"']",
            Pattern.CASE_INSENSITIVE
        )
    }
}
