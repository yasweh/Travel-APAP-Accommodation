package apap.ti._5.accommodation_2306212083_be.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation for SecurityContext
 * Contains user information from Profile Service
 */
@Data
@Builder
public class UserPrincipal implements UserDetails {
    
    private String userId;
    private String username;
    private String email;
    private String name;
    private String role; // Superadmin, Accommodation Owner, Customer
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the role name directly as the authority, without adding "ROLE_" prefix
        // This ensures we match exactly what is in the database/external service
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
    
    @Override
    public String getPassword() {
        return null; // No password needed for token-based auth
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
