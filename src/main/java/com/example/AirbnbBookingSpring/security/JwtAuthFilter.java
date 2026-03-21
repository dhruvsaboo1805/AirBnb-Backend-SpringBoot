package com.example.AirbnbBookingSpring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // ─── No token ────────────────────────────────────────────────────────
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[JwtAuthFilter] No Bearer token - {} {}", request.getMethod(), request.getRequestURI());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authorization header is missing");
            return;
        }

        // ─── Validate via auth microservice ──────────────────────────────────
        try {
            AuthServiceClient.UserDetailsDTO userDetails = authServiceClient.validate(authHeader);

            request.setAttribute("email", userDetails.getEmail());
            request.setAttribute("roles", userDetails.getRoles());

            log.debug("[JwtAuthFilter] Authenticated - email={}, roles={}",
                    userDetails.getEmail(), userDetails.getRoles());

            List<SimpleGrantedAuthority> authorities = userDetails.getRoles()
                    .stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails.getEmail(),
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("[JwtAuthFilter] Authentication failed - {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}