package apap.ti._5.accommodation_2306212083_be.client;

import apap.ti._5.accommodation_2306212083_be.dto.LoginRequest;
import apap.ti._5.accommodation_2306212083_be.dto.LoginResponse;
import apap.ti._5.accommodation_2306212083_be.dto.ProfileValidateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProfileClient
 * These tests verify the behavior of ProfileClient methods without making actual HTTP calls.
 * Due to the nature of WebClient being reactive and requiring actual HTTP connections,
 * we test the edge cases and null handling of the client.
 */
@ExtendWith(MockitoExtension.class)
class ProfileClientTest {

    private ProfileClient profileClient;

    @BeforeEach
    void setUp() {
        // Create profile client with a mock URL
        profileClient = new ProfileClient("http://localhost:9999");
    }

    @Test
    void validateToken_WithNullToken_ReturnsNull() {
        ProfileValidateResponse result = profileClient.validateToken(null);
        
        assertNull(result);
    }

    @Test
    void validateToken_WithBlankToken_ReturnsNull() {
        ProfileValidateResponse result = profileClient.validateToken("   ");
        
        assertNull(result);
    }

    @Test
    void validateToken_WithEmptyToken_ReturnsNull() {
        ProfileValidateResponse result = profileClient.validateToken("");
        
        assertNull(result);
    }

    @Test
    void validateToken_WithInvalidServerUrl_ReturnsNull() {
        // This will fail to connect and should return null gracefully
        ProfileValidateResponse result = profileClient.validateToken("some-token");
        
        // Should not throw exception, just return null due to connection error
        assertNull(result);
    }

    @Test
    void login_WithInvalidServerUrl_ReturnsNull() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");
        
        // This will fail to connect and should return null gracefully
        LoginResponse result = profileClient.login(request);
        
        assertNull(result);
    }

    @Test
    void register_WithInvalidServerUrl_ReturnsNull() {
        Object payload = new Object();
        
        // This will fail to connect and should return null gracefully
        Object result = profileClient.register(payload);
        
        assertNull(result);
    }

    @Test
    void constructor_InitializesWithBaseUrl() {
        // Should not throw exception when creating client
        ProfileClient client = new ProfileClient("https://example.com");
        
        assertNotNull(client);
    }

    @Test
    void constructor_InitializesWithDefaultUrl() {
        // Should not throw exception when creating client with default URL pattern
        ProfileClient client = new ProfileClient("https://2306219575-be.hafizmuh.site");
        
        assertNotNull(client);
    }
}
