import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

compose.resources {
    packageOfResClass = "com.finley.android.shared"
    generateResClass = always
}

// 兼容 Kotlin 2.1.10+ 的 Web 构建镜像配置
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    val nodeJs = rootProject.extensions.getByName("kotlinNodeJs")
    nodeJs.javaClass.methods.find { it.name == "setDownloadRoot" || it.name == "setNodeDownloadRoot" }
        ?.invoke(nodeJs, "https://mirrors.huaweicloud.com/nodejs-release/")
}
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    val yarn = rootProject.extensions.getByName("kotlinYarn")
    yarn.javaClass.methods.find { it.name == "setDownloadBaseUrl" }
        ?.invoke(yarn, "https://mirrors.huaweicloud.com/yarn/")
}

compose.desktop {
    application {
        mainClass = "com.finley.android.shared.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "24BestPoint"
            packageVersion = "1.0.0"
        }
    }
}

kotlin {
    android {
        namespace = "com.finley.android.shared"
        compileSdk = 35
        minSdk = 29
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    androidLibrary {
        androidResources.enable = true
    }

    jvm() // Desktop target
    
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "composeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.androidx.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.viewmodel)
            implementation(libs.kotlinx.datetime)
            implementation("org.kotlincrypto.hash:sha2:0.6.1")
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
        }
        
        androidMain {
            // 显式包含资源目录，确保在 KMA 模式下资源能正确打包
            resources.srcDirs("src/commonMain/composeResources")
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.swing)
        }
        wasmJsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
