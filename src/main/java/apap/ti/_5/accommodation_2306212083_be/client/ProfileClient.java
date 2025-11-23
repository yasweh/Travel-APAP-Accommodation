package apap.ti._5.accommodation_2306212083_be.client;

import apap.ti._5.accommodation_2306212083_be.dto.TokenValidationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for Profile Service (https://2306219575-be.hafizmuh.site)
 * Handles authentication operations: login, register, token validation
 */
@Component
@RequiredArgsConstructor
public class ProfileClient {

    private static final Logger logger = LoggerFactory.getLogger(ProfileClient.class);

    @Value("${profile.service.base-url:https://2306219575-be.hafizmuh.site}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Validate JWT token with Profile Service
     * 
     * @param token JWT token to validate
     * @return TokenValidationResponse with user details
     */
    public TokenValidationResponse validateToken(String token) {
        try {
            String url = baseUrl + "/api/auth/validate-token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cookie", "JWT_TOKEN=" + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                TokenValidationResponse validationResponse = objectMapper.readValue(
                        response.getBody(), 
                        TokenValidationResponse.class
                );
                
                logger.info("Token validation successful for user: {}", 
                        validationResponse.getData() != null ? validationResponse.getData().getUsername() : "unknown");
                
                return validationResponse;
            }
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Login to Profile Service
     * 
     * @param loginRequest Map containing username and password
     * @return Response with JWT token
     */
    public Map<String, Object> login(Map<String, String> loginRequest) {
        try {
            String url = baseUrl + "/api/auth/login";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                
                // Extract JWT token from Set-Cookie header
                HttpHeaders responseHeaders = response.getHeaders();
                if (responseHeaders.containsKey(HttpHeaders.SET_COOKIE)) {
                    result.put("cookie", responseHeaders.getFirst(HttpHeaders.SET_COOKIE));
                }
                
                logger.info("Login successful for user: {}", loginRequest.get("username"));
                return result;
            }
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Login failed");
        return errorResponse;
    }

    /**
     * Register new user to Profile Service
     * 
     * @param registerPayload Map containing registration details
     * @return Response with registration status
     */
    public Map<String, Object> register(Map<String, Object> registerPayload) {
        try {
            String url = baseUrl + "/api/auth/register";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(registerPayload, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);
                
                logger.info("Registration successful for user: {}", registerPayload.get("username"));
                return result;
            }
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
        }
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Registration failed");
        return errorResponse;
    }

    /**
     * Get current user info from Profile Service
     * 
     * @param token JWT token
     * @return User information
     */
    public Map<String, Object> getCurrentUser(String token) {
        TokenValidationResponse validationResponse = validateToken(token);
        
        if (validationResponse != null && validationResponse.getData() != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", validationResponse.getData().getUserId());
            userInfo.put("username", validationResponse.getData().getUsername());
            userInfo.put("email", validationResponse.getData().getEmail());
            userInfo.put("name", validationResponse.getData().getName());
            userInfo.put("role", validationResponse.getData().getRole());
            userInfo.put("valid", validationResponse.getData().getValid());
            return userInfo;
        }
        
        return null;
    }
}
