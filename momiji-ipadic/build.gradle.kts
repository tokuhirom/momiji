@file:Suppress("ktlint:standard:no-wildcard-imports", "DEPRECATION")

import Mecab_dict_ipadic_gradle.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("module.publication")
    id("mecab.dict.ipadic")
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm()
//    js {
//        nodejs {
//            testTask {
//                useMocha {
//                    timeout = "10000" // 10 seconds timeout
//                }
//            }
//        }
//    }
//    macosArm64()
//    macosX64()
//    linuxX64()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/generated/commonMain/kotlin")

            dependencies {
                implementation("io.github.tokuhirom.kdary:kdary:0.9.2")
            }
        }
    }
}

tasks.register<BuildDictTask>("buildDict")

// Example task dependency
tasks.getByName("build").dependsOn("buildDict")

/*
mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Dokka("dokkaHtml")))
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    if (project.hasProperty("mavenCentralUsername") ||
        System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername") != null
    ) {
        signAllPublications()
    }
}

tasks.dokkaHtml {
    dokkaSourceSets {
        configureEach {
            includeNonPublic = false
        }
    }
}
*/
