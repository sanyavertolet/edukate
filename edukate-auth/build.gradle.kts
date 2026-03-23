plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    id("io.github.sanyavertolet.edukate.buildutils.kotlin-quality-configuration")
}

kotlin {
    jvmToolchain(23)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.edukateCommon)

    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    implementation(libs.jjwt.impl)
    implementation(libs.jjwt.jackson)
    implementation(libs.reactor.core)
    implementation(libs.reactor.kotlin.extensions)
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
