package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserTest {

    private UserPrincipal customerUser;
    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        
        customerUser = UserPrincipal.builder()
                .userId("11111111-1111-1111-1111-111111111111")
                .username("customer")
                .email("customer@test.com")
                .name("Test Customer")
                .role("CUSTOMER")
                .build();

        ownerUser = UserPrincipal.builder()
                .userId("22222222-2222-2222-2222-222222222222")
                .username("owner")
                .email("owner@test.com")
                .name("Test Owner")
                .role("ACCOMMODATION_OWNER")
                .build();

        superadminUser = UserPrincipal.builder()
                .userId("33333333-3333-3333-3333-333333333333")
                .username("admin")
                .email("admin@test.com")
                .name("Test Admin")
                .role("SUPERADMIN")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void get_WithAuthenticatedUser_ReturnsUser() {
        setAuthentication(customerUser);

        UserPrincipal result = CurrentUser.get();

        assertNotNull(result);
        assertEquals("customer", result.getUsername());
    }

    @Test
    void get_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        UserPrincipal result = CurrentUser.get();

        assertNull(result);
    }

    @Test
    void get_WithNonUserPrincipal_ReturnsNull() {
        SecurityContextHolder.clearContext();
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("stringPrincipal", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserPrincipal result = CurrentUser.get();

        assertNull(result);
    }

    @Test
    void getUsername_WithAuthenticatedUser_ReturnsUsername() {
        setAuthentication(customerUser);

        String result = CurrentUser.getUsername();

        assertEquals("customer", result);
    }

    @Test
    void getUsername_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = CurrentUser.getUsername();

        assertNull(result);
    }

    @Test
    void getUserId_WithAuthenticatedUser_ReturnsUserId() {
        setAuthentication(customerUser);

        String result = CurrentUser.getUserId();

        assertEquals("11111111-1111-1111-1111-111111111111", result);
    }

    @Test
    void getUserId_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = CurrentUser.getUserId();

        assertNull(result);
    }

    @Test
    void getEmail_WithAuthenticatedUser_ReturnsEmail() {
        setAuthentication(customerUser);

        String result = CurrentUser.getEmail();

        assertEquals("customer@test.com", result);
    }

    @Test
    void getEmail_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = CurrentUser.getEmail();

        assertNull(result);
    }

    @Test
    void getName_WithAuthenticatedUser_ReturnsName() {
        setAuthentication(customerUser);

        String result = CurrentUser.getName();

        assertEquals("Test Customer", result);
    }

    @Test
    void getName_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = CurrentUser.getName();

        assertNull(result);
    }

    @Test
    void getRole_WithAuthenticatedUser_ReturnsRole() {
        setAuthentication(ownerUser);

        String result = CurrentUser.getRole();

        assertEquals("ACCOMMODATION_OWNER", result);
    }

    @Test
    void getRole_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = CurrentUser.getRole();

        assertNull(result);
    }

    @Test
    void isAuthenticated_WithUser_ReturnsTrue() {
        setAuthentication(customerUser);

        assertTrue(CurrentUser.isAuthenticated());
    }

    @Test
    void isAuthenticated_WithNoUser_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(CurrentUser.isAuthenticated());
    }

    @Test
    void isSuperadmin_WithSuperadminUser_ReturnsTrue() {
        setAuthentication(superadminUser);

        assertTrue(CurrentUser.isSuperadmin());
    }

    @Test
    void isSuperadmin_WithNonSuperadminUser_ReturnsFalse() {
        setAuthentication(customerUser);

        assertFalse(CurrentUser.isSuperadmin());
    }

    @Test
    void isSuperadmin_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(CurrentUser.isSuperadmin());
    }

    @Test
    void isOwner_WithOwnerUser_ReturnsTrue() {
        setAuthentication(ownerUser);

        assertTrue(CurrentUser.isOwner());
    }

    @Test
    void isOwner_WithSuperadminUser_ReturnsTrue() {
        setAuthentication(superadminUser);

        assertTrue(CurrentUser.isOwner());
    }

    @Test
    void isOwner_WithCustomerUser_ReturnsFalse() {
        setAuthentication(customerUser);

        assertFalse(CurrentUser.isOwner());
    }

    @Test
    void isOwner_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(CurrentUser.isOwner());
    }

    @Test
    void isCustomer_WithCustomerUser_ReturnsTrue() {
        setAuthentication(customerUser);

        assertTrue(CurrentUser.isCustomer());
    }

    @Test
    void isCustomer_WithNonCustomerUser_ReturnsFalse() {
        setAuthentication(superadminUser);

        assertFalse(CurrentUser.isCustomer());
    }

    @Test
    void isCustomer_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(CurrentUser.isCustomer());
    }

    @Test
    void hasRole_WithMatchingRole_ReturnsTrue() {
        setAuthentication(customerUser);

        assertTrue(CurrentUser.hasRole("CUSTOMER"));
    }

    @Test
    void hasRole_WithNonMatchingRole_ReturnsFalse() {
        setAuthentication(customerUser);

        assertFalse(CurrentUser.hasRole("SUPERADMIN"));
    }

    @Test
    void hasRole_WithNullRole_ReturnsFalse() {
        setAuthentication(customerUser);

        assertFalse(CurrentUser.hasRole(null));
    }

    @Test
    void hasRole_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(CurrentUser.hasRole("CUSTOMER"));
    }

    @Test
    void getOrThrow_WithAuthenticatedUser_ReturnsUser() {
        setAuthentication(customerUser);

        UserPrincipal result = CurrentUser.getOrThrow();

        assertNotNull(result);
        assertEquals("customer", result.getUsername());
    }

    @Test
    void getOrThrow_WithNoAuthentication_ThrowsException() {
        SecurityContextHolder.clearContext();

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> CurrentUser.getOrThrow()
        );
        
        assertEquals("User not authenticated", exception.getMessage());
    }

    private void setAuthentication(UserPrincipal user) {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
