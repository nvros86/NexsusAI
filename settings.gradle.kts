pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NexsusAI"

include(":app")
include(":core")
include(":domain")
include(":data")
include(":di")
include(":feature:tabs")
include(":feature:settings")
include(":feature:editor")
include(":feature:aiprovider")
include(":feature:localai")
include(":feature:teamworkspaces")
