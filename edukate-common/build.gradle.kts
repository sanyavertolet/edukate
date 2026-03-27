plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
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

tasks.bootJar { enabled = false }

tasks.jar { enabled = true }

dependencies {
    implementation(projects.edukateMessaging)

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.springdoc.openapi.starter.webflux.ui)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
