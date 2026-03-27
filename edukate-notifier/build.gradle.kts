plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)

    id("io.github.sanyavertolet.edukate.buildutils.spring-boot-app-configuration")
    id("io.github.sanyavertolet.edukate.buildutils.kotlin-quality-configuration")
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
    implementation(projects.edukateMessaging)

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.data.mongodb.reactive)
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation(libs.flapdoodle.embed.mongo)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
