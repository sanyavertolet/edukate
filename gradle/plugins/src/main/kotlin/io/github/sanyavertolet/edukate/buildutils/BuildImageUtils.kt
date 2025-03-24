package io.github.sanyavertolet.edukate.buildutils

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

/**
 * Sane default for task of type [BootBuildImage] for any module.
 * Sets image name and configures Docker Registries.
 */
fun BootBuildImage.commonConfigure() {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
    runImage.set("paketobuildpacks/run-jammy-base:latest")
    imageName.set("registry.digitalocean.com/edukate-container-registry/${project.name}")
    imagePlatform.set("linux/amd64")
    setPullPolicy("IF_NOT_PRESENT")
    tags.set(setOf(
        "registry.digitalocean.com/edukate-container-registry/${project.name}:latest",
    ))
    verboseLogging.set(true)
    publish.set(true)
    docker {
        publishRegistry {
            url.set("registry.digitalocean.com")
            username.set(project.providers
                .gradleProperty("do.docker.username")
                .orElse(project.providers.environmentVariable("DO_DOCKER_USERNAME"))
                .orNull
            )
            password.set(project.providers
                .gradleProperty("do.docker.password")
                .orElse(project.providers.environmentVariable("DO_DOCKER_PASSWORD"))
                .orNull
            )
        }
    }
}
