package com.example.AirbnbBookingSpring.services;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String email = request.getAttribute("email").toString();
        String path = request.getServletPath();

        // skip rate limiting for public endpoints
        if (email == null) {
            filterChain.doFilter(request, response);
        }

        Bucket bucket;
        if(path.startsWith("/api/bookings/")) {
            bucket = rateLimiterService.resolveBucketForBooking(email);
        } else if (path.startsWith("/api/airbnb/")) {
            bucket = rateLimiterService.resolveBucketForListing(email);
        } else {
            bucket = rateLimiterService.resolveBucketForGeneral(email);
        }

//        Checks if at least 1 token is available
//        If yes → removes 1 token from bucket → sets isConsumed = true
//        If no → leaves bucket unchanged → sets isConsumed = false
//        Returns probe with full details
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // add remaining tokens to response header
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            log.debug("[RateLimiter] allowed - email={}, path={}, remaining={}",
                    email, path, probe.getRemainingTokens());
            filterChain.doFilter(request, response);
        } else {
            // rate limit exceeded
            long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitSeconds));
            rateLimiterService.incrementBlocked();
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "error": "Too Many Requests",
                        "message": "Rate limit exceeded. Try again in %d seconds.",
                        "status": 429
                    }
                    """.formatted(waitSeconds));
            log.warn("[RateLimiter] BLOCKED - email={}, path={}, retryAfter={}s",
                    email, path, waitSeconds);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator/");
    }
}