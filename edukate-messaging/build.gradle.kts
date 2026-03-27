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

tasks.bootJar { enabled = false }

tasks.jar { enabled = true }

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.amqp)
}
