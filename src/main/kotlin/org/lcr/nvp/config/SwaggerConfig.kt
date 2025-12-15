package org.lcr.nvp.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val jwtSchemeName = "bearerAuth"
        val securityRequirement = SecurityRequirement().addList(jwtSchemeName)
        val components = Components()
            .addSecuritySchemes(jwtSchemeName, SecurityScheme()
                .name(jwtSchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"))

        return OpenAPI()
            .info(apiInfo())
            .servers(listOf(
                Server().url("http://localhost:8080").description("Local"),
                Server().url("https://nvp.kr").description("Production"),
            ))
            .addSecurityItem(securityRequirement)
            .components(components)
    }

    private fun apiInfo() = Info()
        .title("NVP API Documentation")
        .description("NVP 프로젝트의 API 명세서입니다.")
        .version("1.0.0")
}
