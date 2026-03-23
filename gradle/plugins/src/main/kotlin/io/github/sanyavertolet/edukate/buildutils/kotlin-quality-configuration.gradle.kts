package io.github.sanyavertolet.edukate.buildutils

plugins {
    id("dev.detekt")
    id("com.ncorti.ktfmt.gradle")
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

ktfmt {
    // Mirrors .idea/ktfmt.xml: Custom style, 125-char width, 4-space indent, trailing commas.
    // These values must stay in sync with detekt.yml style.MaxLineLength.
    maxWidth.set(125)
    blockIndent.set(4)
    continuationIndent.set(4)
    manageTrailingCommas.set(true)
    removeUnusedImports.set(true)
}
