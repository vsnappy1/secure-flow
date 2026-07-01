package dev.randos.secureflow.gradle

import dev.randos.secureflow.gradle.task.SecureFlowCheckTask
import dev.randos.secureflow.gradle.utils.SecureFlowExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class SecureFlowPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "secureFlow",
            SecureFlowExtension::class.java,
            project
        )

        val checkTask = project.tasks.register(
            "secureFlowCheck",
            SecureFlowCheckTask::class.java
        ) { task ->
            task.group = "verification"
            task.description = "Scans source and configuration files for SecureFlow security findings."
            task.scanDirectory.set(extension.scanDirectory)
            task.reportDirectory.set(extension.reportDirectory)
            task.failOnFindings.set(extension.failOnFindings)
        }

        project.tasks.register("secureFlowReport") { task ->
            task.group = "reporting"
            task.description = "Generates SecureFlow markdown and JSON reports."
            task.dependsOn(checkTask)
        }
    }
}
