package dev.randos.secureflow.gradle.utils

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class SecureFlowExtension @Inject constructor(project: Project) {
    abstract val scanDirectory: DirectoryProperty
    abstract val reportDirectory: DirectoryProperty
    abstract val failOnFindings: Property<Boolean>

    init {
        scanDirectory.convention(project.layout.projectDirectory)
        reportDirectory.convention(project.layout.buildDirectory.dir("reports/secure-flow"))
        failOnFindings.convention(true)
    }
}