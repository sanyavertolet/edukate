plugins {
    java
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    id("io.github.sanyavertolet.edukate.buildutils.spring-boot-app-configuration")
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
    implementation(projects.edukateCommon)
    implementation(projects.edukateAuth)

    implementation(libs.spring.boot.starter.webflux)
    // TODO: uncomment after Kotlin rewrite
    // implementation(libs.reactor.kotlin.extensions)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.cloud.starter.gateway)
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    implementation(libs.snakeyaml)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    useJUnitPlatform()
}
