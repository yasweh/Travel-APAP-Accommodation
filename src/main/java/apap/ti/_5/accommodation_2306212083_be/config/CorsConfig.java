package apap.ti._5.accommodation_2306212083_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for Frontend Integration
 * Allows Vue.js frontend to call backend REST API
 * Supports both local development and production environments via CORS_ALLOWED_ORIGINS env variable
 * Includes Profile Service origin for authentication integration
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed.origins:http://localhost:5173,http://127.0.0.1:5173,https://2306219575-fe.hafizmuh.site,https://2306219575-be.hafizmuh.site}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        
        System.out.println("CORS Configuration - Allowed Origins:");
        for (String origin : origins) {
            System.out.println("  - " + origin.trim());
        }
        
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Authorization", "Content-Type")
                .maxAge(3600);
    }
}
