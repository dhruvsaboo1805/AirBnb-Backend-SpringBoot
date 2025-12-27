package com.example.AirbnbBookingSpring.services;

import com.example.AirbnbBookingSpring.models.Booking;
import com.example.AirbnbBookingSpring.models.readModels.BookingReadModel;
import com.example.AirbnbBookingSpring.repositories.reads.RedisReadRepository;
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

    @Override
    public boolean isIdempotencyKeyUsed(String idempotencyKey) {
        return this.findBookingByIdempotencyKey(idempotencyKey).isPresent();
    }

    @Override
    public Optional<Booking> findBookingByIdempotencyKey(String idempotencyKey) {
        BookingReadModel bookingReadModel = redisReadRepository.findBookingByIdempotencyKey(idempotencyKey);

        if(bookingReadModel != null) {
            Booking booking = Booking.builder()
//                    .id(bookingReadModel.getId()) //todo check why id is not coming
                    .airbnbId(bookingReadModel.getAirbnbId())
                    .userId(bookingReadModel.getUserId())
                    .totalPrice(bookingReadModel.getTotalPrice())
                    .bookingStatus(Booking.BookingStatus.valueOf(bookingReadModel.getBookingStatus()))
                    .idempotencyKey(bookingReadModel.getIdempotencyKey())
                    .checkInDate(bookingReadModel.getCheckInDate())
                    .checkOutDate(bookingReadModel.getCheckOutDate())
                    .build();

            return Optional.of(booking);
        }
        return bookingWriteRepository.findByIdempotencyKey(idempotencyKey);
    }
}
