package com.example.AirbnbBookingSpring.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class RateLimiterService {

    // for distributed redis calculations
    private final Map<String , Bucket> buckets = new ConcurrentHashMap<>();

    private final Counter rateLimitBlockedCounter;

    public RateLimiterService(MeterRegistry registry) {
        this.rateLimitBlockedCounter = Counter.builder("rate_limit.blocked.total")
                .description("Total requests blocked by rate limiter")
                .register(registry);
    }

    public void incrementBlocked() {
        rateLimitBlockedCounter.increment();
    }

    // Different limits for end point basically handling different request per bucket

    public Bucket resolveBucketForBooking(String customerEmail) {
        return buckets.computeIfAbsent(customerEmail + ":booking", key -> createBookingBucket());
    }

    public Bucket resolveBucketForListing(String ownerEmail) {
        return buckets.computeIfAbsent(ownerEmail + ":listing", key -> createListingBucket());
    }

    public Bucket resolveBucketForGeneral(String email) {
        return buckets.computeIfAbsent(email + ":booking", key -> createGeneralBucket());
    }

    // Bucket Configurations

    private Bucket createBookingBucket() {
        // 5 booking requests per minute per user
        return Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket createListingBucket() {
        // 10 listing requests per minute per owner
        return Bucket.builder()
                .addLimit(Bandwidth.classic(3, Refill.greedy(10, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket createGeneralBucket() {
        // 30 general requests per minute per user
        return Bucket.builder()
                .addLimit(Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1))))
                .build();
    }



}
