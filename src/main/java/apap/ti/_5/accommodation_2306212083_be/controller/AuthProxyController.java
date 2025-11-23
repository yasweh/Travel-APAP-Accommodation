package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.client.ProfileClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Auth Proxy Controller
 * Proxies authentication requests to Profile Service
 * Handles login, register, and logout operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthProxyController {

    private final ProfileClient profileClient;

    /**
     * POST /api/auth/login - Login via Profile Service
     * 
     * @param loginRequest Map containing username and password
     * @param response HttpServletResponse to set JWT cookie
     * @return Login response with user details
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> loginRequest,
            HttpServletResponse response) {
        
        Map<String, Object> loginResponse = profileClient.login(loginRequest);
        
        // Extract JWT token from response and set as cookie
        if (loginResponse.containsKey("cookie")) {
            String cookieHeader = (String) loginResponse.get("cookie");
            
            // Parse Set-Cookie header to extract token value
            if (cookieHeader != null && cookieHeader.contains("JWT_TOKEN=")) {
                String token = extractTokenFromCookie(cookieHeader);
                
                // Set JWT token as cookie
                Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                // jwtCookie.setSecure(true); // Enable in production with HTTPS
                
                response.addCookie(jwtCookie);
            }
            
            // Remove cookie from response body
            loginResponse.remove("cookie");
        }
        
        if (loginResponse.containsKey("success") && Boolean.TRUE.equals(loginResponse.get("success"))) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse);
        }
    }

    /**
     * POST /api/auth/register - Register via Profile Service
     * 
     * @param registerPayload Map containing registration details
     * @return Registration response
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> registerPayload) {
        Map<String, Object> registerResponse = profileClient.register(registerPayload);
        
        if (registerResponse.containsKey("success") && Boolean.TRUE.equals(registerResponse.get("success"))) {
            return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerResponse);
        }
    }

    /**
     * POST /api/auth/logout - Logout by clearing JWT cookie
     * 
     * @param response HttpServletResponse to clear JWT cookie
     * @return Logout response
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        // Clear JWT token cookie
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        
        response.addCookie(jwtCookie);
        
        Map<String, Object> logoutResponse = new HashMap<>();
        logoutResponse.put("success", true);
        logoutResponse.put("message", "Logged out successfully");
        
        return ResponseEntity.ok(logoutResponse);
    }

    /**
     * GET /api/auth/me - Get current user info
     * 
     * @param jwtToken JWT token from cookie
     * @return Current user information
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @CookieValue(value = "JWT_TOKEN", required = false) String jwtToken) {
        
        if (jwtToken == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        Map<String, Object> userInfo = profileClient.getCurrentUser(jwtToken);
        
        if (userInfo != null) {
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("data", userInfo);
            return ResponseEntity.ok(successResponse);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Helper method to extract token value from Set-Cookie header
     */
    private String extractTokenFromCookie(String cookieHeader) {
        // Cookie format: JWT_TOKEN=<token>; Path=/; HttpOnly; Max-Age=...
        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("JWT_TOKEN=")) {
                return part.trim().substring("JWT_TOKEN=".length());
            }
        }
        return "";
    }
}
