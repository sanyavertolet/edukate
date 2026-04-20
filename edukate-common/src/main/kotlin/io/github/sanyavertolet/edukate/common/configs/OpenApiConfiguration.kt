package io.github.sanyavertolet.edukate.common.configs

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "cookieAuth",
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.COOKIE,
    paramName = "X-Auth",
    description = "JWT authentication cookie",
)
class OpenApiConfiguration {
    @Bean
    @ConditionalOnProperty(name = ["gateway.url"])
    fun openAPI(@Value($$"${gateway.url}") gatewayUrl: String): OpenAPI =
        OpenAPI()
            .servers(listOf(Server().url(gatewayUrl)))
            .security(listOf(SecurityRequirement().addList("cookieAuth")))
            .info(
                Info()
                    .license(
                        License()
                            .name("GNU Affero General Public License v3.0")
                            .url("https://www.gnu.org/licenses/agpl-3.0.html")
                    )
            )
}
