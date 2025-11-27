package apap.ti._5.accommodation_2306212083_be.controller;

import apap.ti._5.accommodation_2306212083_be.client.ProfileClient;
import apap.ti._5.accommodation_2306212083_be.dto.LoginRequest;
import apap.ti._5.accommodation_2306212083_be.dto.LoginResponse;
import apap.ti._5.accommodation_2306212083_be.dto.ProfileValidateResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthProxyController {

    private static final Logger logger = LoggerFactory.getLogger(AuthProxyController.class);
    private final ProfileClient profileClient;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        logger.info("Login attempt for user: {}", loginRequest.getEmail());
        
        LoginResponse loginResponse = profileClient.login(loginRequest);
        
        if (loginResponse != null && loginResponse.getToken() != null) {
            logger.info("Login successful for user: {}", loginRequest.getEmail());
            Cookie jwtCookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(jwtCookie);
            return ResponseEntity.ok(loginResponse);
        }
        
        logger.warn("Login failed for user: {} - Invalid credentials or Profile Service unavailable", loginRequest.getEmail());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Object registerPayload) {
        Object response = profileClient.register(registerPayload);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        Map<String, Object> logoutResponse = new HashMap<>();
        logoutResponse.put("success", true);
        logoutResponse.put("message", "Logged out successfully");
        return ResponseEntity.ok(logoutResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileValidateResponse> getCurrentUser(@CookieValue(value = "JWT_TOKEN", required = false) String jwtToken) {
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        ProfileValidateResponse response = profileClient.validateToken(jwtToken);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
