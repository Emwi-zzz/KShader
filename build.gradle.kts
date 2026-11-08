plugins {
    kotlin("multiplatform") version "2.4.10" apply false
    kotlin("jvm") version "2.4.10" apply false
}

group = "dev.emwi-zzz"
version = "0.0"

extra.set("generatedPath", project.layout.buildDirectory.dir("generated-src/kotlin/commonMain").get().asFile.absolutePath)

allprojects {
    repositories {
        mavenCentral()
    }
}

