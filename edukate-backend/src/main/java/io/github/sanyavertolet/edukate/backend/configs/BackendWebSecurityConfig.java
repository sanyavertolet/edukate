package io.github.sanyavertolet.edukate.backend.configs;

import io.github.sanyavertolet.edukate.auth.configs.WebSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Profile("secure")
@Import(WebSecurityConfig.class)
public class BackendWebSecurityConfig { }
