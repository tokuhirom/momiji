pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "momiji"

include(":momiji-core")
include(":momiji-ipadic")
include(":momiji-ipadic-code")
include(":momiji-ipadic-resources")
include(":momiji-binary-dict")
