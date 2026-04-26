package io.github.sanyavertolet.edukate.buildutils

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.detekt")
    id("com.ncorti.ktfmt.gradle")
    jacoco
}

detekt {
    config.setFrom("${rootProject.projectDir}/detekt.yml")
    // buildUponDefaultConfig = true: start from detekt defaults, then apply our yml overrides.
    // This ensures rules added in future detekt versions are auto-enabled (with their defaults)
    // rather than silently ignored, keeping the zero-tolerance policy future-proof.
    buildUponDefaultConfig = true
    allRules = false
    // Zero-tolerance: fail on any warning or error (replaces maxIssues: 0 from detekt 1.x)
    failOnSeverity = dev.detekt.gradle.extensions.FailOnSeverity.Warning
}

// Configure SARIF output on tasks directly (the detekt extension reports DSL is deprecated).
// SARIF output enables inline PR annotations via github/codeql-action/upload-sarif.
tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    reports.sarif.required.set(true)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

ktfmt {
    kotlinLangStyle()
    // Mirrors .idea/ktfmt.xml: Custom style, 125-char width, 4-space indent, trailing commas.
    // These values must stay in sync with detekt.yml style.MaxLineLength.
    maxWidth.set(125)
    blockIndent.set(4)
    continuationIndent.set(4)
    manageTrailingCommas.set(true)
    removeUnusedImports.set(true)
}

// Automatically generate the JaCoCo XML + HTML report after every test run.
tasks.withType<Test>().configureEach {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.withType<Test>())
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        classDirectories.files.map { dir ->
            fileTree(dir) { exclude("**/configs/**") }
        }
    )
}
