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

class UnsafeLoggingScanner {
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

            if (!trimmed.containsLoggingCall() || !trimmed.containsSensitiveValueReference()) {
                return@forEachIndexed
            }

            findings += Finding(
                severity = Severity.WARNING,
                ruleId = RULE_ID,
                message = "Sensitive value may be written to logs",
                filePath = relativePath,
                lineNumber = index + 1,
                evidence = trimmed
            )
        }
    }

    private fun String.shouldSkip(): Boolean = isEmpty() ||
        startsWith("//") ||
        startsWith("/*") ||
        startsWith("*")

    private fun String.containsLoggingCall(): Boolean = LOGGING_CALLS.any { pattern -> pattern.matcher(this).find() }

    private fun String.containsSensitiveValueReference(): Boolean {
        val withoutPlainStrings = removeNonInterpolatedStringContents()
        return SENSITIVE_REFERENCES.any { pattern -> pattern.matcher(withoutPlainStrings).find() }
    }

    private fun String.removeNonInterpolatedStringContents(): String {
        val matcher = STRING_LITERAL.matcher(this)
        val result = StringBuffer()
        while (matcher.find()) {
            val literal = matcher.group()
            val replacement = if (literal.contains('$')) literal else "\"\""
            matcher.appendReplacement(result, MatcherReplacement.quote(replacement))
        }
        matcher.appendTail(result)
        return result.toString()
    }

    private fun Path.isSupported(): Boolean {
        val name = fileName.toString().lowercase(Locale.ROOT)
        return SUPPORTED_EXTENSIONS.any(name::endsWith)
    }

    private object MatcherReplacement {
        fun quote(value: String): String = java.util.regex.Matcher.quoteReplacement(value)
    }

    private companion object {
        const val RULE_ID = "UnsafeLogging"

        val SUPPORTED_EXTENSIONS = setOf(
            ".kt",
            ".kts",
            ".java"
        )

        val IGNORED_DIRECTORIES = setOf(
            ".git",
            ".gradle",
            ".idea",
            "build",
            ".externalNativeBuild",
            ".cxx"
        )

        val LOGGING_CALLS = listOf(
            Pattern.compile("\\bLog\\.(?:v|d|i|w|e|wtf)\\s*\\("),
            Pattern.compile("\\bTimber\\.(?:v|d|i|w|e|wtf)\\s*\\("),
            Pattern.compile("\\bprintln\\s*\\("),
            Pattern.compile("\\bprint\\s*\\("),
            Pattern.compile("\\bSystem\\.(?:out|err)\\.(?:print|println|printf)\\s*\\(")
        )

        val SENSITIVE_REFERENCES = listOf(
            Pattern.compile("(?i)\\b(?:access|auth|bearer|id|refresh)[_-]?token\\b"),
            Pattern.compile("(?i)\\b(?:api|secret|private)[_-]?key\\b"),
            Pattern.compile("(?i)\\b(?:client|app)?[_-]?secret\\b"),
            Pattern.compile("(?i)\\b(?:password|passwd|pwd|passcode|pin)\\b"),
            Pattern.compile("(?i)\\b(?:authorization|authHeader|cookie|session|jwt)\\b"),
            Pattern.compile("(?i)\\b(?:email|e[-_]?mail|phone|address|ssn|socialSecurity|dob|dateOfBirth)\\b"),
            Pattern.compile("(?i)\\b(?:latitude|longitude|location|gps|geoPoint)\\b"),
            Pattern.compile("(?i)\\b(?:prompt|userInput|rawInput|rawRequest|rawResponse)\\b")
        )

        val STRING_LITERAL: Pattern = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'")
    }
}
