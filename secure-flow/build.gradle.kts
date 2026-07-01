plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm")
}

group = "dev.randos.secureflow"
version = "0.1.0"

gradlePlugin {
    plugins {
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
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}
