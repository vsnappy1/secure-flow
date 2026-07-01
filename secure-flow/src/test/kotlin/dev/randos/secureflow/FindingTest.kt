package dev.randos.secureflow

import dev.randos.secureflow.gradle.model.Finding
import dev.randos.secureflow.gradle.type.Severity
import org.junit.Assert.assertEquals
import org.junit.Test

class FindingTest {
    @Test
    fun exposesDataClassBehavior() {
        val finding = Finding(
            severity = Severity.CRITICAL,
            ruleId = "HardcodedSecret",
            message = "Suspicious hardcoded secret detected",
            filePath = "src/main/App.kt",
            lineNumber = 5,
            evidence = "sk-1...mnop"
        )

        val (severity, ruleId, message, filePath, lineNumber, evidence) = finding

        assertEquals(Severity.CRITICAL, severity)
        assertEquals("HardcodedSecret", ruleId)
        assertEquals("Suspicious hardcoded secret detected", message)
        assertEquals("src/main/App.kt", filePath)
        assertEquals(5, lineNumber)
        assertEquals("sk-1...mnop", evidence)
        assertEquals(finding.hashCode(), finding.copy().hashCode())
    }
}
