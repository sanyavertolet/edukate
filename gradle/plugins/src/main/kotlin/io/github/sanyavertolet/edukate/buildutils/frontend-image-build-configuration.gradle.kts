package io.github.sanyavertolet.edukate.buildutils

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage

plugins {
    id("com.bmuschko.docker-remote-api")
}

configureDockerExtension()

private val generateFrontendEnvTask = tasks.register("generateFrontendEnv") {
    group = "frontend"
    description = "Writes .env.production.local with the reckon version for the Vite build"
    doLast {
        file("edukate-frontend/.env.production.local")
            .writeText("VITE_APP_VERSION=${project.version}\n")
    }
}

private val buildTask = tasks.register<DockerBuildImage>("buildFrontendImage") {
    configureFrontendBuild()
    dependsOn(generateFrontendEnvTask)
}

private val pushTask = tasks.register<DockerPushImage>("pushFrontendImage") {
    configureFrontendPush()
    dependsOn(buildTask)
}

private val buildAndPushTask = tasks.register("buildAndPushFrontendImage") {
    group = "docker"
    description = "Builds and pushes the frontend Docker image"
    dependsOn(buildTask, pushTask)
}

tasks.register("buildAllImages") {
    group = "docker"
    description = "Builds and pushes both frontend and backend Docker images"
    dependsOn(
        "edukate-backend:bootBuildImage",
        "edukate-gateway:bootBuildImage",
        "edukate-notifier:bootBuildImage",
        buildAndPushTask,
    )
}
