pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "sample-wasm"

includeBuild("../..") {
    dependencySubstitution {
        substitute(module("dev.emwi-zzz:core")).using(project(":core"))
    }
}
