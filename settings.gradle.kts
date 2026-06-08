pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Easy"
include(":app")

// Salida de build fuera de OneDrive (evita "Unable to delete directory" en app/build).
val easyLocalBuild = java.io.File(
    System.getenv("LOCALAPPDATA") ?: System.getProperty("user.home"),
    "Easy-Android-build",
)
gradle.beforeProject {
    val subpath = project.path.removePrefix(":").ifBlank { "root" }
    project.layout.buildDirectory.set(easyLocalBuild.resolve(subpath))
}