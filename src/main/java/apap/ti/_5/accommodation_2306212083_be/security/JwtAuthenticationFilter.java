package apap.ti._5.accommodation_2306212083_be.security;

import apap.ti._5.accommodation_2306212083_be.client.ProfileClient;
import apap.ti._5.accommodation_2306212083_be.dto.TokenValidationResponse;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Extracts JWT token from Authorization header or cookie and validates with Profile Service
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String JWT_TOKEN_COOKIE_NAME = "JWT_TOKEN";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ProfileClient profileClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract JWT token from header or cookie
            String token = extractToken(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Validate token with Profile Service using ProfileClient
                TokenValidationResponse validationResponse = profileClient.validateToken(token);

                if (validationResponse != null && 
                    validationResponse.getData() != null && 
                    Boolean.TRUE.equals(validationResponse.getData().getValid())) {

                    // Map Profile Service role to Accommodation Service role
                    String mappedRole = mapRole(validationResponse.getData().getRole());

                    // Create UserPrincipal
                    UserPrincipal userPrincipal = UserPrincipal.builder()
                            .userId(validationResponse.getData().getUserId())
                            .username(validationResponse.getData().getUsername())
                            .email(validationResponse.getData().getEmail())
                            .name(validationResponse.getData().getName())
                            .role(mappedRole)
                            .build();

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to authenticate user: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header or cookie
     * Priority: 1) Bearer header, 2) Cookie
     */
    private String extractToken(HttpServletRequest request) {
        // Check Authorization header first
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        // Check cookie as fallback
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Map Profile Service roles to Accommodation Service roles
     * ADMIN -> SUPERADMIN
     * TRAVEL_AGENT -> ACCOMMODATION_OWNER
     * CUSTOMER -> CUSTOMER
     */
    private String mapRole(String profileServiceRole) {
        if (profileServiceRole == null) {
            return "CUSTOMER";
        }
        
        return switch (profileServiceRole.toUpperCase()) {
            case "ADMIN" -> "SUPERADMIN";
            case "TRAVEL_AGENT" -> "ACCOMMODATION_OWNER";
            default -> "CUSTOMER";
        };
    }
}
