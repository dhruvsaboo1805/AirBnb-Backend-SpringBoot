package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.BookingRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequestDTO;
import com.example.AirbnbBookingSpring.exceptions.BookingException;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.repositories.writes.*;
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
    private final AvailabilityWriteRepository availabilityWriteRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO createBookingRequest) throws JsonProcessingException {
        log.info("[createBooking] START - airbnbId={}, customerEmail={}, checkIn={}, checkOut={}",
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getEmail(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate());

        Airbnb airbnb = airbnbWriteRepository.findById(createBookingRequest.getAirbnbId())
                .orElseThrow(() -> new RuntimeException("Airbnb not found: " + createBookingRequest.getAirbnbId()));

        if (createBookingRequest.getCheckInDate().isAfter(createBookingRequest.getCheckOutDate())) {
            throw new BookingException("Check-in date must be before check-out date");
        }
        if (createBookingRequest.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BookingException("Check-in date must be today or in the future");
        }

        List<Availability> availabilityList = concurrencyControlStrategy.lockAndCheckAvailability(
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate(),
                createBookingRequest.getEmail()
        );

        long nights = ChronoUnit.DAYS.between(createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate());
        BigDecimal totalPrice = airbnb.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        String idempotencyKey = UUID.randomUUID().toString();

        Booking booking = Booking.builder()
                .airbnb(airbnb)
                .customerEmail(createBookingRequest.getEmail())
                .totalPrice(totalPrice)
                .idempotencyKey(idempotencyKey)
                .bookingStatus(Booking.BookingStatus.PENDING)
                .checkInDate(createBookingRequest.getCheckInDate())
                .checkOutDate(createBookingRequest.getCheckOutDate())
                .build();

        booking = bookingWriteRepository.save(booking);
        log.info("[createBooking] Booking saved - bookingId={}", booking.getId());

        availabilityWriteRepository.updateBookingIdByAirbnbIdAndDateBetween(
                booking.getId(),
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate()
        );

        redisWriteRepository.writeBookingReadModel(booking);
        log.info("[createBooking] END - bookingId={}", booking.getId());
        return booking;
    }

    @Override
    @Transactional
    public Booking updateBooking(UpdateBookingRequestDTO updateBookingRequest) {
        log.info("[updateBooking] START - idempotencyKey={}", updateBookingRequest.getIdempotencyKey());

        Booking booking = idempotencyService.findBookingByIdempotencyKey(updateBookingRequest.getIdempotencyKey())
                .orElseThrow(() -> new RuntimeException("Idempotency key not found"));

        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new BookingException("Booking is not PENDING. Current: " + booking.getBookingStatus());
        }

        booking.setBookingStatus(updateBookingRequest.getBookingStatus());
        booking = bookingWriteRepository.save(booking);
        redisWriteRepository.writeBookingReadModel(booking);

        Map<String, Object> payload = Map.of(
                "bookingId", booking.getId(),
                "airbnbId", booking.getAirbnb().getId(),
                "customerEmail", booking.getCustomerEmail(),
                "checkInDate", booking.getCheckInDate(),
                "checkOutDate", booking.getCheckOutDate()
        );

        if (updateBookingRequest.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
            sagaEventPublisher.publishEvent("BOOKING_CONFIRM_REQUESTED", "CONFIRM_BOOKING", payload);
        } else if (updateBookingRequest.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING", payload);
        }

        log.info("[updateBooking] END - bookingId={} status={}", booking.getId(), booking.getBookingStatus());
        return booking;
    }
}