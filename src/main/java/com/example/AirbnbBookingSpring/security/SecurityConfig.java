package com.example.AirbnbBookingSpring.security;

import com.example.AirbnbBookingSpring.services.RateLimiterFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimiterFilter rateLimiterFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ─── Public endpoints ──────────────────────────────────────
                        .requestMatchers("/actuator/**").permitAll()

                        // ─── Owner only — listing management ──────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/airbnb/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/airbnb/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/airbnb/**").hasRole("ADMIN")

                        // ─── Anyone authenticated can view listings ────────────────
                        .requestMatchers(HttpMethod.GET, "/api/airbnb/**").authenticated()

                        // ─── Guest only — booking management ──────────────────────
                        .requestMatchers(HttpMethod.POST,  "/api/bookings/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/bookings/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET,   "/api/bookings/**").hasRole("USER")

                        // ─── Everything else needs auth ────────────────────────────
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimiterFilter, JwtAuthFilter.class);

        return http.build();
    }
}