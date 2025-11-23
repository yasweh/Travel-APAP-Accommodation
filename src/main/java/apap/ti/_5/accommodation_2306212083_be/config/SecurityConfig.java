package apap.ti._5.accommodation_2306212083_be.config;

import apap.ti._5.accommodation_2306212083_be.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Accommodation Service
 * Integrates with Profile Service for JWT authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure session management as stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // ========== PUBLIC ENDPOINTS (No authentication required) ==========
                
                // Auth proxy endpoints - Public (login, register, logout)
                .requestMatchers("/api/auth/**").permitAll()
                
                // Property endpoints - Public read access
                .requestMatchers(HttpMethod.GET, "/api/property", "/api/property/*", "/api/property/*/room-types").permitAll()
                
                // Room type endpoints - Public read access
                .requestMatchers(HttpMethod.GET, "/api/room-types", "/api/room-types/*", "/api/roomtype/*").permitAll()
                
                // Booking endpoints - Public access for cascading dropdowns
                .requestMatchers(HttpMethod.GET, "/api/bookings/create", "/api/bookings/create/*",
                                "/api/bookings/roomtypes/*", "/api/bookings/rooms/*/*").permitAll()
                
                // Health check
                .requestMatchers("/actuator/health").permitAll()
                
                // ========== CUSTOMER ONLY ENDPOINTS ==========
                
                // Create booking - Customer only
                .requestMatchers(HttpMethod.POST, "/api/bookings/create").hasRole("CUSTOMER")
                
                // Update/Pay/Cancel/Refund booking - Customer only (ownership validated in controller)
                .requestMatchers(HttpMethod.GET, "/api/bookings/update/*").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/update/*").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/pay/*").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/cancel/*").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/refund/*").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/bookings/status/pay").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/bookings/status/cancel").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/bookings/status/refund").hasRole("CUSTOMER")
                
                // ========== SUPPORT TICKET ENDPOINTS ==========
                
                // Support tickets - Any authenticated user can create and view their tickets
                .requestMatchers(HttpMethod.POST, "/api/support-tickets").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/support-tickets", "/api/support-tickets/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/support-tickets/*/messages").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/support-tickets/*/messages").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/support-tickets/*/messages/mark-read").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/support-tickets/*/close").authenticated()
                
                // Admin support ticket endpoints - Superadmin only
                .requestMatchers("/api/admin/support-tickets/**").hasRole("SUPERADMIN")
                
                // ========== OWNER + SUPERADMIN ENDPOINTS ==========
                
                // Property management - Owner (own properties) + Superadmin (all)
                .requestMatchers(HttpMethod.GET, "/api/property/update/*").hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/property/create").hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/property/update").hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                .requestMatchers(HttpMethod.POST, "/api/property/updateroom").hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                
                // Maintenance management - Owner (own properties) + Superadmin (all)
                .requestMatchers(HttpMethod.POST, "/api/property/maintenance/add").hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/property/maintenance", "/api/property/maintenance/*",
                                "/api/property/maintenance/room-type/*", "/api/property/maintenance/room/*")
                    .hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                
                // Statistics/Charts - Owner (own properties) + Superadmin (all)
                .requestMatchers(HttpMethod.GET, "/api/bookings/chart").hasAnyRole("ACCOMMODATION_OWNER", "SUPERADMIN")
                
                // View bookings - Customer (own), Owner (own properties), Superadmin (all)
                .requestMatchers(HttpMethod.GET, "/api/bookings", "/api/bookings/*")
                    .hasAnyRole("CUSTOMER", "ACCOMMODATION_OWNER", "SUPERADMIN")
                
                // ========== SUPERADMIN ONLY ENDPOINTS ==========
                
                // Delete property - Superadmin only
                .requestMatchers(HttpMethod.DELETE, "/api/property/delete/*").hasRole("SUPERADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
