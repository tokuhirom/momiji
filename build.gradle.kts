plugins {
    id("root.publication")
    id("module.publication")

    kotlin("multiplatform") version "2.0.21" apply false

    id("io.gitlab.arturbosch.detekt") version "1.23.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.vanniktech.maven.publish") version "0.29.0" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.github.tokuhirom.momiji"
val libVersion =
    System.getenv("LIB_VERSION") ?: (
        "1.0.0" +
            if (hasProperty("release")) {
                ""
            } else {
                "-SNAPSHOT"
            }
    )
version = libVersion

allprojects {
    repositories {
        mavenCentral()
    }

    group = "io.github.tokuhirom.momiji"
    version = libVersion
}
