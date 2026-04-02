plugins {
    java
}

dependencies {
    // Available since 1.12.2 (or even earlier)
    // Removed in a certain version after 1.17, that's why we need
    // java.net.http for 1.17+ (in `java16` sourceset)
    compileOnly("org.apache.httpcomponents:httpclient:4.3.3")
    implementation("org.apache.logging.log4j:log4j-api:2.8.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(if ("Java16" in name) 16 else 8)
    }
}

val allSources by configurations.creating

val java16 by sourceSets.registering {
    java.srcDir("src/java16/java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
}

val finalZip by tasks.registering(Zip::class) {
    dependsOn(tasks.compileJava, "compileJava16Java")
    from(sourceSets.main.map { it.output })
    from(java16.map { it.output })
}

artifacts {
    add("allSources", finalZip)
}