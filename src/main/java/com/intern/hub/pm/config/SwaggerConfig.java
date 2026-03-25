package com.intern.hub.pm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${gateway-url:http://localhost:8765}")
    private String gatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Quản Lý Ví Của User Intern Hub")
                        .description("Tài liệu API cho nghiệp vụ ví và blockchain"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"));
    }

    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> openApi
                .addServersItem(new Server().url(gatewayUrl + "/api"))
                .components((
                        openApi.getComponents() == null ? new Components() : openApi.getComponents())
                        .addSecuritySchemes(
                                "Bearer",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSecuritySchemes(
                                "InternalAPIKey",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-Internal-Secret"))
                );
    }
}
