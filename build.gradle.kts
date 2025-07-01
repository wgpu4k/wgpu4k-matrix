@file:OptIn(ExperimentalWasmDsl::class)

import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest
import org.jetbrains.kotlin.gradle.tasks.KotlinTest

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.vanniktech.maven.publish") version "0.31.0"
    kotlin("plugin.serialization") version "2.2.0-RC"
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
        commonMain {
            dependencies {
                // We don't need to pull in this dependency transitively
                compileOnly(libs.serialization)
                api(libs.serialization)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
group = "io.github.natanfudge"
version = "0.5.1"

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


tasks.withType<KotlinTest>() {
    testLogging { // credits: https://stackoverflow.com/a/36130467/5917497
        // set options for log level LIFECYCLE
        events = setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
            TestLogEvent.STANDARD_OUT
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
        // set options for log level DEBUG and INFO
        debug {
            events = setOf(
                TestLogEvent.STARTED,
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT
            )
            exceptionFormat = TestExceptionFormat.FULL
        }
        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat
        afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
            if (desc.parent == null) { // will match the outermost suite
                val pass = "${Color.GREEN}${result.successfulTestCount} passed${Color.NONE}"
                val fail = "${Color.RED}${result.failedTestCount} failed${Color.NONE}"
                val skip = "${Color.YELLOW}${result.skippedTestCount} skipped${Color.NONE}"
                val type = when (val r = result.resultType) {
                    TestResult.ResultType.SUCCESS -> "${Color.GREEN}$r${Color.NONE}"
                    TestResult.ResultType.FAILURE -> "${Color.RED}$r${Color.NONE}"
                    TestResult.ResultType.SKIPPED -> "${Color.YELLOW}$r${Color.NONE}"
                }
                val output = "Results: $type (${result.testCount} tests, $pass, $fail, $skip)"
                val startItem = "|   "
                val endItem = "   |"
                val repeatLength = startItem.length + output.length + endItem.length - 36
                println("")
                println("\n" + ("-" * repeatLength) + "\n" + startItem + output + endItem + "\n" + ("-" * repeatLength))
            }
        }))
    }
    onOutput(KotlinClosure2({ _: TestDescriptor, event: TestOutputEvent ->
        if (event.destination == TestOutputEvent.Destination.StdOut) {
            logger.lifecycle(event.message.replace(Regex("""\s+$"""), ""))
        }
    }))
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = 1
}

operator fun String.times(x: Int): String {
    return List(x) { this }.joinToString("")
}

internal enum class Color(ansiCode: Int) {
    NONE(0),
    RED(31),
    GREEN(32),
    YELLOW(33);
    private val ansiString: String = "\u001B[${ansiCode}m"

    override fun toString(): String {
        return ansiString
    }
}