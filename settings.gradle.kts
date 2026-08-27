pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    @Suppress("UnstableApiUsage")
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
