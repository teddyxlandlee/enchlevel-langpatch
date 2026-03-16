import java.io.DataOutputStream
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.encoding.Base64
import kotlin.io.path.createParentDirectories
import kotlin.io.path.outputStream
import me.modmuss50.mpp.ReleaseType

buildscript {
    dependencies {
        classpath("com.google.guava:guava:33.5.0-jre")
    }
}

plugins {
    `java-library`
    idea
    id("me.modmuss50.mod-publish-plugin") version "1.1.0"
    id("xland.gradle.forge-init-injector") version "3.1.0"
    `maven-publish`
}

base {
    archivesName.set(project.ext["archives_base_name"].toString())
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

group = project.ext["maven_group"]!!
version = project.ext["mod_version"]!!

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net") {
            name = "Fabric"
        }
        maven("https://libraries.minecraft.net") {
            name = "Mojang"
        }
        maven("https://mvn.7c7.icu") {
            name = "7c7maven"
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }
    }
}

dependencies {
    implementation("net.fabricmc:fabric-loader:${project.ext["loader_version"]}")
    compileOnlyApi("com.google.guava:guava:21.0")
    implementation("it.unimi.dsi:fastutil:8.2.1")
    implementation("net.fabricmc:sponge-mixin:0.11.4+mixin.0.8.5") {
        isTransitive = false
    }
    implementation("org.ow2.asm:asm:6.2")       // used by MCF 1.13.2
    implementation("org.ow2.asm:asm-tree:6.2")  // used by MCF 1.13.2
    implementation("org.apache.logging.log4j:log4j-api:2.8.1")

    compileOnlyApi("org.apiguardian:apiguardian-api:1.1.2")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core:2.25.3")
}

forgeInitInjector {
    modId = "enchlevellangpatch"
    stubPackage = "I_CDNx2L8vzK6E5HtDsRJ"
    setClientEntrypoint("xland/mcmod/enchlevellangpatch/impl/LangPatchImpl")
    neoFlag("pre_20_5", "post_20_5")
    supportLegacyForgeLifecycle = true
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching(listOf("fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml")) {
        expand("version" to project.version)
    }
}

tasks.register("checkValueTableSum") {
    val valueTableFile = file("src/main/resources/xland/mcmod/enchlevellangpatch/impl/ValueTable.txt")
    val expectedHash = "3c09cc78904fc47fd583b680ecfa9e2ad7370787ea149d843a56fb8f8c15c8d4"

    @Suppress("UnstableApiUsage")
    doLast {
        val hasher = com.google.common.hash.Hashing.sha256().newHasher()
        valueTableFile.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var len: Int
            while (input.read(buffer).also { len = it } != -1) {
                hasher.putBytes(buffer, 0, len)
            }
            check(hasher.hash().toString() == expectedHash) {
                "Hash mismatch"
            }
        }
    }
}

