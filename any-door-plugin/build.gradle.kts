import java.util.Arrays

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("java")
    /**
     * org.jetbrains.intellij 专门用于 IntelliJ 平台插件开发，自动配置构建环境，包括依赖管理、IDE 版本适配
     * checkUrl: https://plugins.gradle.org/m2/org/jetbrains/intellij/plugins/gradle-intellij-plugin
     * 2023.3 及以上 ==> 需手动安装 Plugin DevKit 插件 ==> 1.17.4+
     *
     */
    // id("org.jetbrains.intellij") version "1.17.4"

    id("org.jetbrains.intellij.platform") version "2.5.0" // intellij 243+ 版本支持
}

group = "io.github.lgp547"
version = "2.2.1-21"

repositories {
    mavenLocal() // 默认使用 ~/.m2/ repository 可自行修改成本地仓库地址
    mavenCentral()

    // 使用 "org.jetbrains.intellij.platform"
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("io.github.lgp547:any-door-core:2.2.1")
    implementation("io.github.lgp547:any-door-attach:2.2.1")

    // 使用 "org.jetbrains.intellij.platform"
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugins(listOf("com.intellij.java", "com.intellij.modules.json"))
    }
}

// 使用 "org.jetbrains.intellij" 需要进行下面的配置
// 沙盒 idea 的版本
// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
//intellij {
//    // TODO 若没商业版授权，这里改成社区版进行调式
//    version.set("2024.2")
//    type.set("IC") // 商业版=type.set("IU") 社区版=type.set("IC")
//    // plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))
//    plugins.set(listOf("com.intellij.java"))
//}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))
    }

//    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//    }
//
//    publishPlugin {
//        token.set(System.getenv("PUBLISH_TOKEN"))
//    }
}
