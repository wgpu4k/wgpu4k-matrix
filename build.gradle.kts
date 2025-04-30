@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.adarshr.test-logger") version "4.0.0"
    id("com.vanniktech.maven.publish") version "0.31.0"
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }

    jvm {}
    js {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
    wasmWasi {
        nodejs()
    }
    mingwX64()
    macosX64()
    iosArm64()
    linuxX64()


    sourceSets {
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
group = "io.github.natanfudge"
version = "0.1.0"

val artifactId = "wgpu4k-matrix"
val githubUrl = "https://github.com/natanfudge/wgpu4k-matrix"


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(group.toString(), artifactId, version.toString())
    pom {
        name = artifactId
        description = "Basic math primitives for WebGPU + Kotlin Multiplatform"
        inceptionYear = "2025"
        url = githubUrl
        licenses {
            license {
                name = "The MIT License"
            }
        }
        developers {
            developer {
                name = "Fudge"
                url = "https://github.com/natanfudge"
            }
        }
        scm {
            url = githubUrl
        }
    }
}

afterEvaluate {
    tasks["publishAndReleaseToMavenCentral"].dependsOn("allTests")
}



