plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("io.ktor.plugin") version "3.0.3"
    application
}

application {
    mainClass.set("com.finley.android.server.ApplicationKt")
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.content.negotiation)
    
    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.hikari)
    
    // Logging
    implementation(libs.logback)
}
