package apap.ti._5.accommodation_2306212083_be.service;

import apap.ti._5.accommodation_2306212083_be.dto.UserDetailApiResponse;
import apap.ti._5.accommodation_2306212083_be.dto.UserDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for fetching user details from external user service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final RestTemplate restTemplate;
    
    private static final String USER_SERVICE_BASE_URL = "http://2306219575-be.hafizmuh.site/api/users";
    
    /**
     * Get JWT token from current authentication context
     */
    private String getAuthToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            return authentication.getCredentials().toString();
        }
        return null;
    }
    
    /**
     * Create HTTP headers with JWT authorization
     */
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String token = getAuthToken();
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
            log.debug("Token added to user request: {}...", token.substring(0, Math.min(20, token.length())));
        } else {
            log.warn("No JWT token found for user detail request");
        }
        
        return headers;
    }
    
    /**
     * Fetch user detail from external user service by search parameter (userId, username, or email)
     * 
     * @param search User ID, username, or email to search
     * @return UserDetailDTO or null if not found
     */
    public UserDetailDTO getUserDetail(String search) {
        try {
            log.info("Fetching user detail for search: {}", search);
            
            HttpHeaders headers = createAuthHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            String url = USER_SERVICE_BASE_URL + "/detail?search=" + search;
            log.debug("Calling user service URL: {}", url);
            
            ResponseEntity<UserDetailApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserDetailApiResponse.class
            );
            
            log.info("User service response status: {}", response.getStatusCode());
            
            UserDetailApiResponse apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.getData() != null) {
                log.info("Successfully fetched user detail for: {}", search);
                return apiResponse.getData();
            }
            
            log.warn("User service returned null data for: {}", search);
            return null;
            
        } catch (Exception e) {
            log.error("Error fetching user detail for {}: {}", search, e.getMessage());
            return null;
        }
    }
    
    /**
     * Fetch current authenticated user's detail
     * Uses the userId from the JWT token
     * 
     * @param userId Current user's ID
     * @return UserDetailDTO or null if not found
     */
    public UserDetailDTO getCurrentUserDetail(String userId) {
        return getUserDetail(userId);
    }
}
