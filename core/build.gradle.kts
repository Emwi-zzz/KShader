plugins {
    kotlin("multiplatform")
}
val generatedPath = rootProject.extra["generatedPath"] as String
val skslTypeGeneratorTask = project.tasks.register("skslTypeGenerator") {
    dependsOn(":sksl-typegen:run")
    outputs.dir(generatedPath)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin.sourceSets.getByName("commonMain").generatedKotlin.srcDir(skslTypeGeneratorTask)

kotlin {
    jvmToolchain(25)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain {
            kotlin.srcDirs("src/main/kotlin")
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        jvmTest {
            kotlin.srcDirs("src/test/kotlin")
            dependencies {
                implementation(kotlin("reflect"))
            }
        }
    }
}



tasks.register("test") {
    dependsOn(tasks.named("jvmTest"))
    group = "verification"
    description = "Runs the JVM unit tests (alias for jvmTest)"
}

