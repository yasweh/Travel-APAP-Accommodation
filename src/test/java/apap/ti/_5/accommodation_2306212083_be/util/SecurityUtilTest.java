package apap.ti._5.accommodation_2306212083_be.util;

import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {

    private UserPrincipal customerUser;
    private UserPrincipal ownerUser;
    private UserPrincipal superadminUser;

    @BeforeEach
    void setUp() {
        customerUser = UserPrincipal.builder()
                .userId("11111111-1111-1111-1111-111111111111")
                .username("customer")
                .email("customer@test.com")
                .name("Test Customer")
                .role("Customer")
                .build();

        ownerUser = UserPrincipal.builder()
                .userId("22222222-2222-2222-2222-222222222222")
                .username("owner")
                .email("owner@test.com")
                .name("Test Owner")
                .role("Accommodation Owner")
                .build();

        superadminUser = UserPrincipal.builder()
                .userId("33333333-3333-3333-3333-333333333333")
                .username("admin")
                .email("admin@test.com")
                .name("Test Admin")
                .role("Superadmin")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ReturnsUser() {
        setAuthentication(customerUser);

        UserPrincipal result = SecurityUtil.getCurrentUser();

        assertNotNull(result);
        assertEquals("customer", result.getUsername());
    }

    @Test
    void getCurrentUser_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        UserPrincipal result = SecurityUtil.getCurrentUser();

        assertNull(result);
    }

    @Test
    void getCurrentUser_WithNonUserPrincipal_ReturnsNull() {
        SecurityContextHolder.clearContext();
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken("stringPrincipal", null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserPrincipal result = SecurityUtil.getCurrentUser();

        assertNull(result);
    }

    @Test
    void getCurrentUserId_WithAuthenticatedUser_ReturnsUserId() {
        setAuthentication(customerUser);

        String result = SecurityUtil.getCurrentUserId();

        assertEquals("11111111-1111-1111-1111-111111111111", result);
    }

    @Test
    void getCurrentUserId_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = SecurityUtil.getCurrentUserId();

        assertNull(result);
    }

    @Test
    void getCurrentUserRole_WithAuthenticatedUser_ReturnsRole() {
        setAuthentication(ownerUser);

        String result = SecurityUtil.getCurrentUserRole();

        assertEquals("Accommodation Owner", result);
    }

    @Test
    void getCurrentUserRole_WithNoAuthentication_ReturnsNull() {
        SecurityContextHolder.clearContext();

        String result = SecurityUtil.getCurrentUserRole();

        assertNull(result);
    }

    @Test
    void isSuperadmin_WithSuperadminUser_ReturnsTrue() {
        setAuthentication(superadminUser);

        assertTrue(SecurityUtil.isSuperadmin());
    }

    @Test
    void isSuperadmin_WithNonSuperadminUser_ReturnsFalse() {
        setAuthentication(customerUser);

        assertFalse(SecurityUtil.isSuperadmin());
    }

    @Test
    void isSuperadmin_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(SecurityUtil.isSuperadmin());
    }

    @Test
    void isAccommodationOwner_WithOwnerUser_ReturnsTrue() {
        setAuthentication(ownerUser);

        assertTrue(SecurityUtil.isAccommodationOwner());
    }

    @Test
    void isAccommodationOwner_WithNonOwnerUser_ReturnsFalse() {
        setAuthentication(customerUser);

        assertFalse(SecurityUtil.isAccommodationOwner());
    }

    @Test
    void isAccommodationOwner_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(SecurityUtil.isAccommodationOwner());
    }

    @Test
    void isCustomer_WithCustomerUser_ReturnsTrue() {
        setAuthentication(customerUser);

        assertTrue(SecurityUtil.isCustomer());
    }

    @Test
    void isCustomer_WithNonCustomerUser_ReturnsFalse() {
        setAuthentication(superadminUser);

        assertFalse(SecurityUtil.isCustomer());
    }

    @Test
    void isCustomer_WithNoAuthentication_ReturnsFalse() {
        SecurityContextHolder.clearContext();

        assertFalse(SecurityUtil.isCustomer());
    }

    private void setAuthentication(UserPrincipal user) {
        UsernamePasswordAuthenticationToken auth = 
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
