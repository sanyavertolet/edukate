package io.github.sanyavertolet.edukate.backend.configs;

import io.github.sanyavertolet.edukate.auth.configs.NoopWebSecurityConfig;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@EnableWebFluxSecurity
@Profile("!secure")
@Import(NoopWebSecurityConfig.class)
public class BackendNoopWebSecurityConfig { }
