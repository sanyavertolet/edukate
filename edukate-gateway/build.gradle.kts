plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    id("io.github.sanyavertolet.edukate.buildutils.spring-boot-app-configuration")
    id("io.github.sanyavertolet.edukate.buildutils.kotlin-quality-configuration")
    id("io.github.sanyavertolet.edukate.buildutils.springdoc-spec-generation")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvmToolchain(21)
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
    implementation(libs.reactor.kotlin.extensions)
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

    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
