package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for retrieving authenticated user information
 */
public class SecurityUtil {

    /**
     * Get the currently authenticated user
     * @return UserPrincipal or null if not authenticated
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get the ID of the currently authenticated user
     * @return User ID or null if not authenticated
     */
    public static String getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get the role of the currently authenticated user
     * @return Role name or null if not authenticated
     */
    public static String getCurrentUserRole() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * Check if current user has Superadmin role
     * Checks for multiple possible role name variations
     */
    public static boolean isSuperadmin() {
        String role = getCurrentUserRole();
        if (role == null) return false;
        
        // Check for various possible superadmin role names
        String normalizedRole = role.toLowerCase().replace(" ", "").replace("_", "");
        return normalizedRole.equals("superadmin") || 
               normalizedRole.equals("super") ||
               normalizedRole.equals("admin") ||
               role.equalsIgnoreCase("Super Admin") ||
               role.equalsIgnoreCase("SuperAdmin") ||
               role.equalsIgnoreCase("SUPERADMIN");
    }

    /**
     * Check if current user has Accommodation Owner role
     */
    public static boolean isAccommodationOwner() {
        String role = getCurrentUserRole();
        return "Accommodation Owner".equals(role);
    }

    /**
     * Check if current user has Customer role
     */
    public static boolean isCustomer() {
        String role = getCurrentUserRole();
        return "Customer".equals(role);
    }

    /**
     * Get the JWT token from the current authentication context
     * @return JWT token string or null if not available
     */
    public static String getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }
}
