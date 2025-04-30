@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id ("com.adarshr.test-logger") version "4.0.0"
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
//    macosX64()
//    iosArm64()
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
