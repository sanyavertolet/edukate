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

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(projects.edukateCommon)
    implementation(projects.edukateAuth)
    implementation(projects.edukateMessaging)
    implementation(projects.edukateStorage)

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.aspectj)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.spring.boot.starter.opentelemetry)
    implementation(libs.logstash.logback.encoder)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.ai.starter.model.openai)
    implementation(libs.spring.boot.http.client)
    implementation(libs.kotlin.reflect)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
    testImplementation(libs.jackson.module.kotlin)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
