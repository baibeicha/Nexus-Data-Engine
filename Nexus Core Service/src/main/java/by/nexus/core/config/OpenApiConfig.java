package by.nexus.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) для документации API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus Data Engine API")
                        .version("1.0.0")
                        .description("API documentation for Nexus Data Engine - Hybrid Analytics Platform")
                        .contact(new Contact()
                                .name("Nexus Team")
                                .email("support@nexus-data-engine.com")
                                .url("https://github.com/baibeicha/Nexus-Data-Engine"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token")));
    }
}
