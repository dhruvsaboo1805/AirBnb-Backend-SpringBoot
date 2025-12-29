package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.BookingRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequestDTO;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.repositories.writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.RedisWriteRepository;
import com.example.AirbnbBookingSpring.saga.SagaEventPublisher;
import com.example.AirbnbBookingSpring.services.ImplInterfaces.IBookingService;
import com.example.AirbnbBookingSpring.services.concurrency.ConcurrencyControlStrategy;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService implements IBookingService {

    private final BookingWriteRepository bookingWriteRepository;
    private final SagaEventPublisher sagaEventPublisher;
    private final ConcurrencyControlStrategy concurrencyControlStrategy;
    private final AirbnbWriteRepository airbnbWriteRepository;
    private final RedisWriteRepository redisWriteRepository;
    private final IdempotencyService idempotencyService;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO createBookingRequest) throws JsonProcessingException {
        Airbnb airbnb = airbnbWriteRepository.findById(createBookingRequest.getAirbnbId())
                .orElseThrow(() -> new RuntimeException("Airbnb not found"));

        if(createBookingRequest.getCheckInDate().isAfter(createBookingRequest.getCheckOutDate())) {
            throw new RuntimeException("Check-in date must be before check-out date");
        }

        if(createBookingRequest.getCheckInDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Check-in date must be today or in the future");
        }

        List<Availability> availabilityList = concurrencyControlStrategy.lockAndCheckAvailability(
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate(),
                createBookingRequest.getUserId()
        );

        long nights = ChronoUnit.DAYS.between(createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate());

        BigDecimal pricePerNight = airbnb.getPricePerNight();

        BigDecimal totalPrice = pricePerNight.multiply(BigDecimal.valueOf(nights));

        String idempotencyKey = UUID.randomUUID().toString();

        log.info("Creating booking for Airbnb {} with check-in date {} and check-out date {} and total price {} and idempotency key {}",
                airbnb.getId(), createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate(), totalPrice, idempotencyKey);

        Booking booking = Booking.builder()
                .id(airbnb.getId())
                .airbnbId(airbnb.getId())
                .userId(createBookingRequest.getUserId())
                .totalPrice(totalPrice)
                .idempotencyKey(idempotencyKey)
                .bookingStatus(Booking.BookingStatus.PENDING)
                .checkInDate(createBookingRequest.getCheckInDate())
                .checkOutDate(createBookingRequest.getCheckOutDate())
                .build();

        booking = bookingWriteRepository.save(booking);

        redisWriteRepository.writeBookingReadModel(booking);

        return booking;
    }
//    1. The Major Violation: Open/Closed Principle (OCP)
//    The Principle: Software entities should be open for extension, but closed for modification.
//    The Violation: The if-else if block that checks updateBookingRequest.getBookingStatus().
//            2. The Minor Violation: Single Responsibility Principle (SRP)
//    The Principle: A class/method should have one reason to change. The Violation: This method is doing two distinct types of work:
//
//    Business Logic Flow: Validating the booking and deciding what to do.
//
//    Data Transformation: Manually constructing the Map<String, Object> payload for the event.
    @Override
    @Transactional
    public Booking updateBooking(UpdateBookingRequestDTO updateBookingRequest) {
        log.info("Updating booking for idempotency key {}", updateBookingRequest.getIdempotencyKey());
        Booking booking = idempotencyService.findBookingByIdempotencyKey(updateBookingRequest.getIdempotencyKey())
                .orElseThrow(() -> new RuntimeException("Idempotency key not found"));
        log.info("Booking found for idempotency key {}", updateBookingRequest.getIdempotencyKey());
        log.info("Booking status: {}", booking.getBookingStatus());

        if(booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Booking is not pending");
        }

        if(updateBookingRequest.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
            sagaEventPublisher.publishEvent("BOOKING_CONFIRM_REQUESTED", "CONFIRM_BOOKING", Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnbId(), "checkInDate", booking.getCheckInDate(), "checkOutDate", booking.getCheckOutDate()));
        } else if(updateBookingRequest.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnbId(), "checkInDate", booking.getCheckInDate(), "checkOutDate", booking.getCheckOutDate()));
        }
        return booking;
    }
}
