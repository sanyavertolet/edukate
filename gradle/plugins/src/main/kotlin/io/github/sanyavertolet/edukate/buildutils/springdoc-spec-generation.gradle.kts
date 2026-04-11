package io.github.sanyavertolet.edukate.buildutils

import org.springdoc.openapi.gradle.plugin.OpenApiExtension

apply(plugin = "org.springdoc.openapi-gradle-plugin")

configure<OpenApiExtension> {
    outputDir.set(rootProject.layout.projectDirectory.dir("spec"))
    outputFileName.set("openapi-${project.name}.yaml")
    waitTimeInSeconds.set(120)
    customBootRun {
        args.add("--spring.profiles.active=spec-gen")
    }
}

tasks.named("bootRun") {
    dependsOn(tasks.named("generateOpenApiDocs"))
}
