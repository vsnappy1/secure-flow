import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    `java-gradle-plugin`
    `maven-publish`
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

apply(from = "../gradle/jacoco.gradle.kts")

group = "dev.randos.secureflow"
version = "0.1.0"

gradlePlugin {
    plugins {
        website.set("https://github.com/vsnappy1/secure-flow")
        vcsUrl.set("https://github.com/vsnappy1/secure-flow")
        create("secureFlow") {
            id = "dev.randos.secure-flow"
            implementationClass = "dev.randos.secureflow.gradle.SecureFlowPlugin"
            displayName = "Secure Flow"
            description = "A Gradle plugin for scanning source and configuration files for security findings."
            tags.set(listOf("security", "static analysis", "reporting"))
        }
    }
}

dependencies {
    testImplementation(libs.junit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}

tasks.withType<Test>().configureEach {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    )
}

ktlint {
    reporters {
        reporter(ReporterType.HTML)
    }
}
