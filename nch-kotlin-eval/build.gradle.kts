import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
    //id("com.gradleup.shadow") version "8.3.6"
    //kotlin("kapt") version "2.1.20"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://maven.xpdustry.com/mindustry")
    maven("https://jitpack.io")
}

kotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-dependencies"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-compiler-embeddable"))


    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.6.4")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.2")

    /*
    implementation("org.jetbrains.exposed", "exposed-core", "0.41.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.41.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.41.1")


    implementation("dev.kord", "kord-core", "0.8.0-M17")
    implementation("org.sejda.webp-imageio", "webp-imageio-sejda", "0.1.0") // webp support for ImageIO
    implementation("info.debatty", "java-string-similarity", "2.0.0")
    implementation("info.picocli", "picocli", "4.7.0")

    implementation("com.github.mnemotechnician", "markov-chain", "1.0")

    kapt("info.picocli", "picocli-codegen", "4.7.0")
     */
}