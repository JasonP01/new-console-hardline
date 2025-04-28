pluginManagement{
    repositories{
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

if(JavaVersion.current().ordinal < JavaVersion.VERSION_17.ordinal) throw GradleException("JDK 17 is a required minimum version. Yours: ${System.getProperty("java.version")}")

rootProject.name = "new-console-hardline"
include("kts-eval")
