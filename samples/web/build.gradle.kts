import Mecab_dict_ipadic_gradle.*

plugins {
    kotlin("multiplatform")
    id("module.publication")
    id("mecab.dict.ipadic")
}

kotlin {
    js {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":momiji-core"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")

                implementation(project.dependencies.platform("io.ktor:ktor-bom:2.3.12"))
                implementation("io.ktor:ktor-client-core")
                implementation("io.ktor:ktor-client-js")
//                implementation("io.ktor:ktor-serialization-kotlinx-json")
//                implementation("io.ktor:ktor-client-content-negotiation")
//                implementation("io.ktor:ktor-client-serialization")
                implementation("io.ktor:ktor-client-logging")
//                implementation("io.ktor:ktor-client-json")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.register<BuildDictTask>("buildDict") {
    type = "resources"
}
