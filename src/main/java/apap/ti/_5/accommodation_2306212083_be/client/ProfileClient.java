package apap.ti._5.accommodation_2306212083_be.client;

import apap.ti._5.accommodation_2306212083_be.dto.LoginRequest;
import apap.ti._5.accommodation_2306212083_be.dto.LoginResponse;
import apap.ti._5.accommodation_2306212083_be.dto.LoginWrapper;
import apap.ti._5.accommodation_2306212083_be.dto.ProfileValidateResponse;
import apap.ti._5.accommodation_2306212083_be.dto.ProfileValidateWrapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

import java.time.Duration;

@Component
public class ProfileClient {

    private static final Logger logger = LoggerFactory.getLogger(ProfileClient.class);
    private final WebClient webClient;

    public ProfileClient(@Value("${profile.service.base-url:://2306219575-be.hafizmuh.site}") String baseUrl) {
        logger.info("Initializing ProfileClient with base URL: {}", baseUrl);

        // Configure HttpClient to trust all certificates (INSECURE - FOR DEV ONLY)
        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> {
                    try {
                        SslContext sslContext = SslContextBuilder.forClient()
                                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                .build();
                        sslContextSpec.sslContext(sslContext);
                    } catch (Exception e) {
                        logger.error("Failed to configure SSL context", e);
                    }
                });

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ProfileValidateResponse validateToken(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            logger.debug("Validating token with Profile Service...");
            Mono<ProfileValidateWrapper> mono = webClient.post()
                    .uri("/api/auth/validate-token")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ProfileValidateWrapper.class)
                    .timeout(Duration.ofSeconds(5)) // Timeout after 5 seconds
                    .doOnError(e -> logger.error("Error validating token: {}", e.getMessage()))
                    .onErrorReturn(new ProfileValidateWrapper());

            ProfileValidateWrapper wrapper = mono.block();
            return (wrapper != null) ? wrapper.getData() : null;
        } catch (Exception ex) {
            logger.error("Exception during token validation: {}", ex.getMessage());
            return null;
        }
    }

    public LoginResponse login(LoginRequest req) {
        try {
            logger.info("Attempting login for email: {}", req.getEmail());

            // Construct payload with email and password as requested
            var payload = new java.util.HashMap<String, String>();
            payload.put("email", req.getEmail());
            payload.put("password", req.getPassword());

            Mono<LoginWrapper> mono = webClient.post()
                    .uri("/api/auth/login")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(LoginWrapper.class)
                    .timeout(Duration.ofSeconds(5)) // Timeout after 5 seconds
                    .doOnError(e -> {
                        logger.error("Error during login request: {}", e.getMessage());
                        if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            String responseBody = ((org.springframework.web.reactive.function.client.WebClientResponseException) e).getResponseBodyAsString();
                            logger.error("External Service Error Body: {}", responseBody);
                        }
                    })
                    .onErrorReturn(new LoginWrapper());

            LoginWrapper wrapper = mono.block();
            if (wrapper == null) {
                logger.warn("Login response wrapper is null");
                return null;
            }
            if (wrapper.getData() == null) {
                logger.warn("Login response data is null. Status: {}, Message: {}", wrapper.getStatus(), wrapper.getMessage());
            }
            return wrapper.getData();
        } catch (Exception ex) {
            logger.error("Exception during login: {}", ex.getMessage());
            return null;
        }
    }

    public Object register(Object payload) {
        try {
            logger.info("Attempting registration...");
            Mono<Object> mono = webClient.post()
                    .uri("/api/auth/register")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(5)) // Timeout after 5 seconds
                    .doOnError(e -> logger.error("Error during registration: {}", e.getMessage()))
                    .onErrorReturn(null);
            return mono.block();
        } catch (Exception ex) {
            logger.error("Exception during registration: {}", ex.getMessage());
            return null;
        }
    }
}
