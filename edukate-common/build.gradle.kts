plugins {
    java
    kotlin("jvm")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("io.github.sanyavertolet.edukate.buildutils.kotlin-quality-configuration")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

kotlin {
    jvmToolchain(23)
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.edukateMessaging)

    implementation(libs.spring.boot.starter.webflux)
    // TODO: uncomment after Kotlin rewrite
    // implementation(libs.reactor.kotlin.extensions)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}
