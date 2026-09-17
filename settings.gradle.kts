pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.hixland.com") {
            name = "7c7maven"
        }
        maven("https://maven.fabricmc.net") {
            name = "Fabric"
        }
//        mavenLocal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "enchlevel-langpatch"
