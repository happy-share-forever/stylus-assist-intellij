fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
}

group = "vip.guco"
version = "2.4.0"

// ------------------------------------------------------------
// IntelliJ Platform Plugin configuration
// ------------------------------------------------------------
intellijPlatform {
    pluginConfiguration {
        id = "vip.guco.stylusassist"
        name = "Stylus Assist"
        description = "An IntelliJ plugin that assists Stylus CSS editing."
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "252"
            untilBuild = "259.*"
        }
    }

    // 发布配置
    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

// ------------------------------------------------------------
// Repositories
// ------------------------------------------------------------
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// ------------------------------------------------------------
// Dependencies
// ------------------------------------------------------------
dependencies {
    implementation(kotlin("stdlib"))

    intellijPlatform {
        // 多种方式支持本地 IDE 路径配置：
        // 1. 环境变量: export INTELLIJ_PATH="/path/to/ide"
        // 2. 命令行参数: -PplatformPath="~/Applications/WebStorm.app"
        // 3. gradle.properties 中的 platformPath 属性
        val envPath = providers.environmentVariable("INTELLIJ_PATH").orNull
        val localIdePath = providers.gradleProperty("localIdePath").orNull
        val platformPath = providers.gradleProperty("platformPath").orNull

        // 处理波浪号路径展开
        fun expandPath(path: String?): String? {
            if (path == null) return null
            return if (path.startsWith("~")) {
                System.getProperty("user.home") + path.substring(1)
            } else {
                path
            }
        }

        val chosenLocalPath = listOf(
            expandPath(envPath),
            expandPath(platformPath),
            expandPath(localIdePath)
        ).firstOrNull { !it.isNullOrBlank() }

        if (!chosenLocalPath.isNullOrBlank()) {
            println("使用本地 IDE: $chosenLocalPath")
            local(chosenLocalPath)
        } else {
            println("使用下载的 IntelliJ IDEA Community Edition 2024.3")
            intellijIdea("2024.3")
        }
        // 由于 bundled plugin 可能不存在，先移除所有 bundled plugins
        // bundledPlugins("com.intellij.java")
    }
}

// ------------------------------------------------------------
// Compiler settings
// ------------------------------------------------------------
tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    // 禁用 buildSearchableOptions 任务，避免在本地 IDE 环境下的兼容性问题
    buildSearchableOptions {
        enabled = false
    }
}
