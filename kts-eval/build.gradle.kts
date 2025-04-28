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
    implementation(kotlin("reflect"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(kotlin("scripting-compiler-embeddable"))

    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.9.0")
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.8.1")
}