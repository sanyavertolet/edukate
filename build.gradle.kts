import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    java
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.bmuschko.docker-remote-api") version "9.4.0"
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
}

tasks.register<DockerBuildImage>("buildFrontendImage") {
    group = "docker"
    description = "Builds the frontend Docker image"

    inputDir.set(file("${project.projectDir}/edukate-frontend"))
    images.add("registry.digitalocean.com/edukate-container-registry/edukate-frontend:latest")
    images.add("registry.digitalocean.com/edukate-container-registry/edukate-frontend:${project.version}")
    platform.set("linux/amd64")
    buildArgs.set(mapOf(
        "NODE_ENV" to "production"
    ))
}

tasks.register<DockerPushImage>("pushFrontendImage") {
    group = "docker"
    description = "Pushes the frontend Docker image to DigitalOcean registry"

    images.add("registry.digitalocean.com/edukate-container-registry/edukate-frontend:latest")
    images.add("registry.digitalocean.com/edukate-container-registry/edukate-frontend:${project.version}")
    registryCredentials {
        url.set("registry.digitalocean.com")
        username.set(providers
            .gradleProperty("do.docker.username")
            .orElse(providers.environmentVariable("DO_DOCKER_USERNAME"))
            .orNull
        )
        password.set(providers
            .gradleProperty("do.docker.password")
            .orElse(providers.environmentVariable("DO_DOCKER_PASSWORD"))
            .orNull
        )
    }
}

tasks.register("buildAndPushFrontendImage") {
    group = "docker"
    description = "Builds and pushes the frontend Docker image"
    dependsOn("buildFrontendImage", "pushFrontendImage")
    tasks.findByName("pushFrontendImage")?.mustRunAfter("buildFrontendImage")
}

tasks.register("buildAllImages") {
    group = "docker"
    description = "Builds and pushes both frontend and backend Docker images"
    dependsOn("edukate-backend:bootBuildImage", "buildAndPushFrontendImage")
}
