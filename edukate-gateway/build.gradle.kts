plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

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

dependencies {
    implementation(projects.edukateCommon)
    implementation(projects.edukateAuth)

    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.cloud.starter.gateway)

    implementation(libs.snakeyaml)

    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
    runImage.set("paketobuildpacks/run-jammy-base:latest")
    imageName.set("registry.digitalocean.com/edukate-container-registry/edukate-gateway")
    imagePlatform.set("linux/amd64")
    pullPolicy.set(org.springframework.boot.buildpack.platform.build.PullPolicy.IF_NOT_PRESENT)
    tags.set(setOf(
        "registry.digitalocean.com/edukate-container-registry/edukate-gateway:latest",
    ))
    verboseLogging.set(true)
    publish.set(true)
    docker {
        publishRegistry {
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
}
