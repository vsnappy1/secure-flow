package dev.randos.secureflow.gradle.utils

import dev.randos.secureflow.gradle.model.Finding
import java.nio.file.Path

internal object LogLinkFormatter {
    fun reportDirectory(path: Path): String = path.clickableUri()

    fun finding(scanPath: Path, finding: Finding): String =
        "${scanPath.resolve(finding.filePath).clickableUri()}:${finding.lineNumber}"

    private fun Path.clickableUri(): String = toAbsolutePath().normalize().toUri().toString()
}
