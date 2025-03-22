plugins {
    java
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
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

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("software.amazon.awssdk:s3:2.25.6")
    implementation("software.amazon.awssdk:s3-transfer-manager:2.25.6")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
    runImage.set("paketobuildpacks/run-jammy-base:latest")
    imageName.set("registry.digitalocean.com/edukate-container-registry/edukate-backend")
    imagePlatform.set("linux/amd64")
    pullPolicy.set(org.springframework.boot.buildpack.platform.build.PullPolicy.IF_NOT_PRESENT)
    tags.set(setOf(
        "registry.digitalocean.com/edukate-container-registry/edukate-backend:latest",
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
