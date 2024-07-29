import org.gradle.api.tasks.testing.logging.TestExceptionFormat
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
    macosX64()
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
                implementation("io.kotest:kotest-framework-engine:5.9.1")
                implementation("io.kotest:kotest-framework-datatest:5.9.1")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":momiji-ipadic-resources"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(project(":momiji-ipadic-code"))
            }
        }
        val linuxX64Main by getting {
            dependencies {
                implementation(project(":momiji-ipadic-code"))
            }
        }
        val macosArm64Main by getting {
            dependencies {
                implementation(project(":momiji-ipadic-code"))
            }
        }
        val macosX64Main by getting {
            dependencies {
                implementation(project(":momiji-ipadic-code"))
            }
        }
    }
}

tasks.withType<Test> {
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    useJUnitPlatform()
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
