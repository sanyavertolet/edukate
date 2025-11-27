plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

tasks.jar {
    enabled = true
}

dependencies {
    implementation(libs.spring.boot.starter.data.mongodb.reactive)

    apiElements(libs.awssdk.s3)
    implementation(libs.awssdk.s3.transfer.manager)
    implementation(libs.reactor.core)

    compileOnly(libs.lombok)
    compileOnly(libs.fasterxml.jackson.annotations)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}
