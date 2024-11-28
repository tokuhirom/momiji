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

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

                implementation(project.dependencies.platform("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:1.0.0-pre.840"))

                implementation("org.jetbrains.kotlin-wrappers:kotlin-emotion")

                implementation("org.jetbrains.kotlin-wrappers:kotlin-react")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-use")

                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-base")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-icons-material")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-lab")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-material")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-mui-system")

                implementation(project.dependencies.platform("io.ktor:ktor-bom:3.0.1"))
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
