@file:Suppress("ktlint:standard:no-wildcard-imports", "DEPRECATION")

import Mecab_dict_ipadic_gradle.*
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest


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

    jvm {
    }
    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "100000" // 100 seconds timeout
                }
            }
        }
    }
    macosArm64()
//    macosX64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":momiji-core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":momiji-ipadic-resources"))
            }
        }

        // otherMain
        val jsMain by getting {
            kotlin.srcDir("src/generated/otherMain/kotlin")
            kotlin.srcDir("src/otherMain/kotlin")
        }
        val linuxX64Main by getting {
            kotlin.srcDir("src/generated/otherMain/kotlin")
            kotlin.srcDir("src/otherMain/kotlin")
        }
        val macosArm64Main by getting {
            kotlin.srcDir("src/generated/otherMain/kotlin")
            kotlin.srcDir("src/otherMain/kotlin")
        }
    }
}

tasks.register<BuildDictTask>("buildDict") {
    url = "https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"
    dicType = "ipadic"
    type = "code"
}

tasks.withType<KotlinJvmTest> {
    jvmArgs =
        listOf(
            "-Xmx2g",
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-XX:HeapDumpPath=./heapdump.hprof",
        )
}

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
