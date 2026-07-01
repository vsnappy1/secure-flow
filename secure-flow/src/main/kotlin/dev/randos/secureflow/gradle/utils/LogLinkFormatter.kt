package dev.randos.secureflow.gradle.utils

import dev.randos.secureflow.gradle.model.Finding
import java.nio.file.Path

internal object LogLinkFormatter {

    fun finding(scanPath: Path, finding: Finding): String =
        "${scanPath.resolve(finding.filePath)}:${finding.lineNumber}"
}
