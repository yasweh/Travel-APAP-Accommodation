package apap.ti._5.accommodation_2306212083_be.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI Configuration
 * 
 * Access Swagger UI at: /swagger-ui.html
 * Access OpenAPI JSON at: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Accommodation Service API")
                        .version("1.0.0")
                        .description("""
                                API documentation for Accommodation Back End (2306212083).
                                
                                ## Features:
                                - **Properties**: Manage accommodation properties
                                - **Room Types**: Manage room types within properties
                                - **Bookings**: Create and manage bookings
                                - **Reviews**: Customer reviews for accommodations
                                - **Maintenance**: Property maintenance management
                                - **Support Tickets**: Customer support system
                                - **Authentication**: JWT-based authentication via Profile Service
                                
                                ## Authentication:
                                Most endpoints require Bearer token authentication. 
                                Use the Authorize button to set your JWT token.
                                """)
                        .contact(new Contact()
                                .name("APAP Team")
                                .email("apap@cs.ui.ac.id")
                                .url("https://cs.ui.ac.id"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("http://2306212083-be.hafizmuh.site").description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from login")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * Group: All APIs - includes every endpoint
     */
    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("all")
                .displayName("All APIs")
                .pathsToMatch("/api/**")
                .build();
    }

    /**
     * Group: Property Management APIs
     */
    @Bean
    public GroupedOpenApi propertyApis() {
        return GroupedOpenApi.builder()
                .group("properties")
                .displayName("Property Management")
                .pathsToMatch("/api/properties/**")
                .build();
    }

    /**
     * Group: Booking APIs
     */
    @Bean
    public GroupedOpenApi bookingApis() {
        return GroupedOpenApi.builder()
                .group("bookings")
                .displayName("Bookings")
                .pathsToMatch("/api/bookings/**")
                .build();
    }

    /**
     * Group: Room Type APIs
     */
    @Bean
    public GroupedOpenApi roomTypeApis() {
        return GroupedOpenApi.builder()
                .group("room-types")
                .displayName("Room Types")
                .pathsToMatch("/api/room-types/**")
                .build();
    }

    /**
     * Group: Review APIs
     */
    @Bean
    public GroupedOpenApi reviewApis() {
        return GroupedOpenApi.builder()
                .group("reviews")
                .displayName("Reviews")
                .pathsToMatch("/api/reviews/**")
                .build();
    }

    /**
     * Group: Maintenance APIs
     */
    @Bean
    public GroupedOpenApi maintenanceApis() {
        return GroupedOpenApi.builder()
                .group("maintenance")
                .displayName("Maintenance")
                .pathsToMatch("/api/maintenance/**")
                .build();
    }

    /**
     * Group: Support APIs
     */
    @Bean
    public GroupedOpenApi supportApis() {
        return GroupedOpenApi.builder()
                .group("support")
                .displayName("Support & Tickets")
                .pathsToMatch("/api/support/**", "/api/support-tickets/**")
                .build();
    }

    /**
     * Group: Auth APIs
     */
    @Bean
    public GroupedOpenApi authApis() {
        return GroupedOpenApi.builder()
                .group("auth")
                .displayName("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }
}
