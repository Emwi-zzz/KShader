plugins {
    kotlin("jvm")
    application
}

val generatedPath = rootProject.extra["generatedPath"] as String

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

application {
    mainClass.set("dev.emwi.zzz.kotlin.sksl.typegen.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    args(generatedPath)
}