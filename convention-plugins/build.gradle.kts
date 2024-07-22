plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("io.github.gradle-nexus.publish-plugin:io.github.gradle-nexus.publish-plugin.gradle.plugin:2.0.0")

    implementation("io.github.tokuhirom.kdary:kdary:0.9.2")

    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
}
