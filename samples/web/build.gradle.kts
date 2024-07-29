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
            webpackTask {
                this.mainOutputFileName = "web.[contenthash].js"
            }
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
                implementation("io.ktor:ktor-client-logging")

                implementation(devNpm("html-webpack-plugin", "5.5.0"))
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

tasks.named<ProcessResources>("jsProcessResources") {
    exclude("index.html")
}
