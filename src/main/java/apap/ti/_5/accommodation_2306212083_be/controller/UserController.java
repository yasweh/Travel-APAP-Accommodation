package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.dto.UserDetailDTO;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import apap.ti._5.accommodation_2306212083_be.service.UserService;
import apap.ti._5.accommodation_2306212083_be.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for user-related operations
 * Fetches user details from external user service
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    /**
     * GET /api/users/me - Get current authenticated user's profile
     * Fetches detailed info from external user service
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile() {
        try {
            UserPrincipal currentUser = SecurityUtil.getCurrentUser();
            log.info("Fetching profile for user: {}", currentUser.getUserId());
            
            UserDetailDTO userDetail = userService.getCurrentUserDetail(currentUser.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            
            if (userDetail != null) {
                response.put("success", true);
                response.put("data", userDetail);
                response.put("message", "User profile retrieved successfully");
                return ResponseEntity.ok(response);
            } else {
                // Fallback to JWT token data if external service fails
                log.warn("External user service unavailable, using JWT data");
                
                UserDetailDTO fallbackData = UserDetailDTO.builder()
                    .id(currentUser.getUserId())
                    .username(currentUser.getUsername())
                    .email(currentUser.getEmail())
                    .name(currentUser.getName())
                    .role(currentUser.getRole())
                    .build();
                
                response.put("success", true);
                response.put("data", fallbackData);
                response.put("message", "User profile retrieved from local auth (external service unavailable)");
                response.put("isLocalData", true);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to fetch user profile: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * GET /api/users/detail?search={id/username/email} - Search for a user
     * Available for authenticated users
     */
    @GetMapping("/detail")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserDetail(@RequestParam String search) {
        try {
            log.info("Searching for user: {}", search);
            
            UserDetailDTO userDetail = userService.getUserDetail(search);
            
            Map<String, Object> response = new HashMap<>();
            
            if (userDetail != null) {
                response.put("success", true);
                response.put("data", userDetail);
                response.put("message", "User found");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error searching for user: {}", e.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error searching for user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
