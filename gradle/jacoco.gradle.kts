tasks.register<JacocoReport>("jacocoCoverageReport") {
    group = "Reporting"
    description = "Execute unit tests, generate and combine Jacoco coverage report"

    reports {
        html.required.set(true)
    }

    // Jacoco resolves package-relative Kotlin paths against these source roots.
    sourceDirectories.setFrom(
        layout.projectDirectory.dir("src/main/kotlin"),
        layout.projectDirectory.dir("src/main/java")
    )
    // Set class directories to compiled Java and Kotlin classes, excluding specified exclusions
    classDirectories.setFrom(files(
        fileTree(layout.buildDirectory.dir("classes/kotlin/main"))
    ))
    // Collect execution data from .exec and .ec files generated during test execution
    executionData.setFrom(files(
        fileTree(layout.buildDirectory) { include(listOf("**/*.exec", "**/*.ec")) }
    ))

    dependsOn("test")

    doLast {
        val reportUrl =
            layout.buildDirectory.file("reports/jacoco/jacocoCoverageReport/html/index.html")
                .get().asFile.toURI()
        println("Jacoco report generated at: file://${reportUrl.path}")
    }
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification"){
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.9".toBigDecimal()
            }
        }
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.9".toBigDecimal()
            }
        }
    }
    // Jacoco resolves package-relative Kotlin paths against these source roots.
    sourceDirectories.setFrom(
        layout.projectDirectory.dir("src/main/kotlin"),
        layout.projectDirectory.dir("src/main/java")
    )
    // Set class directories to compiled Java and Kotlin classes, excluding specified exclusions
    classDirectories.setFrom(files(
        fileTree(layout.buildDirectory.dir("classes/kotlin/main"))
    ))
    // Collect execution data from .exec and .ec files generated during test execution
    executionData.setFrom(files(
        fileTree(layout.buildDirectory) { include(listOf("**/*.exec", "**/*.ec")) }
    ))
    dependsOn("jacocoCoverageReport")
}
