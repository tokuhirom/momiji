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
    js {
        nodejs {
            testTask {
                useMocha {
                    timeout = "10000" // 10 seconds timeout
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
                implementation("io.github.tokuhirom.kdary:kdary:0.9.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val jvmTest by getting {
            dependencies {
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