tasks.processResources {
    finalizedBy("checkValueTableSum")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (JavaVersion.current().isJava9Compatible) {
        options.release.set(8)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withSourcesJar()
}

tasks.javadoc {
    isFailOnError = false
    options {
        locale = "en_US"    // this option is invalid in Java 17
        jFlags("-Duser.language=en", "-Duser.country=US")

        encoding = "UTF-8"
        header("LangPatch").apply {
            links(
                "https://docs.oracle.com/en/java/javase/17/docs/api/",
                "https://javadoc.io/doc/com.google.guava/guava/21.0/",
                "https://javadoc.io/doc/org.jetbrains/annotations/26.1.0/",
                "https://javadoc.io/doc/org.apiguardian/apiguardian-api/1.1.2/",
            )
            tags("implNote")
        }
        include("xland/mcmod/enchlevellangpatch/api/**")
    }
}

tasks.jar {
    from("LICENSE") {
        rename { "META-INF/LICENSE_${project.base.archivesName.get()}" }
    }
    manifest.attributes(
        "Specification-Title"      to "Enchantment Level Language Patch",
        "Specification-Vendor"     to "teddyxlandlee",
        "Specification-Version"    to "1",  // We are version 1 of ourselves
        "Implementation-Title"     to project.name,
        "Implementation-Version"   to project.version,
        "Implementation-Vendor"    to "teddyxlandlee",
        "Implementation-Timestamp" to Instant.now(),
        "MixinConfigs"             to "ellp-forge.mixins.json", // for Forge FML
    )
}

//<editor-fold desc="Template Packs" defaultstate="collapsed">
object TemplatePacks {  // break implicit inner class declaration
    abstract class PackTemplate @Inject constructor(@get:Input val templateId: String): Jar() {
        private val stubModId = "lpstub_$templateId"
        private companion object {
            val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val pkgEncoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

            fun String.encodeBase64Identifier() = pkgEncoder.encode(encodeToByteArray()).replace('-', '$')
            fun String.decodeBase64() = pkgEncoder.decode(this)
            fun CopySpec.renameTo(s: String) = rename { s }
        }

        init {
            val packVersion = run {
                val time = dateFormatter.format(ZonedDateTime.now())
                val patch = project.providers.gradleProperty("template_patch_number").getOrElse("0")
                "$time.$patch"
            }

            val templateProperties = mapOf(
                "pack_id" to stubModId,
                "pack_version" to packVersion,
                "pack_desc" to "Generated stub pack for enchlevel-langpatch: '$templateId'"
            )
            inputs.properties(templateProperties)
            from(project.file("pack_templates/fmj_templates.json")) {
                renameTo("fabric.mod.json")
                expand(templateProperties)
            }
            listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml").forEach { fn ->
                from(project.file("pack_templates/modinfo_templates.toml")) {
                    renameTo(fn)
                    expand(templateProperties)
                }
            }

            listOf("en_us", "zh_cn", "lzh").forEach { langCode ->
                val targetFile = "assets/langpatch-generated-stubpack/lang/$langCode.json"
                from(project.file("pack_templates/lang/$templateId.json")) {
                    renameTo(targetFile)
                }
            }
            from(project.file("pack_templates/pack.mcmeta")) {
                renameTo("pack.mcmeta")
            }

            archiveFileName.convention(project.provider {
                "lpstub-$templateId-$packVersion.jar"
            })

            genClass()

            destinationDirectory = project.layout.buildDirectory.dir("lpstub")
        }

        fun genClass() {
            val pkgName = "lpstub_${templateId.encodeBase64Identifier()}"
            val classFile = project.layout.buildDirectory.dir("tmp/lpstub").map {
                it.file("$pkgName.bin")
            }
            val className = "$pkgName/a"
            val genClass = project.tasks.register("packTemplateModClass_$templateId") {
                val parts = Triple(
                    "yv66vgAAADQAEQE=", // ClassFile Header
                    // CP#1: utf8 modId
                    "AQ==", // Constant '1'
                    // CP#2: utf8 className
                    "BwACAQAQamF2YS9sYW5nL09iamVjdAcABAEAI0xuZXQvbWluZWNyYWZ0Zm9yZ2UvZm1sL2NvbW1" +
                            "vbi9Nb2Q7AQAFdmFsdWUBAB5MbmV0L25lb2ZvcmdlZC9mbWwvY29tbW9uL01vZDsBAA" +
                            "Y8aW5pdD4BAAMoKVYMAAkACgoABQALAQAJR2VuZXJhdGVkAQAEQ29kZQEAClNvdXJjZ" +
                            "UZpbGUBABlSdW50aW1lVmlzaWJsZUFubm90YXRpb25zACEAAwAFAAAAAAABAAEACQAK" +
                            "AAEADgAAABEAAQABAAAABSq3AAyxAAAAAAACAA8AAAACAA0AEAAAABQAAgAGAAEAB3M" +
                            "AAQAIAAEAB3MAAQ==",    // Rest of classfile
                )
                doLast {
                    with(classFile.get().asFile.toPath()) {
                        createParentDirectories()
                        DataOutputStream(outputStream()).use { o ->
                            o.write(parts.first.decodeBase64())
                            o.writeUTF(stubModId)
                            o.write(parts.second.decodeBase64())
                            o.writeUTF(className)
                            o.write(parts.third.decodeBase64())
                        }

                    }
                }
            }
            from(genClass) {
                renameTo("$className.class")
            }
        }
    }
}

tasks.register<TemplatePacks.PackTemplate>("packAllArabic", "all_arabic")
tasks.register<TemplatePacks.PackTemplate>("packAllRoman", "roman")

tasks.register("templatePacks") {
    dependsOn("packAllArabic", "packAllRoman")
}

tasks.build {
    dependsOn("templatePacks")
}
//</editor-fold>

fun javaVersions(range: IntRange) = range.map(JavaVersion::toVersion)

publishMods {
    file = tasks.jar.flatMap { it.archiveFile }
    modLoaders.addAll("fabric", "forge", "neoforge", "quilt")
    type = providers.gradleProperty("release_type").map(ReleaseType::of)
    changelog = providers.gradleProperty("changelog")
    displayName = "[1.16+/ALL] LangPatch ${project.version}"

    val supportedAncientVersions = listOf("1.13.2")

    curseforge {
        projectId = "529854"
        minecraftVersionRange {
            start = "1.14"
            end = "latest"
        }
        minecraftVersions.addAll(supportedAncientVersions)
        // CurseForge supports up to Java 22
        javaVersions.addAll(javaVersions(8..22))
        clientRequired = true
        serverRequired = false
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
    }

    modrinth {
        projectId = "Lf4kDKU9"
        minecraftVersionRange {
            start = "18w43b"    // the first version to have Fabric
            end = "latest"
            includeSnapshots = true
        }
        minecraftVersions.addAll(supportedAncientVersions)
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
    }

    additionalFiles.from(
        tasks["packAllArabic"], tasks["packAllRoman"]
    )
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifact(tasks.jar)
            artifact(tasks["sourcesJar"])
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.test {
    failOnNoDiscoveredTests = false
}
