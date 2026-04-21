plugins {
    java
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.detekt) apply false

    id("io.github.sanyavertolet.edukate.buildutils.frontend-image-build-configuration")
    kotlin("plugin.spring") version "2.3.0"
}

group = "io.github.sanyavertolet.edukate"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
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
//    implementation(kotlin("stdlib"))
}

// ---------------------------------------------------------------------------
// Aggregated reports
// ---------------------------------------------------------------------------

tasks.register<TestReport>("aggregateTestReport") {
    group = "verification"
    description = "Aggregates JUnit HTML reports from all subprojects."
    destinationDirectory.set(layout.buildDirectory.dir("reports/tests/aggregated"))
    testResults.from(subprojects.flatMap { it.tasks.withType<Test>() })
}

val detektReportMergeSarif by tasks.registering(dev.detekt.gradle.report.ReportMergeTask::class) {
    group = "verification"
    description = "Merges detekt SARIF reports from all subprojects."
    output.set(layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {
    plugins.withId("dev.detekt") {
        tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
            finalizedBy(detektReportMergeSarif)
            detektReportMergeSarif.configure { input.from(this@configureEach.reports.sarif.outputLocation) }
        }
    }
}

// ---------------------------------------------------------------------------
// Infrastructure (Docker Compose)
// ---------------------------------------------------------------------------

tasks.register<Exec>("infrastructureUp") {
    group = "infrastructure"
    description = "Starts all local infrastructure services (PostgreSQL, MongoDB, MinIO, RabbitMQ)."
    commandLine("sh", "-c", "docker compose up -d")
}

tasks.register<Exec>("infrastructureDown") {
    group = "infrastructure"
    description = "Stops all local infrastructure services."
    commandLine("sh", "-c", "docker compose down")
}

tasks.register("allReports") {
    group = "verification"
    description = "Generates all aggregated reports (tests + detekt)."
    dependsOn("aggregateTestReport", detektReportMergeSarif)
}