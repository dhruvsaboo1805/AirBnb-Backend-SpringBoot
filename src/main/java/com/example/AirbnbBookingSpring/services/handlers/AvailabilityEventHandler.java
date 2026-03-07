package com.example.AirbnbBookingSpring.services.handlers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.AirbnbBookingSpring.repositories.writes.AvailabilityWriteRepository;
import com.example.AirbnbBookingSpring.saga.SagaEvent;
import com.example.AirbnbBookingSpring.saga.SagaEventPublisher;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityEventHandler {

    private final AvailabilityWriteRepository availabilityWriteRepository;
    private final SagaEventPublisher sagaEventPublisher;

    @Transactional
    public void handleBookingConfirmed(SagaEvent sagaEvent) {
        Map<String, Object> payload = sagaEvent.getPayload();
        Long bookingId = Long.valueOf(payload.get("bookingId").toString());
        Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
        LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
        LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());

        // Check if another booking already claimed these dates
        Long count = availabilityWriteRepository.countBookedSlotsExcludingBooking(
                airbnbId, checkInDate, checkOutDate, bookingId
        );

        if (count > 0) {
            sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", payload);
            throw new RuntimeException("Dates already booked. Compensating.");
        }

        availabilityWriteRepository.updateBookingIdByAirbnbIdAndDateBetween(
                bookingId, airbnbId, checkInDate, checkOutDate
        );
    }

    @Transactional
    public void handleBookingCancelled(SagaEvent sagaEvent) {
        try {
            Map<String, Object> payload = sagaEvent.getPayload();
            Long bookingId = Long.valueOf(payload.get("bookingId").toString());
            Long airbnbId = Long.valueOf(payload.get("airbnbId").toString());
            LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());

            availabilityWriteRepository.updateBookingIdByAirbnbIdAndDateBetween(null, airbnbId, checkInDate, checkOutDate);

        } catch (Exception e) {
            Map<String, Object> payload = sagaEvent.getPayload();
            sagaEventPublisher.publishEvent("BOOKING_COMPENSATED", "COMPENSATE_BOOKING", payload);
            throw new RuntimeException("Failed to cancel booking", e);
        }
    }
}
