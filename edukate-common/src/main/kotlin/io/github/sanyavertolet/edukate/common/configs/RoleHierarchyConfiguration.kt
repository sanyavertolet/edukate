package io.github.sanyavertolet.edukate.common.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl

@Configuration
class RoleHierarchyConfiguration {
     // todo: this does not seem to work at all
    @Bean fun roleHierarchy(): RoleHierarchy = RoleHierarchyImpl.withDefaultRolePrefix()
        .role("ADMIN").implies("MODERATOR")
        .role("MODERATOR").implies("USER")
        .build()
}
