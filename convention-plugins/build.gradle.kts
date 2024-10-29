plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("io.github.gradle-nexus.publish-plugin:io.github.gradle-nexus.publish-plugin.gradle.plugin:2.0.0")

    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-cio:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
}
