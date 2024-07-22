plugins {
    id("root.publication")
    id("module.publication")

    kotlin("multiplatform") version "2.0.0" apply false

    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "io.github.tokuhirom.kdary"
version = System.getenv("LIB_VERSION") ?: (
    "1.0.0" +
        if (hasProperty("release")) {
            ""
        } else {
            "-SNAPSHOT"
        }
)

allprojects {
    repositories {
        mavenCentral()
    }
}
