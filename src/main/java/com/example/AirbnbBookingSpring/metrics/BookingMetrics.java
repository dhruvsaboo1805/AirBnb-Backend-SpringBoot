package com.example.AirbnbBookingSpring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class BookingMetrics {

    // Counters — only go up
    private final Counter bookingCreatedCounter;
    private final Counter bookingConfirmedCounter;
    private final Counter bookingCancelledCounter;
    private final Counter bookingFailedCounter;
    private final Counter doubleBookingAttemptCounter;

    // Timer — measures duration
    private final Timer bookingCreationTimer;

    // Gauge — goes up and down
    private final AtomicInteger pendingBookingsCount = new AtomicInteger(0);

    public BookingMetrics(MeterRegistry registry) {
        this.bookingCreatedCounter = Counter.builder("booking.created.total")
                .description("Total bookings created")
                .register(registry);

        this.bookingConfirmedCounter = Counter.builder("booking.confirmed.total")
                .description("Total bookings confirmed")
                .register(registry);

        this.bookingCancelledCounter = Counter.builder("booking.cancelled.total")
                .description("Total bookings cancelled")
                .register(registry);

        this.bookingFailedCounter = Counter.builder("booking.failed.total")
                .description("Total failed booking attempts")
                .register(registry);

        this.doubleBookingAttemptCounter = Counter.builder("booking.double_booking.total")
                .description("Total double booking attempts blocked")
                .register(registry);

        this.bookingCreationTimer = Timer.builder("booking.creation.duration")
                .description("Time taken to create a booking")
                .register(registry);

        // Gauge tracks current pending bookings
        Gauge.builder("booking.pending.current", pendingBookingsCount, AtomicInteger::get)
                .description("Current number of pending bookings")
                .register(registry);
    }

    public void incrementBookingCreated() { bookingCreatedCounter.increment(); }
    public void incrementBookingConfirmed() { bookingConfirmedCounter.increment(); }
    public void incrementBookingCancelled() { bookingCancelledCounter.increment(); }
    public void incrementBookingFailed() { bookingFailedCounter.increment(); }
    public void incrementDoubleBookingAttempt() { doubleBookingAttemptCounter.increment(); }
    public void incrementPendingBookings() { pendingBookingsCount.incrementAndGet(); }
    public void decrementPendingBookings() { pendingBookingsCount.decrementAndGet(); }
    public Timer getBookingCreationTimer() { return bookingCreationTimer; }
}