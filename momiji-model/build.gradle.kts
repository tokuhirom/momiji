import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    id("module.publication")
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
            dependencies {
                implementation("io.github.tokuhirom.kdary:kdary:0.9.2")

//                implementation("com.github.ajalt.clikt:clikt:4.4.0")

                implementation("io.ktor:ktor-client-core:2.3.12")
                implementation("io.ktor:ktor-client-cio:2.3.12")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

                implementation("com.squareup.okio:okio:3.9.0")
            }
        }
    }
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
