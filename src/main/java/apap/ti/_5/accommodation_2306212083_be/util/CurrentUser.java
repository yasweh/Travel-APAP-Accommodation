package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class to access current authenticated user information
 * Similar to Flight Service's CurrentUser utility
 */
public class CurrentUser {

    /**
     * Get current authenticated user
     * 
     * @return UserPrincipal or null if not authenticated
     */
    public static UserPrincipal get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get username of current user
     * 
     * @return Username or null
     */
    public static String getUsername() {
        UserPrincipal user = get();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Get user ID of current user
     * 
     * @return User ID or null
     */
    public static String getUserId() {
        UserPrincipal user = get();
        return user != null ? user.getUserId() : null;
    }

    /**
     * Get email of current user
     * 
     * @return Email or null
     */
    public static String getEmail() {
        UserPrincipal user = get();
        return user != null ? user.getEmail() : null;
    }

    /**
     * Get name of current user
     * 
     * @return Name or null
     */
    public static String getName() {
        UserPrincipal user = get();
        return user != null ? user.getName() : null;
    }

    /**
     * Get role of current user
     * 
     * @return Role (SUPERADMIN, ACCOMMODATION_OWNER, CUSTOMER) or null
     */
    public static String getRole() {
        UserPrincipal user = get();
        return user != null ? user.getRole() : null;
    }

    /**
     * Check if user is authenticated
     * 
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return get() != null;
    }

    /**
     * Check if current user is Superadmin
     * 
     * @return true if Superadmin, false otherwise
     */
    public static boolean isSuperadmin() {
        String role = getRole();
        return "SUPERADMIN".equals(role);
    }

    /**
     * Check if current user is Owner
     * 
     * @return true if Owner or Superadmin, false otherwise
     */
    public static boolean isOwner() {
        String role = getRole();
        return "ACCOMMODATION_OWNER".equals(role) || "SUPERADMIN".equals(role);
    }

    /**
     * Check if current user is Customer
     * 
     * @return true if Customer, false otherwise
     */
    public static boolean isCustomer() {
        String role = getRole();
        return "CUSTOMER".equals(role);
    }

    /**
     * Check if current user has specific role
     * 
     * @param targetRole Role to check
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String targetRole) {
        String role = getRole();
        return targetRole != null && targetRole.equals(role);
    }

    /**
     * Get full UserPrincipal object or throw exception
     * 
     * @return UserPrincipal
     * @throws IllegalStateException if not authenticated
     */
    public static UserPrincipal getOrThrow() {
        UserPrincipal user = get();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return user;
    }
}
