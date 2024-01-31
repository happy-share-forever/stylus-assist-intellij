fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "vip.guco"
version = "2.3.1"

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set("2022.2.5")
    type.set("IC")
    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(listOf(/* Plugin Dependencies */))
}

tasks {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("222.*")
        untilBuild.set("234.*")
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
