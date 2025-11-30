package apap.ti._5.accommodation_2306212083_be.security;

import apap.ti._5.accommodation_2306212083_be.client.ProfileClient;
import apap.ti._5.accommodation_2306212083_be.dto.ProfileValidateResponse;
import apap.ti._5.accommodation_2306212083_be.dto.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private ProfileClient profileClient;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProfileValidateResponse validResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        validResponse = ProfileValidateResponse.builder()
                .valid(true)
                .userId("11111111-1111-1111-1111-111111111111")
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .role("Customer")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WithValidBearerToken_SetsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(profileClient.validateToken("valid-token")).thenReturn(validResponse);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("testuser", principal.getUsername());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidCookieToken_SetsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie jwtCookie = new Cookie("JWT_TOKEN", "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(profileClient.validateToken("valid-token")).thenReturn(validResponse);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoToken_ContinuesFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidToken_ContinuesFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(profileClient.validateToken("invalid-token")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidValidationResponse_ContinuesFilter() throws Exception {
        ProfileValidateResponse invalidResponse = ProfileValidateResponse.builder()
                .valid(false)
                .build();
        
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");
        when(profileClient.validateToken("some-token")).thenReturn(invalidResponse);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNonBearerHeader_ContinuesFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");
        when(request.getCookies()).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithEmptyCookieArray_ContinuesFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{});

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithOtherCookies_ContinuesFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie otherCookie = new Cookie("OTHER_COOKIE", "value");
        when(request.getCookies()).thenReturn(new Cookie[]{otherCookie});

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExceptionDuringValidation_ContinuesFilter() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(profileClient.validateToken(anyString())).thenThrow(new RuntimeException("Connection error"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNullRole_SetsDefaultRole() throws Exception {
        ProfileValidateResponse responseWithNullRole = ProfileValidateResponse.builder()
                .valid(true)
                .userId("11111111-1111-1111-1111-111111111111")
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .role(null)
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(profileClient.validateToken("valid-token")).thenReturn(responseWithNullRole);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("Customer", principal.getRole());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithSuperadminRole_SetsCorrectRole() throws Exception {
        ProfileValidateResponse adminResponse = ProfileValidateResponse.builder()
                .valid(true)
                .userId("11111111-1111-1111-1111-111111111111")
                .username("admin")
                .email("admin@test.com")
                .name("Admin User")
                .role("Superadmin")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(profileClient.validateToken("valid-token")).thenReturn(adminResponse);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("Superadmin", principal.getRole());
    }

    @Test
    void doFilterInternal_WithAccommodationOwnerRole_SetsCorrectRole() throws Exception {
        ProfileValidateResponse ownerResponse = ProfileValidateResponse.builder()
                .valid(true)
                .userId("22222222-2222-2222-2222-222222222222")
                .username("owner")
                .email("owner@test.com")
                .name("Owner User")
                .role("Accommodation Owner")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(profileClient.validateToken("valid-token")).thenReturn(ownerResponse);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        assertEquals("Accommodation Owner", principal.getRole());
    }
}
