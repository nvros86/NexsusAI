pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
