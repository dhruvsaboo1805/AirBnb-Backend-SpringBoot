package com.example.AirbnbBookingSpring.services.concurrency;

import com.example.AirbnbBookingSpring.exceptions.BookingException;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.repositories.writes.AvailabilityWriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockStrategy implements ConcurrencyControlStrategy {

    @Value("${LOCK_TIME_OUT_DURATION}")
    private Duration LOCK_TIMEOUT;

    @Value("${LOCK_KEY_PREFIX}")
    private String LOCK_KEY_PREFIX;

    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void releaseLock(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate) {
        String lockKey = generateLockKey(airbnbId, checkInDate, checkOutDate);
        log.debug("[releaseLock] Releasing lock - key={}", lockKey);
        redisTemplate.delete(lockKey);
        log.info("[releaseLock] Lock released - key={}", lockKey);
    }
    
    @Override
    public List<Availability> lockAndCheckAvailability(
            Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate, String customerEmail) {

        log.info("[lockAndCheckAvailability] START - airbnbId={}, checkIn={}, checkOut={}, customerEmail={}",
                airbnbId, checkInDate, checkOutDate, customerEmail);

        Long bookedSlots = availabilityWriteRepository
                .countByAirbnbIdAndDateBetweenAndBookingIdIsNotNull(airbnbId, checkInDate, checkOutDate);

        if (bookedSlots > 0) {
            log.warn("[lockAndCheckAvailability] Dates unavailable - airbnbId={}", airbnbId);
            throw new BookingException("Airbnb is not available for the given dates.");
        }

        String lockKey = generateLockKey(airbnbId, checkInDate, checkOutDate);

        boolean locked = Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, customerEmail, LOCK_TIMEOUT)
        );

        if (!locked) {
            log.warn("[lockAndCheckAvailability] Failed to acquire lock - key={}", lockKey);
            throw new IllegalStateException("Failed to acquire booking. Please try again.");
        }

        log.info("[lockAndCheckAvailability] Lock acquired - key={}", lockKey);

        try {
            return availabilityWriteRepository.findByAirbnbIdAndDateBetween(airbnbId, checkInDate, checkOutDate);
        } catch (Exception e) {
            releaseLock(airbnbId, checkInDate, checkOutDate);
            throw e;
        }
    }

    private String generateLockKey(Long airbnbId, LocalDate checkInDate, LocalDate checkOutDate) {
        return LOCK_KEY_PREFIX + airbnbId + ":" + checkInDate + ":" + checkOutDate;
    }
}