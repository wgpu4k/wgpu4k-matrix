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
