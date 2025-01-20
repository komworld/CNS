import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.pluginYml)
}

group = "me.prdis"
version = "0.0.1"
val codeName = "CNS"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    library(kotlin("stdlib"))

    compileOnly(libs.paper)
    compileOnly(libs.cloud)
    compileOnly(libs.worldedit)

    bukkitLibrary(libs.cloud)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks {
    jar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set("")
    }
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs = listOf("-Dcom.mojang.eula.agree=true")
        downloadPlugins.from(runPaper.downloadPluginsSpec {
            modrinth("worldedit", "HIoAq6RI")
        })
    }
}

idea {
    module {
        excludeDirs.addAll(listOf(file("run"), file("out"), file(".idea"), file(".kotlin")))
    }
}

bukkit {
    val mc = libs.versions.minecraft.get()

    name = rootProject.name
    version = rootProject.version.toString()
    main = "${project.group}.${codeName.lowercase()}.plugin.${codeName.replaceFirstChar { it.uppercase() }}Plugin"

    depend = listOf("WorldEdit")

    apiVersion = mc.split(".").apply { if (this.size == 3) dropLast(1) }.joinToString(".")
}