fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0" /*https://plugins.gradle.org/m2/org/jetbrains/intellij/plugins/gradle-intellij-plugin*/
}

group = "io.github.lgp547"
version = "2.2.1-21"

repositories {
    mavenLocal() // 默认使用 ~/.m2/ repository 可自行修改成本地仓库地址
    mavenCentral()
}

dependencies {
    implementation("io.github.lgp547:any-door-core:2.2.1")
    implementation("io.github.lgp547:any-door-attach:2.2.1")

}

// 沙盒 idea 的版本
// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    // TODO 若没商业版授权，这里改成社区版进行调式
    version.set("2024.3.2.2")
    // type.set("IU") // 商业版
    type.set("IC") // 社区版

    plugins.set(listOf("com.intellij.java", "com.intellij.modules.json"))
}

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
