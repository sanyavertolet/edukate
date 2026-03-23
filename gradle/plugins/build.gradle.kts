import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // workaround https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.spring.boot.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.docker.remote.api.gradle.plugin)
    // String literals required: version catalog is not on the classpath inside included builds
    implementation("dev.detekt:detekt-gradle-plugin:2.0.0-alpha.2")
    implementation("com.ncorti.ktfmt.gradle:plugin:0.20.0")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}
