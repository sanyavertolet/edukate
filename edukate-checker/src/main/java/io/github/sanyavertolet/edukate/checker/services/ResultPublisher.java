package io.github.sanyavertolet.edukate.checker.services;

import io.github.sanyavertolet.edukate.common.checks.CheckResult;
import reactor.core.publisher.Mono;

public interface ResultPublisher {
    Mono<Void> publish(CheckResult result);
}
