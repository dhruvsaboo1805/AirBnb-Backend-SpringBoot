package com.example.AirbnbBookingSpring.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    public UserDetailsDTO validate(String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizationHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.debug("[AuthServiceClient] Calling auth service - url={}/api/v1/auth/validate", authServiceUrl);

        try {
            ResponseEntity<UserDetailsDTO> response = restTemplate.exchange(
                    authServiceUrl + "/api/v1/auth/validate",
                    HttpMethod.GET,
                    entity,
                    UserDetailsDTO.class
            );

            UserDetailsDTO userDetails = response.getBody();
            log.info("[AuthServiceClient] Token valid - email={}, roles={}",
                    userDetails.getEmail(), userDetails.getRoles());

            return userDetails;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("[AuthServiceClient] Token invalid or expired");
            throw new RuntimeException("Invalid or expired token");

        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("[AuthServiceClient] Token forbidden");
            throw new RuntimeException("Access denied");

        } catch (ResourceAccessException e) {
            log.error("[AuthServiceClient] Auth service unreachable - {}", e.getMessage());
            throw new RuntimeException("Auth service is currently unavailable. Please try again later.");

        } catch (Exception e) {
            log.error("[AuthServiceClient] Unexpected error - {}", e.getMessage());
            throw new RuntimeException("Token validation failed");
        }
    }

    @Data
    public static class UserDetailsDTO {
        private String email;
        private List<String> roles;
        private boolean isValid;
    }
}