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

repositories {
    mavenCentral()
}

tasks.bootJar { enabled = false }

tasks.jar {
    enabled = true
}

dependencies {
    implementation(libs.spring.boot.starter)
    apiElements(libs.awssdk.s3)
    implementation(libs.awssdk.s3.transfer.manager)
    implementation(libs.jackson.annotations)
    implementation(libs.reactor.core)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.kotlin.reflect)

    annotationProcessor(libs.spring.boot.configuration.processor)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.jackson.module.kotlin)
    testImplementation("io.projectreactor:reactor-test")
    testImplementation(libs.mockk)
    testImplementation(libs.springmockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
