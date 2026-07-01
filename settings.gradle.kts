pluginManagement {

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
        }
    }
}

rootProject.name = "SecureFlow"
include(":secure-flow")
