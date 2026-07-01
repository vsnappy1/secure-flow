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

class HardcodedSecretScanner {
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
            if (
                trimmed.isEmpty() ||
                trimmed.startsWith("//") ||
                trimmed.startsWith("/*") ||
                trimmed.startsWith("*")
            ) {
                return@forEachIndexed
            }

            val detected = detectSecret(trimmed) ?: return@forEachIndexed
            findings += Finding(
                severity = Severity.CRITICAL,
                ruleId = RULE_ID,
                message = "Suspicious hardcoded secret detected",
                filePath = relativePath,
                lineNumber = index + 1,
                evidence = trimmed.redact(detected)
            )
        }
    }

    private fun detectSecret(line: String): String? {
        TOKEN_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(line)
            if (matcher.find()) {
                return matcher.group()
            }
        }

        val normalizedLine = line.lowercase(Locale.ROOT).replace("-", "_")
        val containsSensitiveName = SENSITIVE_NAMES
            .map { name -> name.replace("-", "_") }
            .any(normalizedLine::contains)
        if (!containsSensitiveName) {
            return null
        }

        val assignedValue = ASSIGNED_VALUE.matcher(line)
        while (assignedValue.find()) {
            val value = assignedValue.group(1)
            if (value.looksLikeSecretValue()) {
                return value
            }
        }

        val xmlValue = XML_STRING_VALUE.matcher(line)
        if (xmlValue.find()) {
            val value = xmlValue.group(1)
            if (value.looksLikeSecretValue()) {
                return value
            }
        }

        return null
    }

    private fun String.looksLikeSecretValue(): Boolean {
        val lower = lowercase(Locale.ROOT)
        if (
            lower.contains("todo") ||
            lower.contains("sample") ||
            lower.contains("example") ||
            lower.contains("placeholder") ||
            lower.contains("your_") ||
            lower.contains("replace_me") ||
            lower.startsWith("\${") ||
            lower.startsWith("@string/")
        ) {
            return false
        }

        var letters = 0
        var digits = 0
        var symbols = 0
        forEach { character ->
            when {
                character.isLetter() -> letters++
                character.isDigit() -> digits++
                else -> symbols++
            }
        }

        return length >= 12 && letters > 0 && (digits > 0 || symbols > 0)
    }

    private fun String.redact(secret: String): String {
        if (secret.length <= 8) {
            return replace(secret, "********")
        }

        val visiblePrefix = secret.take(4)
        val visibleSuffix = secret.takeLast(4)
        return replace(secret, "$visiblePrefix...$visibleSuffix")
    }

    private fun Path.isSupported(): Boolean {
        val name = fileName.toString().lowercase(Locale.ROOT)
        return SUPPORTED_EXTENSIONS.any(name::endsWith)
    }

    private companion object {
        const val RULE_ID = "HardcodedSecret"

        val SUPPORTED_EXTENSIONS = setOf(
            ".kt",
            ".kts",
            ".java",
            ".xml",
            ".gradle",
            ".properties"
        )

        val IGNORED_DIRECTORIES = setOf(
            ".git",
            ".gradle",
            ".idea",
            "build",
            ".externalNativeBuild",
            ".cxx"
        )

        val SENSITIVE_NAMES = listOf(
            "api_key",
            "apikey",
            "api-key",
            "access_token",
            "accesstoken",
            "access-token",
            "auth_token",
            "authtoken",
            "bearer",
            "client_secret",
            "clientsecret",
            "client-secret",
            "secret_key",
            "secretkey",
            "secret-key",
            "firebase_server_key",
            "firebase",
            "openai_api_key",
            "anthropic_api_key",
            "gemini_api_key",
            "ai_api_key",
            "private_key"
        )

        val TOKEN_PATTERNS = listOf(
            Pattern.compile("\\bBearer\\s+[A-Za-z0-9._~+/=-]{16,}\\b"),
            Pattern.compile("\\bAIza[0-9A-Za-z_-]{35}\\b"),
            Pattern.compile("\\bsk-[A-Za-z0-9_-]{20,}\\b"),
            Pattern.compile("\\bghp_[A-Za-z0-9_]{30,}\\b"),
            Pattern.compile("\\bgithub_pat_[A-Za-z0-9_]{30,}\\b"),
            Pattern.compile("\\bxox[baprs]-[A-Za-z0-9-]{20,}\\b"),
            Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b"),
            Pattern.compile("\\bSG\\.[A-Za-z0-9_-]{16,}\\.[A-Za-z0-9_-]{16,}\\b")
        )

        val ASSIGNED_VALUE: Pattern = Pattern.compile(
            "(?i)(?:=|:|:=|=>|value\\s*=)\\s*[\"']?([^\"'\\s,;<>)]{8,})[\"']?"
        )

        val XML_STRING_VALUE: Pattern = Pattern.compile(
            ">\\s*([^<\\s]{8,})\\s*<"
        )
    }
}
