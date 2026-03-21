package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.models.Airbnb;
import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;
import com.example.AirbnbBookingSpring.repositories.reads.RedisReadRepository;
import com.example.AirbnbBookingSpring.repositories.writes.AirbnbWriteRepository;
import com.example.AirbnbBookingSpring.repositories.writes.BookingWriteRepository;
import com.example.AirbnbBookingSpring.services.ImplInterfaces.IIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService implements IIdempotencyService {

    private final RedisReadRepository redisReadRepository;
    private final BookingWriteRepository bookingWriteRepository;
    private final AirbnbWriteRepository airbnbWriteRepository;

    @Override
    public boolean isIdempotencyKeyUsed(String idempotencyKey) {
        log.debug("[isIdempotencyKeyUsed] Checking key={}", idempotencyKey);
        boolean used = this.findBookingByIdempotencyKey(idempotencyKey).isPresent();
        log.debug("[isIdempotencyKeyUsed] key={} used={}", idempotencyKey, used);
        return used;
    }

    @Override
    public Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey) {
        log.debug("[findBookingByIdempotencyKey] Looking up key={}", idempotencyKey);

        BookingReadModel bookingReadModel = redisReadRepository.findBookingByIdempotencyKey(idempotencyKey);

        if (bookingReadModel != null) {
            log.info("[findBookingByIdempotencyKey] Cache HIT (Redis) - key={}, bookingId={}",
                    idempotencyKey, bookingReadModel.getId());

            Airbnb airbnb = airbnbWriteRepository.findById(bookingReadModel.getAirbnbId())
                    .orElseThrow(() -> {
                        log.error("[findBookingByIdempotencyKey] Airbnb not found - id={}",
                                bookingReadModel.getAirbnbId());
                        return new RuntimeException("Airbnb not found with id: " + bookingReadModel.getAirbnbId());
                    });

            Booking booking = Booking.builder()
                    .id(bookingReadModel.getId())
                    .airbnb(airbnb)
                    .customerEmail(bookingReadModel.getCustomerEmail())
                    .totalPrice(bookingReadModel.getTotalPrice())
                    .bookingStatus(Booking.BookingStatus.valueOf(bookingReadModel.getBookingStatus()))
                    .idempotencyKey(bookingReadModel.getIdempotencyKey())
                    .checkInDate(bookingReadModel.getCheckInDate())
                    .checkOutDate(bookingReadModel.getCheckOutDate())
                    .build();

            log.debug("[findBookingByIdempotencyKey] Booking reconstructed from Redis - bookingId={}, status={}, customerEmail={}",
                    booking.getId(), booking.getBookingStatus(), booking.getCustomerEmail());
            return Optional.of(booking);
        }

        log.info("[findBookingByIdempotencyKey] Cache MISS (Redis) - falling back to DB for key={}", idempotencyKey);
        Optional<Booking> dbResult = bookingWriteRepository.findByIdempotencyKey(idempotencyKey);

        if (dbResult.isPresent()) {
            log.info("[findBookingByIdempotencyKey] Found in DB - bookingId={}, customerEmail={}",
                    dbResult.get().getId(), dbResult.get().getCustomerEmail());
        } else {
            log.warn("[findBookingByIdempotencyKey] Not found in DB or Redis - key={}", idempotencyKey);
        }

        return dbResult;
    }
}