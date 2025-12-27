package com.example.AirbnbBookingSpring.services.ImplInterfaces;

import com.example.AirbnbBookingSpring.models.Booking;

import java.util.Optional;

public interface IIdempotencyService {
    boolean isIdempotencyKeyUsed(String idempotencyKey);

    Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey);
}
