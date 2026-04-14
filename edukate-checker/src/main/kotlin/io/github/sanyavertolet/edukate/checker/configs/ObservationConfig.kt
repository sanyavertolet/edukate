package io.github.sanyavertolet.edukate.checker.configs

import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObservationConfig {
    /**
     * Activates @Observed annotation processing via AOP.
     *
     * Spring Boot auto-configures ObservationRegistry but does NOT register ObservedAspect. Without this bean, all Observed
     * annotations are silently ignored — no spans, no metrics.
     */
    @Bean fun observedAspect(observationRegistry: ObservationRegistry): ObservedAspect = ObservedAspect(observationRegistry)
}
