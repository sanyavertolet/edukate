# gradle/plugins

Convention plugins and build utilities shared across all edukate Kotlin modules.
This is an included build — declared in the root `settings.gradle.kts` via `includeBuild("gradle/plugins")`.

## Convention Plugins

| Plugin ID suffix                     | File                                            | Applied to                                                                               |
|:-------------------------------------|:------------------------------------------------|:-----------------------------------------------------------------------------------------|
| `spring-boot-app-configuration`      | `spring-boot-app-configuration.gradle.kts`      | All Spring Boot app modules                                                              |
| `frontend-image-build-configuration` | `frontend-image-build-configuration.gradle.kts` | Root project — frontend Docker build and push tasks                                      |
| `kotlin-quality-configuration`       | `kotlin-quality-configuration.gradle.kts`       | All Kotlin modules — configures detekt and ktfmt                                         |
| `springdoc-spec-generation`          | `springdoc-spec-generation.gradle.kts`          | Spring services with a public API (backend, gateway, notifier) — generates OpenAPI specs |

Full plugin ID prefix: `io.github.sanyavertolet.edukate.buildutils.<suffix>`

## Adding a New Convention Plugin

1. Create `src/main/kotlin/io/github/sanyavertolet/edukate/buildutils/<name>.gradle.kts`
2. Declare `package io.github.sanyavertolet.edukate.buildutils` at the top
3. If the plugin wraps an external Gradle plugin, add its jar to `dependencies {}` in `build.gradle.kts` (see below)
4. Apply in target modules: `id("io.github.sanyavertolet.edukate.buildutils.<name>")`

## Adding External Plugin Jar Dependencies

Plugins used inside convention plugins must be declared as `implementation` deps in `build.gradle.kts`.
**Use string literals** — the version catalog (`libs.versions.*`) is NOT available at plugin classpath
resolution time inside included builds:

```kotlin
// Correct
implementation("com.example:some-gradle-plugin:1.2.3")

// Will NOT compile inside gradle/plugins
implementation(libs.someGradlePlugin)
```

This is a known Gradle limitation — see the workaround already used for `libs.javaClass.superclass...`.

## Utilities

`BuildImageUtils.kt` — Docker image naming and registry credential helpers. Used by both
`spring-boot-app-configuration` and `frontend-image-build-configuration`. Reads credentials from:

| Source   | Gradle property      | Environment variable |
|:---------|:---------------------|:---------------------|
| Username | `do.docker.username` | `DO_DOCKER_USERNAME` |
| Password | `do.docker.password` | `DO_DOCKER_PASSWORD` |

Target registry: `registry.digitalocean.com/edukate-container-registry`.
