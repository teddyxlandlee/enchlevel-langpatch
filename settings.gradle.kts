pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://mvn.7c7.icu") {
            name = "7c7maven"
        }
        maven("https://maven.fabricmc.net") {
            name = "Fabric"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "enchlevel-langpatch"
