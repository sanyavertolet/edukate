package io.github.sanyavertolet.edukate.buildutils

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

tasks.named<BootBuildImage>("bootBuildImage") {
    commonConfigure()
}
