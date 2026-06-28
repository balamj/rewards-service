package com.retail.rewards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Core Application Configuration Layer.
 * @Configuration Declares that this class provides Spring Boot Bean factory definitions.
 * @EnableAsync Enables Spring's background multi-threaded task execution capabilities.
 */
@Configuration
@EnableAsync
public class AppConfig {
    /**
     * Configures and registers a custom OpenAPI bean for dynamic Swagger UI documentation generation.
     * This provides interactive UI documentation illustrating the system runtime topology
     * and HTTP API endpoints for development integration testing.
     * @return A configured OpenAPI meta-data specification object.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("Customer Rewards API").version("1.0")
                .description("Rewards program based on each recorded purchase\n\n" +
                        "**Endpoint:** GET '/api/rewards'\n\n" +
                        "**Endpoint:** GET '/api/rewards/{customerId}'"));
    }
}