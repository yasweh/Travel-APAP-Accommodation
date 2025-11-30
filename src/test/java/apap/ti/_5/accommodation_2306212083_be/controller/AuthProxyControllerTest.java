package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.client.ProfileClient;
import apap.ti._5.accommodation_2306212083_be.dto.LoginRequest;
import apap.ti._5.accommodation_2306212083_be.dto.LoginResponse;
import apap.ti._5.accommodation_2306212083_be.dto.ProfileValidateResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthProxyControllerTest {

    @Mock
    private ProfileClient profileClient;

    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthProxyController authProxyController;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private ProfileValidateResponse validateResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");

        loginResponse = new LoginResponse();
        loginResponse.setToken("jwt-token-123");
        loginResponse.setRole("Customer");

        validateResponse = ProfileValidateResponse.builder()
                .valid(true)
                .userId("11111111-1111-1111-1111-111111111111")
                .username("testuser")
                .email("test@test.com")
                .name("Test User")
                .role("Customer")
                .build();
    }

    @Test
    void login_Success() {
        when(profileClient.login(any(LoginRequest.class))).thenReturn(loginResponse);

        ResponseEntity<Map<String, Object>> response = authProxyController.login(loginRequest, httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Login successful", response.getBody().get("message"));
        
        // Verify cookie was set
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("JWT_TOKEN", cookie.getName());
        assertEquals("jwt-token-123", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void login_InvalidCredentials() {
        when(profileClient.login(any(LoginRequest.class))).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = authProxyController.login(loginRequest, httpServletResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("Invalid email or password", response.getBody().get("message"));
    }

    @Test
    void login_ResponseWithNullToken() {
        LoginResponse noTokenResponse = new LoginResponse();
        noTokenResponse.setToken(null);
        when(profileClient.login(any(LoginRequest.class))).thenReturn(noTokenResponse);

        ResponseEntity<Map<String, Object>> response = authProxyController.login(loginRequest, httpServletResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    void login_Exception() {
        when(profileClient.login(any(LoginRequest.class))).thenThrow(new RuntimeException("Connection error"));

        ResponseEntity<Map<String, Object>> response = authProxyController.login(loginRequest, httpServletResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertEquals("An error occurred during login. Please try again.", response.getBody().get("message"));
    }

    @Test
    void register_Success() {
        Object registerPayload = Map.of("email", "new@test.com", "password", "pass123");
        Object expectedResponse = Map.of("success", true);
        
        when(profileClient.register(any())).thenReturn(expectedResponse);

        ResponseEntity<Object> response = authProxyController.register(registerPayload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void logout_Success() {
        ResponseEntity<Map<String, Object>> response = authProxyController.logout(httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Logged out successfully", response.getBody().get("message"));

        // Verify cookie was cleared
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpServletResponse).addCookie(cookieCaptor.capture());
        
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("JWT_TOKEN", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    void getCurrentUser_WithValidToken_Success() {
        when(profileClient.validateToken("valid-token")).thenReturn(validateResponse);

        ResponseEntity<ProfileValidateResponse> response = authProxyController.getCurrentUser("valid-token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void getCurrentUser_WithNullToken_Unauthorized() {
        ResponseEntity<ProfileValidateResponse> response = authProxyController.getCurrentUser(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getCurrentUser_WithInvalidToken_Unauthorized() {
        when(profileClient.validateToken("invalid-token")).thenReturn(null);

        ResponseEntity<ProfileValidateResponse> response = authProxyController.getCurrentUser("invalid-token");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
