plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false

    id("io.github.sanyavertolet.edukate.buildutils.frontend-image-build-configuration")
    kotlin("plugin.spring") version "2.3.0"
}

group = "io.github.sanyavertolet.edukate"

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
    gradlePluginPortal()
}


dependencies {
    implementation(kotlin("stdlib"))
}