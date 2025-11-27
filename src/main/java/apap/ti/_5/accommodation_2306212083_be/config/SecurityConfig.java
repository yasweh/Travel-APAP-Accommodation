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

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security Configuration for Accommodation Service
 * Integrates with Profile Service for JWT authentication
 * 
 * ============================================================
 * TEMPORARILY DISABLED - SECURITY & RBAC COMMENTED OUT
 * Fokus development fitur support ticket dulu
 * Nanti uncomment untuk mengaktifkan kembali security
 * ============================================================
 */
@Configuration
@EnableWebSecurity
// @EnableMethodSecurity(prePostEnabled = true) // COMMENTED - Disable method security
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ============================================================
        // TEMPORARILY DISABLED - ALL ENDPOINTS ARE PUBLIC
        // Uncomment bagian bawah untuk restore RBAC
        // ============================================================
        http
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure session management as stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // TEMPORARILY: Allow all requests without authentication
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
            
        // COMMENTED - JWT filter temporarily disabled
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    /* ============================================================
     * ORIGINAL SECURITY CONFIGURATION - COMMENTED OUT
     * Uncomment untuk restore RBAC
     * ============================================================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
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
                
                // Create booking - Customer and Superadmin
                .requestMatchers(HttpMethod.POST, "/api/bookings/create").hasAnyAuthority("Customer", "Superadmin")
                
                // Update/Pay/Cancel/Refund booking - Customer and Superadmin
                .requestMatchers(HttpMethod.GET, "/api/bookings/update/*").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/update/*").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/pay/*").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/cancel/*").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/refund/*").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.POST, "/api/bookings/status/pay").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.POST, "/api/bookings/status/cancel").hasAnyAuthority("Customer", "Superadmin")
                .requestMatchers(HttpMethod.POST, "/api/bookings/status/refund").hasAnyAuthority("Customer", "Superadmin")
                
                // ========== SUPPORT TICKET ENDPOINTS ==========
                
                // Support tickets - Any authenticated user can create and view their tickets
                .requestMatchers(HttpMethod.POST, "/api/support-tickets").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/support-tickets", "/api/support-tickets/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/support-tickets/*/messages").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/support-tickets/*/messages").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/support-tickets/*/messages/mark-read").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/support-tickets/*/close").authenticated()
                
                // Admin support ticket endpoints - Superadmin only
                .requestMatchers("/api/admin/support-tickets/**").hasAuthority("Superadmin")
                
                // ========== OWNER + SUPERADMIN ENDPOINTS ==========
                
                // Property management - Owner (own properties) + Superadmin (all)
                .requestMatchers(HttpMethod.GET, "/api/property/update/*").hasAnyAuthority("Accommodation Owner", "Superadmin")
                .requestMatchers(HttpMethod.POST, "/api/property/create").hasAnyAuthority("Accommodation Owner", "Superadmin")
                .requestMatchers(HttpMethod.PUT, "/api/property/update").hasAnyAuthority("Accommodation Owner", "Superadmin")
                .requestMatchers(HttpMethod.POST, "/api/property/updateroom").hasAnyAuthority("Accommodation Owner", "Superadmin")
                
                // Maintenance management - Owner (own properties) + Superadmin (all)
                .requestMatchers(HttpMethod.POST, "/api/property/maintenance/add").hasAnyAuthority("Accommodation Owner", "Superadmin")
                .requestMatchers(HttpMethod.GET, "/api/property/maintenance", "/api/property/maintenance/*",
                                "/api/property/maintenance/room-type/*", "/api/property/maintenance/room/*")
                    .hasAnyAuthority("Accommodation Owner", "Superadmin")
                
                // Statistics/Charts - Owner (own properties) + Superadmin (all)
                .requestMatchers(HttpMethod.GET, "/api/bookings/chart").hasAnyAuthority("Accommodation Owner", "Superadmin")
                
                // View bookings - Customer (own), Owner (own properties), Superadmin (all)
                .requestMatchers(HttpMethod.GET, "/api/bookings", "/api/bookings/*")
                    .hasAnyAuthority("Customer", "Accommodation Owner", "Superadmin")
                
                // ========== SUPERADMIN ONLY ENDPOINTS ==========
                
                // Delete property - Superadmin and Accommodation Owner (own property)
                .requestMatchers(HttpMethod.DELETE, "/api/property/delete/*").hasAnyAuthority("Superadmin", "Accommodation Owner")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    */
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:5173", 
            "https://2306212083-fe.hafizmuh.site"
        )); // Allow Frontend URLs
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Important for Cookies

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
