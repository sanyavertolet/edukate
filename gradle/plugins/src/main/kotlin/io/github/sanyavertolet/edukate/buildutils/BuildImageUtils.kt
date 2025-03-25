package io.github.sanyavertolet.edukate.buildutils

import com.bmuschko.gradle.docker.DockerExtension
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

private const val DOCKER_REGISTRY_URL = "registry.digitalocean.com"
private const val DOCKER_CONTAINER_REGISTRY_NAME = "edukate-container-registry"

fun DefaultTask.version() = project.version.toString().replace("+", "-")

fun DefaultTask.fullImageName(
    imageName: String? = null,
) = "$DOCKER_REGISTRY_URL/$DOCKER_CONTAINER_REGISTRY_NAME/${imageName ?: project.name}"

fun DefaultTask.fullImageNameWithTag(
    imageName: String? = null,
    imageTag: String? = null,
) = "${fullImageName(imageName)}:${imageTag ?: version()}"

private fun Project.usernameFromPropertyOrEnv() = providers
    .gradleProperty("do.docker.username")
    .orElse(providers.environmentVariable("DO_DOCKER_USERNAME"))
    .orNull

private fun Project.passwordFromPropertyOrEnv() = providers
    .gradleProperty("do.docker.password")
    .orElse(providers.environmentVariable("DO_DOCKER_PASSWORD"))
    .orNull

/**
 * Sane default for task of type [BootBuildImage] for any module.
 * Sets image name and configures Docker Registries.
 */
fun BootBuildImage.commonConfigure(shouldPublishLatest: Boolean = true) {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
    runImage.set("paketobuildpacks/run-jammy-base:latest")
    imageName.set(fullImageName())
    imagePlatform.set("linux/amd64")
    setPullPolicy("IF_NOT_PRESENT")
    tags.set(
        setOfNotNull(
            fullImageNameWithTag(),
            fullImageNameWithTag(imageTag = "latest").takeIf { shouldPublishLatest }
        )
    )
    verboseLogging.set(true)
    publish.set(true)
    docker {
        publishRegistry {
            url.set(DOCKER_REGISTRY_URL)
            username.set(project.usernameFromPropertyOrEnv())
            password.set(project.passwordFromPropertyOrEnv())
        }
    }
}

fun DockerPushImage.configureFrontendPush(
    frontendName: String = "edukate-frontend",
    shouldPublishLatest: Boolean = true,
) {
    group = "docker"
    description = "Pushes the frontend Docker image to DigitalOcean registry"

    if (shouldPublishLatest) {
        images.add(fullImageNameWithTag(frontendName, "latest"))
    }
    images.add(fullImageNameWithTag(frontendName))
    registryCredentials {
        url.set(DOCKER_REGISTRY_URL)
        username.set(project.usernameFromPropertyOrEnv())
        password.set(project.passwordFromPropertyOrEnv())
    }
}

fun DockerBuildImage.configureFrontendBuild(
    frontendName: String = "edukate-frontend",
    shouldPublishLatest: Boolean = true,
) {
    group = "docker"
    description = "Builds the frontend Docker image"

    inputDir.set(project.layout.projectDirectory.dir(frontendName))
    if (shouldPublishLatest) {
        images.add(fullImageNameWithTag(frontendName, "latest"))
    }
    images.add(fullImageNameWithTag(frontendName))
    platform.set("linux/amd64")
    buildArgs.set(mapOf("NODE_ENV" to "production"))
}

fun Project.configureDockerExtension() {
    configure<DockerExtension> {
        registryCredentials {
            url.set(DOCKER_REGISTRY_URL)
            username.set(usernameFromPropertyOrEnv())
            password.set(passwordFromPropertyOrEnv())
        }
    }
}
