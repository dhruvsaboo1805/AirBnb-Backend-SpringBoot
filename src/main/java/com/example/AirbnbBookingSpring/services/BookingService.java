package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.dtos.BookingRequestDTO;
import com.example.AirbnbBookingSpring.dtos.UpdateBookingRequestDTO;
import com.example.AirbnbBookingSpring.exceptions.BookingException;
import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Availability;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.User;
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
    private final UserWriteRepository userWriteRepository;
    private final AvailabilityWriteRepository availabilityWriteRepository;

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO createBookingRequest) throws JsonProcessingException {
        log.info("[createBooking] START - airbnbId={}, userId={}, checkIn={}, checkOut={}",
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getUserId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate());

        Airbnb airbnb = airbnbWriteRepository.findById(createBookingRequest.getAirbnbId())
                .orElseThrow(() -> {
                    log.error("[createBooking] Airbnb not found - id={}", createBookingRequest.getAirbnbId());
                    return new RuntimeException("Airbnb not found with id: " + createBookingRequest.getAirbnbId());
                });

        User user = userWriteRepository.findById(createBookingRequest.getUserId())
                .orElseThrow(() -> {
                    log.error("[createBooking] User not found - id={}", createBookingRequest.getUserId());
                    return new RuntimeException("User not found with id: " + createBookingRequest.getUserId());
                });

        if (createBookingRequest.getCheckInDate().isAfter(createBookingRequest.getCheckOutDate())) {
            log.warn("[createBooking] Invalid dates - checkIn={} is after checkOut={}",
                    createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate());
            throw new BookingException("Check-in date must be before check-out date");
        }

        if (createBookingRequest.getCheckInDate().isBefore(LocalDate.now())) {
            log.warn("[createBooking] Invalid checkIn date - {} is in the past", createBookingRequest.getCheckInDate());
            throw new BookingException("Check-in date must be today or in the future");
        }

        log.debug("[createBooking] Acquiring lock and checking availability for airbnbId={}", airbnb.getId());
        List<Availability> availabilityList = concurrencyControlStrategy.lockAndCheckAvailability(
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate(),
                createBookingRequest.getUserId()
        );
        log.debug("[createBooking] Lock acquired - {} availability slots found", availabilityList.size());

        long nights = ChronoUnit.DAYS.between(createBookingRequest.getCheckInDate(), createBookingRequest.getCheckOutDate());
        BigDecimal totalPrice = airbnb.getPricePerNight().multiply(BigDecimal.valueOf(nights));
        String idempotencyKey = UUID.randomUUID().toString();

        log.info("[createBooking] Calculated - nights={}, pricePerNight={}, totalPrice={}, idempotencyKey={}",
                nights, airbnb.getPricePerNight(), totalPrice, idempotencyKey);

        Booking booking = Booking.builder()
                .airbnb(airbnb)
                .user(user)
                .totalPrice(totalPrice)
                .idempotencyKey(idempotencyKey)
                .bookingStatus(Booking.BookingStatus.PENDING)
                .checkInDate(createBookingRequest.getCheckInDate())
                .checkOutDate(createBookingRequest.getCheckOutDate())
                .build();

        booking = bookingWriteRepository.save(booking);
        log.info("[createBooking] Booking saved to DB - bookingId={}", booking.getId());

        log.debug("[createBooking] Marking availability slots for bookingId={}", booking.getId());
        int updatedSlots = availabilityWriteRepository.updateBookingIdByAirbnbIdAndDateBetween(
                booking.getId(),
                createBookingRequest.getAirbnbId(),
                createBookingRequest.getCheckInDate(),
                createBookingRequest.getCheckOutDate()
        );
        log.info("[createBooking] Availability slots marked - bookingId={}, slotsUpdated={}", booking.getId(), updatedSlots);

        redisWriteRepository.writeBookingReadModel(booking);
        log.info("[createBooking] Booking written to Redis - bookingId={}", booking.getId());

        log.info("[createBooking] END - bookingId={} created successfully", booking.getId());
        return booking;
    }

    @Override
    @Transactional
    public Booking updateBooking(UpdateBookingRequestDTO updateBookingRequest) {
        log.info("[updateBooking] START - idempotencyKey={}, requestedStatus={}",
                updateBookingRequest.getIdempotencyKey(),
                updateBookingRequest.getBookingStatus());

        Booking booking = idempotencyService.findBookingByIdempotencyKey(updateBookingRequest.getIdempotencyKey())
                .orElseThrow(() -> {
                    log.error("[updateBooking] Booking not found for idempotencyKey={}", updateBookingRequest.getIdempotencyKey());
                    return new RuntimeException("Idempotency key not found: " + updateBookingRequest.getIdempotencyKey());
                });

        log.debug("[updateBooking] Booking found - bookingId={}, currentStatus={}", booking.getId(), booking.getBookingStatus());

        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            log.warn("[updateBooking] Booking is not PENDING - bookingId={}, currentStatus={}",
                    booking.getId(), booking.getBookingStatus());
            throw new BookingException("Booking is not in PENDING state. Current status: " + booking.getBookingStatus());
        }

        booking.setBookingStatus(updateBookingRequest.getBookingStatus());
        booking = bookingWriteRepository.save(booking);
        log.info("[updateBooking] Booking status updated in DB - bookingId={}, newStatus={}", booking.getId(), booking.getBookingStatus());

        redisWriteRepository.writeBookingReadModel(booking);
        log.info("[updateBooking] Booking status synced to Redis - bookingId={}", booking.getId());

        if (updateBookingRequest.getBookingStatus() == Booking.BookingStatus.CONFIRMED) {
            log.info("[updateBooking] Publishing BOOKING_CONFIRM_REQUESTED event - bookingId={}, airbnbId={}",
                    booking.getId(), booking.getAirbnb().getId());
            sagaEventPublisher.publishEvent("BOOKING_CONFIRM_REQUESTED", "CONFIRM_BOOKING",
                    Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnb().getId(),
                            "checkInDate", booking.getCheckInDate(), "checkOutDate", booking.getCheckOutDate()));

        } else if (updateBookingRequest.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            log.info("[updateBooking] Publishing BOOKING_CANCEL_REQUESTED event - bookingId={}, airbnbId={}",
                    booking.getId(), booking.getAirbnb().getId());
            sagaEventPublisher.publishEvent("BOOKING_CANCEL_REQUESTED", "CANCEL_BOOKING",
                    Map.of("bookingId", booking.getId(), "airbnbId", booking.getAirbnb().getId(),
                            "checkInDate", booking.getCheckInDate(), "checkOutDate", booking.getCheckOutDate()));
        }

        log.info("[updateBooking] END - bookingId={} updated to status={}", booking.getId(), booking.getBookingStatus());
        return booking;
    }
}